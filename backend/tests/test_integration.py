from datetime import timedelta

import pytest
from assignments.models import Assignment
from catalog.models import Subject
from courses.models import CourseClass, Enrollment
from django.contrib.auth import get_user_model
from django.utils import timezone
from questions.models import Question
from rest_framework import status
from rest_framework.test import APIClient
from submissions.models import Answer, PersonalAssignment

pytestmark = pytest.mark.django_db

Account = get_user_model()


class TestFullWorkflow:
    """전체 시스템 워크플로우 통합 테스트"""

    @pytest.fixture
    def api_client(self):
        return APIClient()

    def test_complete_teacher_workflow(self, api_client):
        """교사 전체 워크플로우: 회원가입 → 클래스 생성 → 학생 등록 → 과제 생성"""

        # ========== 1. 교사 회원가입 ==========
        teacher = Account.objects.create_user(
            email="teacher@test.com",
            password="testpass123",
            display_name="김선생",
            is_student=False,
        )
        print(f"✓ 교사 생성됨: {teacher.email}")

        # ========== 2. 과목 생성 ==========
        subject = Subject.objects.create(name="영어")
        print(f"✓ 과목 생성됨: {subject.name}")

        # ========== 3. 클래스 생성 (API 사용) ==========
        class_data = {
            "name": "1학년 A반",
            "teacher_id": teacher.id,
            "subject_name": subject.name,
            "start_date": timezone.now().isoformat(),
            "end_date": (timezone.now() + timedelta(days=180)).isoformat(),
        }

        response = api_client.post("/api/courses/classes/", class_data, format="json")
        assert response.status_code == status.HTTP_201_CREATED
        course_class_id = response.data["data"]["id"]
        print(f"✓ 클래스 생성됨: ID={course_class_id}")

        # ========== 4. 학생 생성 ==========
        students = []
        for i in range(1, 4):
            student = Account.objects.create_user(
                email=f"student{i}@test.com", password="testpass123", display_name=f"학생{i}", is_student=True
            )
            students.append(student)
            print(f"✓ 학생 생성됨: {student.email}")
        # ========== 5. 클래스에 학생 등록 ==========
        for student in students:
            # PUT 메서드는 query parameter를 사용함
            response = api_client.put(f"/api/courses/classes/{course_class_id}/students/?studentId={student.id}")
            assert response.status_code == status.HTTP_200_OK
            print(f"✓ 학생 등록됨: {student.email} → 클래스 {course_class_id}")

        # ========== 6. 클래스 학생 목록 조회 ==========
        response = api_client.get(f"/api/courses/classes/{course_class_id}/students/")
        assert response.status_code == status.HTTP_200_OK
        assert len(response.data["data"]) == 3
        print(f"✓ 클래스 학생 수: {len(response.data['data'])}")

        # ========== 7. 과제 생성 ==========
        assignment_data = {
            "class_id": course_class_id,
            "title": "영어 발음 연습",
            "description": "기본 단어 발음",
            "subject": "영어",
            "due_at": (timezone.now() + timedelta(days=7)).isoformat(),
        }

        response = api_client.post("/api/assignments/create/", assignment_data, format="json")
        assert response.status_code == status.HTTP_201_CREATED
        assignment_id = response.data["data"]["assignment_id"]
        print(f"✓ 과제 생성됨: ID={assignment_id}")

        # ========== 8. 개인 과제 자동 생성 확인 ==========
        personal_assignments = PersonalAssignment.objects.filter(assignment_id=assignment_id)
        assert personal_assignments.count() == 3
        print(f"✓ 개인 과제 자동 생성됨: {personal_assignments.count()}개")

        # ========== 9. 과제 목록 조회 (필터링) ==========
        response = api_client.get(f"/api/assignments/?teacherId={teacher.id}")
        assert response.status_code == status.HTTP_200_OK
        print("✓ 교사별 과제 목록 조회 성공")

        response = api_client.get(f"/api/assignments/?classId={course_class_id}")
        assert response.status_code == status.HTTP_200_OK
        print("✓ 클래스별 과제 목록 조회 성공")

        # ========== 10. 과제 상세 조회 ==========
        response = api_client.get(f"/api/assignments/{assignment_id}/")
        assert response.status_code == status.HTTP_200_OK
        assert response.data["data"]["title"] == "영어 발음 연습"
        print("✓ 과제 상세 조회 성공")

    def test_complete_student_workflow(self, api_client):
        """학생 전체 워크플로우: 과제 조회 → 문제 조회 → 답안 제출 → 통계 조회"""

        # ========== 사전 준비: 교사, 클래스, 학생, 과제 생성 ==========
        teacher = Account.objects.create_user(
            email="teacher@test.com", password="testpass123", display_name="김선생", is_student=False
        )

        student = Account.objects.create_user(
            email="student@test.com", password="testpass123", display_name="김학생", is_student=True
        )

        subject = Subject.objects.create(name="영어")

        # CourseClass 직접 생성 (올바른 필드 사용)
        course_class = CourseClass.objects.create(
            name="1학년 A반",
            teacher=teacher,
            subject=subject,
            start_date=timezone.now(),
            end_date=timezone.now() + timedelta(days=180),
        )
        Enrollment.objects.create(student=student, course_class=course_class, status=Enrollment.Status.ENROLLED)

        assignment = Assignment.objects.create(
            course_class=course_class,
            subject=subject,
            title="영어 발음 연습",
            description="기본 단어 발음",
            visible_from=timezone.now(),  # 필수 필드 추가
            due_at=timezone.now() + timedelta(days=7),
        )

        personal_assignment = PersonalAssignment.objects.create(
            student=student, assignment=assignment, status=PersonalAssignment.Status.NOT_STARTED, solved_num=0
        )

        # 문제 생성
        questions = []
        for i in range(1, 6):
            question = Question.objects.create(
                personal_assignment=personal_assignment,
                number=i,
                content=f"Apple{i}을 발음하세요",
                model_answer=f"애플{i}",
                explanation=f"설명 {i}",
                difficulty="EASY",
                recalled_num=0,
            )
            questions.append(question)

        print(f"✓ 준비 완료: 교사, 학생, 클래스, 과제, 문제({len(questions)}개)")

        # ========== 1. 개인 과제 목록 조회 ==========
        response = api_client.get(f"/api/personal_assignments/?student_id={student.id}")
        assert response.status_code == status.HTTP_200_OK
        assert len(response.data["data"]) == 1
        print(f"✓ 개인 과제 조회 성공: {len(response.data['data'])}개")

        # ========== 2. 개인 과제 문제 목록 조회 ==========
        response = api_client.get(f"/api/personal_assignments/{personal_assignment.id}/questions/")
        assert response.status_code == status.HTTP_200_OK
        assert len(response.data["data"]) == 5
        print(f"✓ 문제 목록 조회 성공: {len(response.data['data'])}개")

        # ========== 3. 답안 제출 (모의) ==========
        for idx, question in enumerate(questions[:3]):
            Answer.objects.create(
                question=question,
                student=student,
                text_answer=f"모의 답변 {idx + 1}",
                state=Answer.State.CORRECT if idx % 2 == 0 else Answer.State.INCORRECT,
                eval_grade=0 + idx * 2,
                started_at=timezone.now() - timedelta(minutes=5),
                submitted_at=timezone.now(),
            )
        print("✓ 답안 제출 완료: 3개")

        # PersonalAssignment 상태 업데이트
        personal_assignment.status = PersonalAssignment.Status.IN_PROGRESS
        personal_assignment.solved_num = 3
        personal_assignment.save()

        # ========== 4. 개인 과제 통계 조회 ==========
        response = api_client.get(f"/api/personal_assignments/{personal_assignment.id}/statistics/")
        assert response.status_code == status.HTTP_200_OK
        stats = response.data["data"]
        assert stats["total_questions"] == 5
        assert stats["answered_questions"] == 3
        assert stats["correct_answers"] == 2
        print(f"✓ 통계 조회 성공: {stats['answered_questions']}/{stats['total_questions']} 완료")

        # ========== 5. 과제별 개인 과제 목록 조회 ==========
        response = api_client.get(f"/api/personal_assignments/?assignment_id={assignment.id}")
        assert response.status_code == status.HTTP_200_OK
        assert len(response.data["data"]) == 1
        print("✓ 과제별 개인 과제 조회 성공")


