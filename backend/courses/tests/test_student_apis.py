import pytest
from courses.models import Enrollment
from django.contrib.auth import get_user_model
from django.urls import reverse
from rest_framework import status
from rest_framework.test import APIClient

from .test_courseclass_factories import CourseClassFactory, EnrollmentFactory, StudentFactory, TeacherFactory

Account = get_user_model()

# 모든 테스트에서 DB 접근 허용
pytestmark = pytest.mark.django_db

# pytest 실행 예시
# pytest courses/tests/test_student_apis.py -v


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
def course_class(teacher):
    return CourseClassFactory(teacher=teacher)


@pytest.fixture
def enrollment(student, course_class):
    return EnrollmentFactory(student=student, course_class=course_class)


class TestStudentListView:
    """학생 목록 조회 API 테스트"""

    def test_get_all_students(self, api_client, student):
        """전체 학생 목록 조회 테스트"""
        url = reverse("student-list")
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert len(response.data["data"]) == 1
        assert response.data["data"][0]["id"] == student.id
        assert response.data["data"][0]["email"] == student.email
        assert response.data["data"][0]["is_student"] is True

    def test_get_students_with_teacher_filter(self, api_client, teacher, student, enrollment):
        """teacherId로 필터링된 학생 목록 조회 테스트"""
        url = reverse("student-list")
        response = api_client.get(url, {"teacherId": teacher.id})

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert len(response.data["data"]) == 1
        assert response.data["data"][0]["id"] == student.id

    def test_get_students_with_class_filter(self, api_client, student, course_class, enrollment):
        """classId로 필터링된 학생 목록 조회 테스트"""
        url = reverse("student-list")
        response = api_client.get(url, {"classId": course_class.id})

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert len(response.data["data"]) == 1
        assert response.data["data"][0]["id"] == student.id

    def test_get_students_no_results(self, api_client):
        """학생이 없을 때 빈 목록 반환 테스트"""
        url = reverse("student-list")
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert len(response.data["data"]) == 0


