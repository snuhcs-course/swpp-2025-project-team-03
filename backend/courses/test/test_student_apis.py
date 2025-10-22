import pytest
from django.contrib.auth import get_user_model
from django.urls import reverse
from rest_framework import status
from rest_framework.test import APIClient

from .test_factories import CourseClassFactory, EnrollmentFactory, StudentFactory, TeacherFactory

Account = get_user_model()

# 모든 테스트에서 DB 접근 허용
pytestmark = pytest.mark.django_db


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
    """학생 삭제 API 테스트"""

    def test_delete_student(self, api_client, student):
        """학생 삭제 성공 테스트"""
        url = reverse("student-detail", kwargs={"id": student.id})
        data = {"reason": "Test deletion"}

        response = api_client.delete(url, data, format="json")

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert "성공적으로 삭제" in response.data["data"]["message"]

        # DB에서 확인 (실제로 삭제되었는지)
        with pytest.raises(Account.DoesNotExist):
            Account.objects.get(id=student.id)

    def test_delete_student_without_reason(self, api_client, student):
        """이유 없이 학생 삭제 테스트"""
        url = reverse("student-detail", kwargs={"id": student.id})
        data = {}

        response = api_client.delete(url, data, format="json")

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True

    def test_delete_nonexistent_student(self, api_client):
        """존재하지 않는 학생 삭제 시도 테스트"""
        url = reverse("student-detail", kwargs={"id": 999})
        data = {"reason": "Test deletion"}

        response = api_client.delete(url, data, format="json")

        assert response.status_code == status.HTTP_404_NOT_FOUND
        assert response.data["success"] is False


# pytest 실행 예시
# pytest courses/test/test_student_apis.py -v
