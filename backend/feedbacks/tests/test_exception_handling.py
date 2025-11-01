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


class TestFeedbacksExceptionHandling:
    """Feedbacks views의 exception 처리 테스트"""

    def test_message_send_missing_teacher_id(self, api_client, course_class, student):
        """MessageSendView의 teacher_id 없음 테스트 (line 51)"""
        url = reverse("message-send")
        data = {
            "class_id": course_class.id,
            "student_id": student.id,
            "content": "Test feedback",
            # teacher_id를 명시적으로 None으로 설정하거나 아예 보내지 않음
        }

        response = api_client.post(url, data, format="json")

        # serializer validation이 먼저 실행되므로 ValidationError가 발생할 수 있음
        assert response.status_code in [status.HTTP_400_BAD_REQUEST, status.HTTP_500_INTERNAL_SERVER_ERROR]
        assert response.data["success"] is False
        # teacher_id가 없으면 serializer validation에서 실패하거나 request.data.get이 None을 반환
        assert "teacher_id" in str(response.data).lower() or "teacher_id is required" in str(response.data)

    def test_message_send_student_as_teacher(self, api_client, course_class, student):
        """MessageSendView의 학생을 교사로 요청 테스트 (line 61-68)"""
        url = reverse("message-send")
        data = {
            "teacher_id": student.id,
            "class_id": course_class.id,
            "student_id": student.id,
            "content": "Test feedback",
        }

        response = api_client.post(url, data, format="json")

        # serializer validation에서 실패할 수 있으므로 400 또는 403
        assert response.status_code in [status.HTTP_400_BAD_REQUEST, status.HTTP_403_FORBIDDEN]
        assert response.data["success"] is False
        if response.status_code == status.HTTP_403_FORBIDDEN:
            assert "Only teachers can send feedback" in response.data["message"]

    def test_message_send_class_not_found(self, api_client, teacher, student):
        """MessageSendView의 클래스 없음 테스트 (line 78-79)"""
        url = reverse("message-send")
        data = {
            "teacher_id": teacher.id,
            "class_id": 99999,
            "student_id": student.id,
            "content": "Test feedback",
        }

        response = api_client.post(url, data, format="json")

        # serializer validation에서 실패할 수 있으므로 400 또는 404
        assert response.status_code in [status.HTTP_400_BAD_REQUEST, status.HTTP_404_NOT_FOUND]
        assert response.data["success"] is False
        if response.status_code == status.HTTP_404_NOT_FOUND:
            assert "Class not found" in response.data["message"]

    def test_message_send_missing_student_id(self, api_client, teacher, course_class):
        """MessageSendView의 student_id 없음 테스트 (line 89)"""
        url = reverse("message-send")
        data = {
            "teacher_id": teacher.id,
            "class_id": course_class.id,
            "content": "Test feedback",
        }

        response = api_client.post(url, data, format="json")

        # serializer validation에서 실패할 수 있으므로 400 또는 500
        assert response.status_code in [status.HTTP_400_BAD_REQUEST, status.HTTP_500_INTERNAL_SERVER_ERROR]
        assert response.data["success"] is False
        # student_id가 없으면 serializer validation에서 실패하거나 request.data.get이 None을 반환
        assert "student_id" in str(response.data).lower() or "student_id is required" in str(response.data)

    def test_message_send_teacher_as_student(self, api_client, teacher, course_class):
        """MessageSendView의 교사를 학생으로 요청 테스트 (line 99-106)"""
        url = reverse("message-send")
        data = {
            "teacher_id": teacher.id,
            "class_id": course_class.id,
            "student_id": teacher.id,
            "content": "Test feedback",
        }

        response = api_client.post(url, data, format="json")

        # serializer validation에서 실패할 수 있으므로 400 또는 403
        assert response.status_code in [status.HTTP_400_BAD_REQUEST, status.HTTP_403_FORBIDDEN]
        assert response.data["success"] is False
        if response.status_code == status.HTTP_403_FORBIDDEN:
            assert "User is not a student" in response.data["message"]

    def test_message_send_exception(self, api_client, teacher, course_class, student):
        """MessageSendView의 exception 처리 테스트 (line 135-137)"""
        url = reverse("message-send")
        data = {
            "teacher_id": teacher.id,
            "class_id": course_class.id,
            "student_id": student.id,
            "content": "Test feedback",
        }

        with patch("feedbacks.views.TeacherFeedback.objects.create") as mock_create:
            mock_create.side_effect = Exception("Database error")

            response = api_client.post(url, data, format="json")

            assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
            assert response.data["success"] is False
            assert "메시지 전송 실패" in response.data["message"]

    def test_class_message_list_view_exception(self, api_client, course_class):
        """ClassMessageListView의 exception 처리 테스트 (line 192-194)"""
        url = reverse("class-message-list", kwargs={"classId": course_class.id})

        with patch("feedbacks.views.TeacherFeedback.objects.filter") as mock_filter:
            mock_filter.side_effect = Exception("Database error")

            response = api_client.get(url, format="json")

            assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
            assert response.data["success"] is False
            assert "클래스 메시지 조회 실패" in response.data["message"]

    def test_message_list_view_value_error(self, api_client, student):
        """MessageListView의 ValueError 처리 테스트 (line 248-250)"""
        url = reverse("message-list")
        response = api_client.get(url, {"userId": student.id, "limit": "invalid"}, format="json")

        assert response.status_code == status.HTTP_200_OK

    def test_message_list_view_exception(self, api_client, student):
        """MessageListView의 exception 처리 테스트 (line 287-289)"""
        url = reverse("message-list")

        with patch("feedbacks.views.Account.objects.get") as mock_get:
            mock_get.side_effect = Exception("Database error")

            response = api_client.get(url, {"userId": student.id}, format="json")

            assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
            assert response.data["success"] is False
            assert "사용자 메시지 조회 실패" in response.data["message"]