class TestStudentDetailView:
    """학생 상세 조회 API 테스트"""

    def test_get_student_detail(self, api_client, student):
        """학생 상세 조회 성공 테스트"""
        url = reverse("student-detail", kwargs={"id": student.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert response.data["data"]["id"] == student.id
        assert response.data["data"]["email"] == student.email
        assert response.data["data"]["display_name"] == student.display_name

    def test_get_student_detail_not_found(self, api_client):
        """존재하지 않는 학생 조회 테스트"""
        url = reverse("student-detail", kwargs={"id": 999})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_404_NOT_FOUND
        assert response.data["success"] is False
        assert "Student not found" in response.data["error"]

    def test_get_teacher_as_student_fails(self, api_client, teacher):
        """선생님을 학생으로 조회하려 할 때 실패 테스트"""
        url = reverse("student-detail", kwargs={"id": teacher.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_404_NOT_FOUND
        assert response.data["success"] is False


class TestStudentEditView:
    """학생 정보 수정 API 테스트"""

    def test_edit_student_display_name(self, api_client, student):
        """학생 display_name 수정 테스트"""
        url = reverse("student-detail", kwargs={"id": student.id})
        new_name = "Updated Student Name"
        data = {"display_name": new_name}

        response = api_client.put(url, data, format="json")

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert response.data["data"]["display_name"] == new_name

        # DB에서 확인
        student.refresh_from_db()
        assert student.display_name == new_name

    def test_edit_student_email(self, api_client, student):
        """학생 email 수정 테스트"""
        url = reverse("student-detail", kwargs={"id": student.id})
        new_email = "updated@example.com"
        data = {"email": new_email}

        response = api_client.put(url, data, format="json")

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert response.data["data"]["email"] == new_email

        # DB에서 확인
        student.refresh_from_db()
        assert student.email == new_email

    def test_edit_student_multiple_fields(self, api_client, student):
        """학생 여러 필드 동시 수정 테스트"""
        url = reverse("student-detail", kwargs={"id": student.id})
        data = {"display_name": "New Name", "email": "newemail@example.com"}

        response = api_client.put(url, data, format="json")

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True

        # DB에서 확인
        student.refresh_from_db()
        assert student.display_name == "New Name"
        assert student.email == "newemail@example.com"

    def test_edit_student_invalid_data(self, api_client, student):
        """잘못된 데이터로 학생 수정 시도 테스트"""
        url = reverse("student-detail", kwargs={"id": student.id})
        data = {"email": "invalid-email"}  # 잘못된 이메일 형식

        response = api_client.put(url, data, format="json")

        assert response.status_code == status.HTTP_400_BAD_REQUEST
        assert response.data["success"] is False

    def test_edit_nonexistent_student(self, api_client):
        """존재하지 않는 학생 수정 시도 테스트"""
        url = reverse("student-detail", kwargs={"id": 999})
        data = {"display_name": "New Name"}

        response = api_client.put(url, data, format="json")

        assert response.status_code == status.HTTP_404_NOT_FOUND
        assert response.data["success"] is False


class TestStudentDeleteView:
    """반에서 학생 제거 API 테스트"""

    def test_delete_student(self, api_client, student, course_class, enrollment):
        """반에서 학생 제거 성공 테스트"""
        url = reverse("class-student-delete", kwargs={"id": course_class.id, "student_id": student.id})

        response = api_client.delete(url)

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert "성공적으로 제거되었습니다" in response.data["message"]
        assert response.data["data"]["class_id"] == course_class.id
        assert response.data["data"]["student_id"] == student.id

        # DB에서 확인 (Enrollment가 삭제되었는지)
        with pytest.raises(Enrollment.DoesNotExist):
            Enrollment.objects.get(course_class=course_class, student=student)

        # 학생 계정은 삭제되지 않아야 함
        assert Account.objects.filter(id=student.id).exists()

    def test_delete_student_without_reason(self, api_client, student, course_class, enrollment):
        """반에서 학생 제거 테스트 (request body 없음)"""
        url = reverse("class-student-delete", kwargs={"id": course_class.id, "student_id": student.id})

        response = api_client.delete(url)

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True

    def test_delete_nonexistent_student(self, api_client, course_class):
        """존재하지 않는 학생 제거 시도 테스트"""
        url = reverse("class-student-delete", kwargs={"id": course_class.id, "student_id": 999})

        response = api_client.delete(url)

        assert response.status_code == status.HTTP_404_NOT_FOUND
        assert response.data["success"] is False


class TestStudentStatisticsView:
    """학생 진도 통계량 조회 API 테스트"""

    @pytest.fixture
    def assignment1(self, course_class):
        """테스트용 과제 1"""
        from datetime import timedelta

        from assignments.models import Assignment
        from catalog.models import Subject
        from django.utils import timezone

        subject = Subject.objects.create(name="Test Subject")
        return Assignment.objects.create(
            course_class=course_class,
            subject=subject,
            title="Test Assignment 1",
            description="Test Description",
            total_questions=3,
            visible_from=timezone.now(),
            due_at=timezone.now() + timedelta(days=7),
            grade="",
        )

    @pytest.fixture
    def assignment2(self, course_class):
        """테스트용 과제 2"""
        from datetime import timedelta

        from assignments.models import Assignment
        from catalog.models import Subject
        from django.utils import timezone

        subject = Subject.objects.create(name="Test Subject 2")
        return Assignment.objects.create(
            course_class=course_class,
            subject=subject,
            title="Test Assignment 2",
            description="Test Description 2",
            total_questions=5,
            visible_from=timezone.now(),
            due_at=timezone.now() + timedelta(days=7),
            grade="",
        )

    @pytest.fixture
    def personal_assignment_not_started(self, student, assignment1):
        """NOT_STARTED 상태의 PersonalAssignment"""
        from submissions.models import PersonalAssignment

        return PersonalAssignment.objects.create(
            student=student,
            assignment=assignment1,
            status=PersonalAssignment.Status.NOT_STARTED,
            solved_num=0,
        )

    @pytest.fixture
    def personal_assignment_in_progress(self, student, assignment2):
        """IN_PROGRESS 상태의 PersonalAssignment"""
        from submissions.models import PersonalAssignment

        return PersonalAssignment.objects.create(
            student=student,
            assignment=assignment2,
            status=PersonalAssignment.Status.IN_PROGRESS,
            solved_num=2,
        )

    def test_get_student_statistics_success(self, api_client, student, personal_assignment_not_started):
        """학생 통계 조회 성공 테스트"""
        url = reverse("student-statistics", kwargs={"id": student.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        data = response.data["data"]

        # 필수 필드 확인
        assert "total_assignments" in data
        assert "submitted_assignments" in data
        assert "in_progress_assignments" in data
        assert "not_started_assignments" in data

    def test_get_student_statistics_correct_counts(
        self, api_client, student, personal_assignment_not_started, personal_assignment_in_progress
    ):
        """통계 수치가 정확한지 테스트"""
        url = reverse("student-statistics", kwargs={"id": student.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]

        # 총 과제 수: 2개
        assert data["total_assignments"] == 2

        # NOT_STARTED: 1개
        assert data["not_started_assignments"] == 1

        # IN_PROGRESS: 1개
        assert data["in_progress_assignments"] == 1

        # SUBMITTED: 0개
        assert data["submitted_assignments"] == 0

    def test_get_student_statistics_with_submitted(
        self, api_client, student, personal_assignment_not_started, personal_assignment_in_progress
    ):
        """SUBMITTED 상태가 포함된 통계 테스트"""
        from submissions.models import PersonalAssignment

        # IN_PROGRESS를 SUBMITTED로 변경
        personal_assignment_in_progress.status = PersonalAssignment.Status.SUBMITTED
        personal_assignment_in_progress.save()

        url = reverse("student-statistics", kwargs={"id": student.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]

        assert data["total_assignments"] == 2
        assert data["submitted_assignments"] == 1
        assert data["in_progress_assignments"] == 0
        assert data["not_started_assignments"] == 1

    def test_get_student_statistics_no_assignments(self, api_client, student):
        """과제가 없는 학생의 통계 조회 테스트"""
        url = reverse("student-statistics", kwargs={"id": student.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]

        assert data["total_assignments"] == 0
        assert data["submitted_assignments"] == 0
        assert data["in_progress_assignments"] == 0
        assert data["not_started_assignments"] == 0

    def test_get_student_statistics_all_submitted(self, api_client, student, assignment1, assignment2):
        """모든 과제가 SUBMITTED인 경우 테스트"""
        from submissions.models import PersonalAssignment

        PersonalAssignment.objects.create(
            student=student, assignment=assignment1, status=PersonalAssignment.Status.SUBMITTED, solved_num=3
        )
        PersonalAssignment.objects.create(
            student=student, assignment=assignment2, status=PersonalAssignment.Status.SUBMITTED, solved_num=5
        )

        url = reverse("student-statistics", kwargs={"id": student.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]

        assert data["total_assignments"] == 2
        assert data["submitted_assignments"] == 2
        assert data["in_progress_assignments"] == 0
        assert data["not_started_assignments"] == 0

    def test_get_student_statistics_all_not_started(self, api_client, student, assignment1, assignment2):
        """모든 과제가 NOT_STARTED인 경우 테스트"""
        from submissions.models import PersonalAssignment

        PersonalAssignment.objects.create(
            student=student, assignment=assignment1, status=PersonalAssignment.Status.NOT_STARTED, solved_num=0
        )
        PersonalAssignment.objects.create(
            student=student, assignment=assignment2, status=PersonalAssignment.Status.NOT_STARTED, solved_num=0
        )

        url = reverse("student-statistics", kwargs={"id": student.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]

        assert data["total_assignments"] == 2
        assert data["submitted_assignments"] == 0
        assert data["in_progress_assignments"] == 0
        assert data["not_started_assignments"] == 2

    def test_get_student_statistics_all_in_progress(self, api_client, student, assignment1, assignment2):
        """모든 과제가 IN_PROGRESS인 경우 테스트"""
        from submissions.models import PersonalAssignment

        PersonalAssignment.objects.create(
            student=student, assignment=assignment1, status=PersonalAssignment.Status.IN_PROGRESS, solved_num=1
        )
        PersonalAssignment.objects.create(
            student=student, assignment=assignment2, status=PersonalAssignment.Status.IN_PROGRESS, solved_num=2
        )

        url = reverse("student-statistics", kwargs={"id": student.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]

        assert data["total_assignments"] == 2
        assert data["submitted_assignments"] == 0
        assert data["in_progress_assignments"] == 2
        assert data["not_started_assignments"] == 0

    def test_get_student_statistics_mixed_statuses(self, api_client, student, course_class):
        """다양한 상태의 과제가 섞여 있는 경우 테스트"""
        from datetime import timedelta

        from assignments.models import Assignment
        from catalog.models import Subject
        from django.utils import timezone
        from submissions.models import PersonalAssignment

        subject = Subject.objects.create(name="Mixed Test Subject")

        # 3개의 과제 생성
        assignments = []
        for i in range(3):
            assignments.append(
                Assignment.objects.create(
                    course_class=course_class,
                    subject=subject,
                    title=f"Assignment {i}",
                    description="Test",
                    total_questions=3,
                    visible_from=timezone.now(),
                    due_at=timezone.now() + timedelta(days=7),
                    grade="",
                )
            )

        # 각각 다른 상태로 PersonalAssignment 생성
        PersonalAssignment.objects.create(
            student=student, assignment=assignments[0], status=PersonalAssignment.Status.NOT_STARTED, solved_num=0
        )
        PersonalAssignment.objects.create(
            student=student, assignment=assignments[1], status=PersonalAssignment.Status.IN_PROGRESS, solved_num=1
        )
        PersonalAssignment.objects.create(
            student=student, assignment=assignments[2], status=PersonalAssignment.Status.SUBMITTED, solved_num=3
        )

        url = reverse("student-statistics", kwargs={"id": student.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]

        assert data["total_assignments"] == 3
        assert data["not_started_assignments"] == 1
        assert data["in_progress_assignments"] == 1
        assert data["submitted_assignments"] == 1

    def test_get_student_statistics_not_found(self, api_client):
        """존재하지 않는 학생 ID로 조회 시 404 에러"""
        url = reverse("student-statistics", kwargs={"id": 99999})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_404_NOT_FOUND
        assert response.data["success"] is False
        assert "찾을 수 없습니다" in response.data["message"]

    def test_get_student_statistics_teacher_account(self, api_client, teacher):
        """교사 계정으로 조회 시 404 에러 (is_student=False)"""
        url = reverse("student-statistics", kwargs={"id": teacher.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_404_NOT_FOUND
        assert response.data["success"] is False

    def test_get_student_statistics_data_types(
        self, api_client, student, personal_assignment_not_started, personal_assignment_in_progress
    ):
        """응답 데이터의 타입이 올바른지 테스트"""
        url = reverse("student-statistics", kwargs={"id": student.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]

        # 모든 필드가 정수 타입인지 확인
        assert isinstance(data["total_assignments"], int)
        assert isinstance(data["submitted_assignments"], int)
        assert isinstance(data["in_progress_assignments"], int)
        assert isinstance(data["not_started_assignments"], int)

        # 모든 값이 0 이상인지 확인
        assert data["total_assignments"] >= 0
        assert data["submitted_assignments"] >= 0
        assert data["in_progress_assignments"] >= 0
        assert data["not_started_assignments"] >= 0

    def test_get_student_statistics_sum_equals_total(
        self, api_client, student, personal_assignment_not_started, personal_assignment_in_progress
    ):
        """각 상태의 합이 전체 과제 수와 일치하는지 테스트"""
        url = reverse("student-statistics", kwargs={"id": student.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]

        # 각 상태의 합이 전체 과제 수와 일치
        status_sum = data["submitted_assignments"] + data["in_progress_assignments"] + data["not_started_assignments"]
        assert status_sum == data["total_assignments"]

    def test_get_student_statistics_multiple_students_isolation(self, api_client, course_class):
        """여러 학생의 통계가 독립적으로 관리되는지 테스트"""
        from datetime import timedelta

        from assignments.models import Assignment
        from catalog.models import Subject
        from django.utils import timezone
        from submissions.models import PersonalAssignment

        # 두 명의 학생 생성
        student1 = StudentFactory()
        student2 = StudentFactory()

        subject = Subject.objects.create(name="Isolation Test Subject")
        assignment = Assignment.objects.create(
            course_class=course_class,
            subject=subject,
            title="Test Assignment",
            description="Test",
            total_questions=3,
            visible_from=timezone.now(),
            due_at=timezone.now() + timedelta(days=7),
            grade="",
        )

        # 학생 1: SUBMITTED 상태
        PersonalAssignment.objects.create(
            student=student1, assignment=assignment, status=PersonalAssignment.Status.SUBMITTED, solved_num=3
        )

        # 학생 2: NOT_STARTED 상태
        PersonalAssignment.objects.create(
            student=student2, assignment=assignment, status=PersonalAssignment.Status.NOT_STARTED, solved_num=0
        )

        # 학생 1 통계 조회
        url1 = reverse("student-statistics", kwargs={"id": student1.id})
        response1 = api_client.get(url1)
        data1 = response1.data["data"]

        # 학생 2 통계 조회
        url2 = reverse("student-statistics", kwargs={"id": student2.id})
        response2 = api_client.get(url2)
        data2 = response2.data["data"]

        # 학생 1: SUBMITTED 1개
        assert data1["total_assignments"] == 1
        assert data1["submitted_assignments"] == 1
        assert data1["not_started_assignments"] == 0

        # 학생 2: NOT_STARTED 1개
        assert data2["total_assignments"] == 1
        assert data2["submitted_assignments"] == 0
        assert data2["not_started_assignments"] == 1