class TestErrorHandling:
    """에러 처리 통합 테스트"""

    @pytest.fixture
    def api_client(self):
        return APIClient()

    def test_invalid_class_creation(self, api_client):
        """존재하지 않는 teacher_id로 클래스 생성 시도"""
        class_data = {
            "name": "테스트반",
            "teacher_id": 99999,
            "subject_name": "영어",
            "start_date": timezone.now().isoformat(),
            "end_date": (timezone.now() + timedelta(days=180)).isoformat(),
        }
        response = api_client.post("/api/courses/classes/", class_data, format="json")
        assert response.status_code in [
            status.HTTP_400_BAD_REQUEST,
            status.HTTP_404_NOT_FOUND,
            status.HTTP_500_INTERNAL_SERVER_ERROR,
        ]
        print("✓ 잘못된 클래스 생성 요청 차단됨")

    def test_invalid_personal_assignment_query(self, api_client):
        """필수 파라미터 없이 개인 과제 조회"""
        response = api_client.get("/api/personal_assignments/")
        assert response.status_code == status.HTTP_400_BAD_REQUEST
        print("✓ 필수 파라미터 없는 조회 차단됨")

    def test_nonexistent_assignment_detail(self, api_client):
        """존재하지 않는 과제 상세 조회"""
        response = api_client.get("/api/assignments/99999/")
        assert response.status_code == status.HTTP_404_NOT_FOUND
        print("✓ 존재하지 않는 과제 조회 시 404 반환")


