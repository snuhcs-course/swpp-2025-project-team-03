import pytest
from courses.models import CourseClass
from django.contrib.auth import get_user_model
from django.urls import reverse
from rest_framework import status
from rest_framework.test import APIClient

from .test_courseclass_factories import (
    CourseClassFactory,
    EnrollmentFactory,
    StudentFactory,
    SubjectFactory,
    TeacherFactory,
)

Account = get_user_model()

pytestmark = pytest.mark.django_db

# pytest 실행 예시
# pytest courses/tests/test_courseclass_apis.py -v


@pytest.fixture
def api_client():
    return APIClient()


@pytest.fixture
def teacher():
    return TeacherFactory()


@pytest.fixture
def student():
    return StudentFactory()


@pytest.fixture
def subject():
    return SubjectFactory()


@pytest.fixture
def course_class(teacher, subject):
    return CourseClassFactory(teacher=teacher, subject=subject)


@pytest.fixture
def enrollment(student, course_class):
    return EnrollmentFactory(student=student, course_class=course_class)


class TestClassListView:
    """클래스 목록 조회 API 테스트"""

    def test_get_all_classes(self, api_client, course_class):
        """전체 클래스 목록 조회 테스트"""
        url = reverse("class-list")
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert len(response.data["data"]) == 1
        assert response.data["data"][0]["id"] == course_class.id
        assert response.data["data"][0]["name"] == course_class.name

    def test_get_classes_with_teacher_filter(self, api_client, teacher, course_class):
        """teacherId로 필터링된 클래스 목록 조회 테스트"""
        url = reverse("class-list")
        response = api_client.get(url, {"teacherId": teacher.id})

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert len(response.data["data"]) == 1
        assert response.data["data"][0]["id"] == course_class.id

    def test_get_classes_no_results(self, api_client):
        """클래스가 없을 때 빈 목록 반환 테스트"""
        url = reverse("class-list")
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert len(response.data["data"]) == 0

    def test_get_classes_multiple_teachers(self, api_client):
        """여러 선생님의 클래스 필터링 테스트"""
        teacher1 = TeacherFactory()
        teacher2 = TeacherFactory()

        class1 = CourseClassFactory(teacher=teacher1)
        class2 = CourseClassFactory(teacher=teacher2)

        # teacher1의 클래스만 조회
        url = reverse("class-list")
        response = api_client.get(url, {"teacherId": teacher1.id})

        assert response.status_code == status.HTTP_200_OK
        assert len(response.data["data"]) == 1
        assert response.data["data"][0]["id"] == class1.id
        assert response.data["data"][0]["id"] != class2.id


