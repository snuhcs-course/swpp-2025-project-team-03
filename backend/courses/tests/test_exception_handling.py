from datetime import timedelta
from unittest.mock import patch

import pytest
from catalog.models import Subject
from courses.models import CourseClass
from django.contrib.auth import get_user_model
from django.urls import reverse
from django.utils import timezone
from rest_framework import status
from rest_framework.test import APIClient

from .test_courseclass_factories import EnrollmentFactory

Account = get_user_model()

pytestmark = pytest.mark.django_db


@pytest.fixture
def api_client():
    return APIClient()


@pytest.fixture
def teacher():
    return Account.objects.create_user(
        email="teacher@test.com", password="testpass123", display_name="Test Teacher", is_student=False
    )


@pytest.fixture
def student():
    return Account.objects.create_user(
        email="student@test.com", password="testpass123", display_name="Test Student", is_student=True
    )


@pytest.fixture
def subject():
    return Subject.objects.create(name="Mathematics")


@pytest.fixture
def course_class(teacher, subject):
    return CourseClass.objects.create(
        teacher=teacher,
        subject=subject,
        name="Test Class",
        description="Test Description",
        start_date=timezone.now(),
        end_date=timezone.now() + timedelta(days=90),
    )


@pytest.fixture
def enrollment(student, course_class):
    return EnrollmentFactory(student=student, course_class=course_class)


class TestCoursesExceptionHandling:
    """Courses views의 exception 처리 테스트"""

    def test_student_list_view_exception(self, api_client):
        """StudentListView의 exception 처리 테스트"""
        url = reverse("student-list")
        with patch("courses.views.Account.objects.filter") as mock_filter:
            mock_filter.side_effect = Exception("Database error")

            response = api_client.get(url, format="json")

            assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
            assert response.data["success"] is False
            assert "학생 목록 조회 중 오류가 발생했습니다" in response.data["message"]

    def test_student_detail_view_exception(self, api_client):
        """StudentDetailView의 exception 처리 테스트"""
        url = reverse("student-detail", kwargs={"id": 1})
        with patch("courses.views.Account.objects.get") as mock_get:
            mock_get.side_effect = Exception("Database error")

            response = api_client.get(url, format="json")

            assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
            assert response.data["success"] is False
            assert "학생 상세 조회 중 오류가 발생했습니다" in response.data["message"]

    def test_student_edit_view_exception(self, api_client, student):
        """StudentEditView의 exception 처리 테스트 (line 150-152)"""
        url = reverse("student-detail", kwargs={"id": student.id})
        with patch("courses.views.Account.objects.get") as mock_get:
            mock_get.side_effect = Exception("Database error")

            response = api_client.put(
                url, {"display_name": "Updated Name", "phone_number": "010-1234-5678"}, format="json"
            )

            assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
            assert response.data["success"] is False
            assert "학생 정보 수정 중 오류가 발생했습니다" in response.data["message"]

    def test_student_delete_view_invalid_serializer(self, api_client, student, course_class, enrollment):
        """ClassStudentDeleteView는 request body를 받지 않으므로 이 테스트는 더 이상 유효하지 않음"""
        # 새로운 API는 request body를 받지 않으므로 이 테스트는 스킵
        pass

    def test_student_delete_view_exception(self, api_client, student, course_class, enrollment):
        """ClassStudentDeleteView의 exception 처리 테스트"""
        url = reverse("class-student-delete", kwargs={"id": course_class.id, "student_id": student.id})
        with patch("courses.views.CourseClass.objects.get") as mock_get:
            mock_get.side_effect = Exception("Database error")

            response = api_client.delete(url)

            assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
            assert response.data["success"] is False
            assert "학생 제거 중 오류가 발생했습니다" in response.data["message"]

    def test_student_statistics_view_exception(self, api_client, student):
        """StudentStatisticsView의 exception 처리 테스트 (line 238-240)"""
        url = reverse("student-statistics", kwargs={"id": student.id})
        with patch("courses.views.Account.objects.get") as mock_get:
            mock_get.side_effect = Exception("Database error")

            response = api_client.get(url, format="json")

            assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
            assert response.data["success"] is False
            assert "학생 진도 통계량 조회 중 오류가 발생했습니다" in response.data["message"]

    def test_class_list_view_exception(self, api_client):
        """ClassListView의 exception 처리 테스트 (line 275-277)"""
        url = reverse("class-list")
        with patch("courses.views.CourseClass.objects.all") as mock_all:
            mock_all.side_effect = Exception("Database error")

            response = api_client.get(url, format="json")

            assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
            assert response.data["success"] is False
            assert "클래스 목록 조회 중 오류가 발생했습니다" in response.data["message"]

    def test_class_detail_view_get_exception(self, api_client, course_class):
        """ClassDetailView GET의 exception 처리 테스트 (line 328-330)"""
        url = reverse("class-detail", kwargs={"id": course_class.id})
        with patch("courses.views.CourseClass.objects.get") as mock_get:
            mock_get.side_effect = Exception("Database error")

            response = api_client.get(url, format="json")

            assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
            assert response.data["success"] is False
            assert "클래스 상세 조회 중 오류가 발생했습니다" in response.data["message"]

    def test_class_detail_view_put_exception(self, api_client, course_class):
        """ClassDetailView PUT의 exception 처리 테스트 (line 368-370)"""
        url = reverse("class-detail", kwargs={"id": course_class.id})
        with patch("courses.views.CourseClass.objects.get") as mock_get:
            mock_get.side_effect = Exception("Database error")

            response = api_client.put(url, {"name": "Updated Class"}, format="json")

            assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
            assert response.data["success"] is False
            assert "클래스 정보 수정 중 오류가 발생했습니다" in response.data["message"]

    def test_class_detail_view_delete_exception(self, api_client, course_class):
        """ClassDetailView DELETE의 exception 처리 테스트 (line 401-403)"""
        url = reverse("class-detail", kwargs={"id": course_class.id})
        with patch("courses.views.CourseClass.objects.get") as mock_get:
            mock_get.side_effect = Exception("Database error")

            response = api_client.delete(url, format="json")

            assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
            assert response.data["success"] is False
            assert "클래스 삭제 중 오류가 발생했습니다" in response.data["message"]

    def test_class_students_view_get_exception(self, api_client, course_class):
        """ClassStudentsView GET의 exception 처리 테스트 (line 433-435)"""
        url = reverse("class-students", kwargs={"id": course_class.id})
        with patch("courses.views.CourseClass.objects.get") as mock_get:
            mock_get.side_effect = Exception("Database error")

            response = api_client.get(url, format="json")

            assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
            assert response.data["success"] is False
            assert "클래스 학생 목록 조회 중 오류가 발생했습니다" in response.data["message"]

    def test_class_students_view_put_exception(self, api_client, course_class, student):
        """ClassStudentsView PUT의 exception 처리 테스트 (line 524-526)"""
        url = reverse("class-students", kwargs={"id": course_class.id})
        with patch("courses.views.CourseClass.objects.get") as mock_get:
            mock_get.side_effect = Exception("Database error")

            response = api_client.put(url, {"student_ids": [student.id]}, format="json")

            assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
            assert response.data["success"] is False
            assert "학생 등록 중 오류가 발생했습니다" in response.data["message"]