class TestCrossModuleIntegration:
    """모듈 간 연동 테스트"""

    @pytest.fixture
    def api_client(self):
        return APIClient()

    @pytest.fixture
    def setup_data(self):
        """공통 데이터 설정"""
        teacher = Account.objects.create_user(
            email="teacher@test.com", password="testpass123", display_name="김선생", is_student=False
        )

        subject = Subject.objects.create(name="영어")

        course_class = CourseClass.objects.create(
            name="1학년 A반",
            teacher=teacher,
            subject=subject,
            start_date=timezone.now(),
            end_date=timezone.now() + timedelta(days=180),
        )

        students = []
        for i in range(1, 4):
            student = Account.objects.create_user(
                email=f"student{i}@test.com", password="testpass123", display_name=f"학생{i}", is_student=True
            )
            Enrollment.objects.create(student=student, course_class=course_class, status=Enrollment.Status.ENROLLED)
            students.append(student)

        return {
            "teacher": teacher,
            "subject": subject,
            "course_class": course_class,
            "students": students,
        }

    def test_assignment_creation_triggers_personal_assignments(self, api_client, setup_data):
        """과제 생성 시 등록된 학생 수만큼 개인 과제 자동 생성"""
        assignment_data = {
            "class_id": setup_data["course_class"].id,
            "title": "테스트 과제",
            "subject": "영어",
            "due_at": (timezone.now() + timedelta(days=7)).isoformat(),
        }

        response = api_client.post("/api/assignments/create/", assignment_data, format="json")
        assert response.status_code == status.HTTP_201_CREATED
        assignment_id = response.data["data"]["assignment_id"]

        personal_count = PersonalAssignment.objects.filter(assignment_id=assignment_id).count()
        assert personal_count == 3
        print(f"✓ 과제 생성 시 개인 과제 자동 생성: {personal_count}개")

    def test_class_deletion_cascades(self, api_client, setup_data):
        """클래스 삭제 시 관련 데이터 cascade 삭제"""
        course_class_id = setup_data["course_class"].id

        assignment = Assignment.objects.create(
            course_class=setup_data["course_class"],
            subject=setup_data["subject"],
            title="테스트 과제",
            visible_from=timezone.now(),
            due_at=timezone.now() + timedelta(days=7),
        )

        response = api_client.delete(f"/api/courses/classes/{course_class_id}/")
        assert response.status_code == status.HTTP_200_OK

        assert not CourseClass.objects.filter(id=course_class_id).exists()
        assert not Enrollment.objects.filter(course_class_id=course_class_id).exists()
        assert not Assignment.objects.filter(course_class_id=course_class_id).exists()
        assert not PersonalAssignment.objects.filter(assignment__course_class_id=course_class_id).exists()
        print("✓ 클래스 삭제 성공")


class TestAPIResponseFormat:
    """API 응답 포맷 일관성 테스트"""

    @pytest.fixture
    def api_client(self):
        return APIClient()

    @pytest.fixture
    def setup_minimal_data(self):
        """최소 데이터 설정"""
        teacher = Account.objects.create_user(
            email="teacher@test.com", password="testpass123", display_name="김선생", is_student=False
        )

        subject = Subject.objects.create(name="영어")

        course_class = CourseClass.objects.create(
            name="1학년 A반",
            teacher=teacher,
            subject=subject,
            start_date=timezone.now(),
            end_date=timezone.now() + timedelta(days=180),
        )

        return {"teacher": teacher, "subject": subject, "course_class": course_class}

    def test_all_success_responses_have_standard_format(self, api_client, setup_minimal_data):
        """모든 성공 응답이 표준 포맷을 가지는지 확인"""
        endpoints = [
            "/api/courses/classes/",
            f"/api/courses/classes/{setup_minimal_data['course_class'].id}/",
            "/api/assignments/",
        ]

        for endpoint in endpoints:
            response = api_client.get(endpoint)
            if response.status_code == 200:
                assert "success" in response.data
                assert "data" in response.data
                assert "message" in response.data
                print(f"✓ {endpoint} 응답 포맷 일치")


if __name__ == "__main__":
    pytest.main([__file__, "-v", "-s"])