class TestClassDetailView:
    """클래스 상세 조회 API 테스트"""

    def test_get_class_detail(self, api_client, course_class):
        """클래스 상세 조회 성공 테스트"""
        url = reverse("class-detail", kwargs={"id": course_class.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert response.data["data"]["id"] == course_class.id
        assert response.data["data"]["name"] == course_class.name
        assert response.data["data"]["description"] == course_class.description

    def test_get_class_detail_not_found(self, api_client):
        """존재하지 않는 클래스 조회 테스트"""
        url = reverse("class-detail", kwargs={"id": 999})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_404_NOT_FOUND
        assert response.data["success"] is False
        assert "Class not found" in response.data["error"]

    def test_get_class_detail_serializer_fields(self, api_client, course_class, enrollment):
        """클래스 상세 조회 시 serializer 필드 확인 테스트"""
        url = reverse("class-detail", kwargs={"id": course_class.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]

        # 필수 필드들 확인
        assert "id" in data
        assert "name" in data
        assert "description" in data
        assert "subject" in data
        assert "teacher_name" in data
        assert "start_date" in data
        assert "end_date" in data
        assert "student_count" in data
        assert "created_at" in data

        # student_count 확인
        assert data["student_count"] == 1


class TestClassUpdateView:
    """클래스 정보 수정 API 테스트"""

    def test_update_class_name(self, api_client, course_class):
        """클래스 이름 수정 테스트"""
        url = reverse("class-detail", kwargs={"id": course_class.id})
        new_name = "Updated Class Name"
        data = {"name": new_name}

        response = api_client.put(url, data, format="json")

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert response.data["data"]["name"] == new_name

        # DB에서 확인
        course_class.refresh_from_db()
        assert course_class.name == new_name

    def test_update_class_description(self, api_client, course_class):
        """클래스 설명 수정 테스트"""
        url = reverse("class-detail", kwargs={"id": course_class.id})
        new_description = "Updated description"
        data = {"description": new_description}

        response = api_client.put(url, data, format="json")

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert response.data["data"]["description"] == new_description

        # DB에서 확인
        course_class.refresh_from_db()
        assert course_class.description == new_description

    def test_update_class_multiple_fields(self, api_client, course_class):
        """클래스 여러 필드 동시 수정 테스트"""
        url = reverse("class-detail", kwargs={"id": course_class.id})
        data = {"name": "New Class Name", "description": "New description"}

        response = api_client.put(url, data, format="json")

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True

        # DB에서 확인
        course_class.refresh_from_db()
        assert course_class.name == "New Class Name"
        assert course_class.description == "New description"

    def test_update_class_invalid_data(self, api_client, course_class):
        """잘못된 데이터로 클래스 수정 시도 테스트"""
        url = reverse("class-detail", kwargs={"id": course_class.id})
        data = {"name": ""}  # 빈 이름

        response = api_client.put(url, data, format="json")

        assert response.status_code == status.HTTP_400_BAD_REQUEST
        assert response.data["success"] is False

    def test_update_nonexistent_class(self, api_client):
        """존재하지 않는 클래스 수정 시도 테스트"""
        url = reverse("class-detail", kwargs={"id": 999})
        data = {"name": "New Name"}

        response = api_client.put(url, data, format="json")

        assert response.status_code == status.HTTP_404_NOT_FOUND
        assert response.data["success"] is False


class TestClassDeleteView:
    """클래스 삭제 API 테스트"""

    def test_delete_class(self, api_client, course_class):
        """클래스 삭제 성공 테스트"""
        url = reverse("class-detail", kwargs={"id": course_class.id})
        class_name = course_class.name

        response = api_client.delete(url)

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert "성공적으로 삭제" in response.data["message"]
        assert response.data["data"]["id"] == course_class.id
        assert response.data["data"]["name"] == class_name

        # DB에서 삭제 확인
        with pytest.raises(CourseClass.DoesNotExist):
            CourseClass.objects.get(id=course_class.id)

    def test_delete_nonexistent_class(self, api_client):
        """존재하지 않는 클래스 삭제 시도 테스트"""
        url = reverse("class-detail", kwargs={"id": 999})
        response = api_client.delete(url)

        assert response.status_code == status.HTTP_404_NOT_FOUND
        assert response.data["success"] is False
        assert "Class not found" in response.data["error"]

    def test_delete_class_with_enrollments(self, api_client, course_class, enrollment):
        """등록된 학생이 있는 클래스 삭제 테스트"""
        url = reverse("class-detail", kwargs={"id": course_class.id})

        response = api_client.delete(url)

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True

        # 클래스가 삭제되었는지 확인
        with pytest.raises(CourseClass.DoesNotExist):
            CourseClass.objects.get(id=course_class.id)


class TestClassStudentsView:
    """클래스 학생 목록 조회 API 테스트"""

    def test_get_class_students(self, api_client, course_class, student, enrollment):
        """클래스 학생 목록 조회 성공 테스트"""
        url = reverse("class-students", kwargs={"id": course_class.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert len(response.data["data"]) == 1
        assert response.data["data"][0]["id"] == student.id
        assert response.data["data"][0]["email"] == student.email

    def test_get_class_students_empty_class(self, api_client, course_class):
        """학생이 없는 클래스의 학생 목록 조회 테스트"""
        url = reverse("class-students", kwargs={"id": course_class.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert len(response.data["data"]) == 0

    def test_get_class_students_nonexistent_class(self, api_client):
        """존재하지 않는 클래스의 학생 목록 조회 테스트"""
        url = reverse("class-students", kwargs={"id": 999})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_404_NOT_FOUND
        assert response.data["success"] is False
        assert "Class not found" in response.data["error"]

    def test_get_class_students_with_multiple_students(self, api_client, course_class):
        """여러 학생이 있는 클래스의 학생 목록 조회 테스트"""
        # 추가 학생들 생성
        student1 = StudentFactory()
        student2 = StudentFactory()
        EnrollmentFactory(student=student1, course_class=course_class)
        EnrollmentFactory(student=student2, course_class=course_class)

        url = reverse("class-students", kwargs={"id": course_class.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert len(response.data["data"]) == 2

        # 학생 ID들 확인
        student_ids = [student["id"] for student in response.data["data"]]
        assert student1.id in student_ids
        assert student2.id in student_ids

    def test_get_class_students_only_enrolled(self, api_client, course_class, student):
        """등록된 학생만 조회되는지 테스트"""
        # 등록되지 않은 학생 생성
        unenrolled_student = StudentFactory()

        # 등록된 학생 생성
        enrolled_student = StudentFactory()
        EnrollmentFactory(student=enrolled_student, course_class=course_class)

        url = reverse("class-students", kwargs={"id": course_class.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert len(response.data["data"]) == 1
        assert response.data["data"][0]["id"] == enrolled_student.id

        # 등록되지 않은 학생은 결과에 없어야 함
        student_ids = [student["id"] for student in response.data["data"]]
        assert unenrolled_student.id not in student_ids

    def test_enroll_student_with_student_id(self, api_client, course_class, student):
        """studentId로 학생 등록 성공 테스트"""
        url = reverse("class-students", kwargs={"id": course_class.id})
        # query parameter로 전달
        response = api_client.put(f"{url}?studentId={student.id}")

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert response.data["data"]["student_name"] == student.display_name
        assert response.data["data"]["class_name"] == course_class.name

    def test_enroll_student_no_student_id(self, api_client, course_class):
        """studentId 없이 학생 등록 시도 시 에러 테스트"""
        url = reverse("class-students", kwargs={"id": course_class.id})
        response = api_client.put(url)

        assert response.status_code == status.HTTP_400_BAD_REQUEST
        assert response.data["success"] is False
        assert "학생 ID는 필수입니다" in response.data["message"]

    def test_enroll_student_invalid_student_id_format(self, api_client, course_class):
        """잘못된 형식의 studentId로 학생 등록 시도 시 에러 테스트"""
        url = reverse("class-students", kwargs={"id": course_class.id})
        response = api_client.put(f"{url}?studentId=abc")

        assert response.status_code == status.HTTP_400_BAD_REQUEST
        assert response.data["success"] is False
        assert "학생 ID는 정수여야 합니다" in response.data["message"]

    def test_enroll_student_invalid_student_id_value(self, api_client, course_class):
        """음수 또는 0인 studentId로 학생 등록 시도 시 에러 테스트"""
        url = reverse("class-students", kwargs={"id": course_class.id})
        response = api_client.put(f"{url}?studentId=0")

        assert response.status_code == status.HTTP_400_BAD_REQUEST
        assert response.data["success"] is False
        assert "학생 ID는 양수여야 합니다" in response.data["message"]

    def test_enroll_student_not_found(self, api_client, course_class):
        """존재하지 않는 학생 등록 시도 시 에러 테스트"""
        url = reverse("class-students", kwargs={"id": course_class.id})
        # query parameter로 전달
        response = api_client.put(f"{url}?studentId=999999")

        assert response.status_code == status.HTTP_404_NOT_FOUND
        assert response.data["success"] is False
        assert "Student not found" in response.data["error"]

    def test_enroll_student_class_not_found(self, api_client, student):
        """존재하지 않는 클래스에 학생 등록 시도 시 에러 테스트"""
        url = reverse("class-students", kwargs={"id": 999999})
        # query parameter로 전달
        response = api_client.put(f"{url}?studentId={student.id}")

        assert response.status_code == status.HTTP_404_NOT_FOUND
        assert response.data["success"] is False
        assert "Class not found" in response.data["error"]

    def test_enroll_student_duplicate(self, api_client, course_class, student, enrollment):
        """이미 등록된 학생 재등록 시 중복 방지 테스트"""
        url = reverse("class-students", kwargs={"id": course_class.id})
        # query parameter로 전달
        response = api_client.put(f"{url}?studentId={student.id}")

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True

        # Enrollment가 하나만 있는지 확인
        from courses.models import Enrollment

        enrollments_count = Enrollment.objects.filter(student=student, course_class=course_class).count()
        assert enrollments_count == 1

    def test_enroll_student_not_a_student(self, api_client, course_class, teacher):
        """교사 계정을 studentId로 등록 시도 시 에러 테스트"""
        url = reverse("class-students", kwargs={"id": course_class.id})
        response = api_client.put(f"{url}?studentId={teacher.id}")

        assert response.status_code == status.HTTP_404_NOT_FOUND
        assert response.data["success"] is False
        assert "학생을 찾을 수 없습니다" in response.data["message"]


class TestClassStudentsStatisticsView:
    """반 학생 통계 조회 API 테스트"""

    def test_get_statistics_no_students(self, api_client, course_class):
        """학생이 없는 반의 통계 조회 테스트"""
        url = reverse("class-students-statistics", kwargs={"classId": course_class.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        # views.py line 584에서 overall_average_score를 직접 반환 (serializer를 거치지 않음)
        assert "overall_average_score" in response.data["data"]
        assert response.data["data"]["overall_average_score"] == 0.0
        assert response.data["data"]["students"] == []

    def test_get_statistics_no_assignments(self, api_client, course_class, student, enrollment):
        """과제가 없는 반의 통계 조회 테스트"""
        url = reverse("class-students-statistics", kwargs={"classId": course_class.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert len(response.data["data"]["students"]) == 1
        assert response.data["data"]["students"][0]["student_id"] == student.id
        assert response.data["data"]["students"][0]["average_score"] == 0.0
        assert response.data["data"]["students"][0]["completion_rate"] == 0.0

    def test_get_statistics_with_completed_assignment(self, api_client, course_class, student, enrollment):
        """완료된 과제가 있는 반의 통계 조회 테스트"""
        from datetime import timedelta

        from assignments.models import Assignment
        from django.utils import timezone
        from questions.models import Question
        from submissions.models import Answer, PersonalAssignment

        # 과제 생성
        assignment = Assignment.objects.create(
            course_class=course_class,
            subject=course_class.subject,
            title="Test Assignment",
            visible_from=timezone.now(),
            due_at=timezone.now() + timedelta(days=7),
        )

        # PersonalAssignment 생성 (SUBMITTED 상태)
        personal_assignment = PersonalAssignment.objects.create(
            student=student,
            assignment=assignment,
            status=PersonalAssignment.Status.SUBMITTED,
        )

        # Question 생성 (recalled_num=0, base question)
        question = Question.objects.create(
            personal_assignment=personal_assignment,
            number=1,
            content="Test Question",
            model_answer="Test Answer",
            difficulty=Question.Difficulty.EASY,
            recalled_num=0,
        )

        # Answer 생성 (CORRECT 상태)
        answer = Answer.objects.create(
            question=question,
            student=student,
            text_answer="Test Answer",
            state=Answer.State.CORRECT,
        )

        url = reverse("class-students-statistics", kwargs={"classId": course_class.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert len(response.data["data"]["students"]) == 1
        assert response.data["data"]["students"][0]["student_id"] == student.id
        assert response.data["data"]["students"][0]["average_score"] == 100.0
        assert response.data["data"]["students"][0]["completion_rate"] == 100.0
        assert response.data["data"]["students"][0]["total_assignments"] == 1
        assert response.data["data"]["students"][0]["completed_assignments"] == 1

    def test_get_statistics_with_recalled_questions(self, api_client, course_class, student, enrollment):
        """recalled_num이 있는 질문이 있는 반의 통계 조회 테스트"""
        from datetime import timedelta

        from assignments.models import Assignment
        from django.utils import timezone
        from questions.models import Question
        from submissions.models import Answer, PersonalAssignment

        # 과제 생성
        assignment = Assignment.objects.create(
            course_class=course_class,
            subject=course_class.subject,
            title="Test Assignment",
            visible_from=timezone.now(),
            due_at=timezone.now() + timedelta(days=7),
        )

        # PersonalAssignment 생성 (SUBMITTED 상태)
        personal_assignment = PersonalAssignment.objects.create(
            student=student,
            assignment=assignment,
            status=PersonalAssignment.Status.SUBMITTED,
        )

        # Base question (recalled_num=0)
        base_question = Question.objects.create(
            personal_assignment=personal_assignment,
            number=1,
            content="Base Question",
            model_answer="Base Answer",
            difficulty=Question.Difficulty.EASY,
            recalled_num=0,
        )

        # Recalled question (recalled_num=1)
        recalled_question = Question.objects.create(
            personal_assignment=personal_assignment,
            number=1,
            content="Recalled Question",
            model_answer="Recalled Answer",
            difficulty=Question.Difficulty.EASY,
            recalled_num=1,
        )

        # Base question에 대한 답변 (INCORRECT)
        Answer.objects.create(
            question=base_question,
            student=student,
            text_answer="Wrong Answer",
            state=Answer.State.INCORRECT,
        )

        # Recalled question에 대한 답변 (CORRECT)
        Answer.objects.create(
            question=recalled_question,
            student=student,
            text_answer="Recalled Answer",
            state=Answer.State.CORRECT,
        )

        url = reverse("class-students-statistics", kwargs={"classId": course_class.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert len(response.data["data"]["students"]) == 1
        # recalled_num=1에서 정답이므로 75점
        assert response.data["data"]["students"][0]["average_score"] == 75.0

    def test_get_statistics_nonexistent_class(self, api_client):
        """존재하지 않는 반의 통계 조회 테스트"""
        url = reverse("class-students-statistics", kwargs={"classId": 999})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_404_NOT_FOUND
        assert response.data["success"] is False
        assert "클래스를 찾을 수 없습니다" in response.data["message"]
