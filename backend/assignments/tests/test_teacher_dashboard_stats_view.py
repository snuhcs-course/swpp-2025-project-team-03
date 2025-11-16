from datetime import timedelta

import pytest
from assignments.models import Assignment
from catalog.models import Subject
from courses.models import CourseClass, Enrollment
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


class TestTeacherDashboardStatsView:
    """TeacherDashboardStatsView 테스트"""

    def test_get_dashboard_stats_success(self, api_client, teacher, course_class, student):
        """대시보드 통계 조회 성공 테스트"""

        Assignment.objects.create(
            course_class=course_class,
            subject=course_class.subject,
            title="Test Assignment",
            due_at=timezone.now() + timedelta(days=7),
        )
        Enrollment.objects.create(course_class=course_class, student=student, status=Enrollment.Status.ENROLLED)

        url = reverse("teacher-dashboard-stats")
        response = api_client.get(url, {"teacherId": teacher.id}, format="json")

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert response.data["message"] == "대시보드 통계 조회 성공"
        assert response.data["data"]["total_assignments"] == 1
        assert response.data["data"]["total_students"] == 1
        assert response.data["data"]["total_classes"] == 1

    def test_get_dashboard_stats_missing_teacher_id(self, api_client):
        """teacherId 파라미터 없이 요청 테스트"""
        url = reverse("teacher-dashboard-stats")
        response = api_client.get(url, format="json")

        assert response.status_code == status.HTTP_400_BAD_REQUEST
        assert response.data["success"] is False
        assert "teacherId 파라미터가 필요합니다" in response.data["message"]

    def test_get_dashboard_stats_teacher_not_found(self, api_client):
        """존재하지 않는 교사 ID로 요청 테스트"""
        url = reverse("teacher-dashboard-stats")
        response = api_client.get(url, {"teacherId": 99999}, format="json")

        assert response.status_code == status.HTTP_404_NOT_FOUND
        assert response.data["success"] is False
        assert "해당 교사를 찾을 수 없습니다" in response.data["message"]

    def test_get_dashboard_stats_student_as_teacher(self, api_client, student):
        """학생 계정을 교사로 요청 테스트"""
        url = reverse("teacher-dashboard-stats")
        response = api_client.get(url, {"teacherId": student.id}, format="json")

        assert response.status_code == status.HTTP_404_NOT_FOUND
        assert response.data["success"] is False
        assert "해당 교사를 찾을 수 없습니다" in response.data["message"]

    def test_get_dashboard_stats_exception(self, api_client, teacher):
        """통계 조회 중 예외 발생 테스트"""
        from unittest.mock import patch

        url = reverse("teacher-dashboard-stats")
        with patch("assignments.views.Account.objects.get") as mock_get:
            mock_get.side_effect = Exception("DB Error")
            response = api_client.get(url, {"teacherId": teacher.id}, format="json")

            assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
            assert response.data["success"] is False
            assert "통계 조회 중 오류가 발생했습니다" in response.data["message"]
            assert "error" in response.data
