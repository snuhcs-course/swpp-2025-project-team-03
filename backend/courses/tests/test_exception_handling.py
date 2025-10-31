from unittest.mock import patch

import pytest
from django.contrib.auth import get_user_model
from django.urls import reverse
from rest_framework import status
from rest_framework.test import APIClient

Account = get_user_model()

pytestmark = pytest.mark.django_db


@pytest.fixture
def api_client():
    return APIClient()


class TestCoursesExceptionHandling:
    """Courses views의 exception 처리 테스트"""

    def test_student_list_view_exception(self, api_client):
        """StudentListView의 exception 처리 테스트"""
        url = reverse("student-list")
        # DB 연결 문제 등을 시뮬레이션
        with patch("courses.views.Account.objects.filter") as mock_filter:
            mock_filter.side_effect = Exception("Database error")

            response = api_client.get(url, format="json")

            assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
            assert response.data["success"] is False
            assert "학생 목록 조회 중 오류가 발생했습니다" in response.data["message"]

    def test_student_detail_view_exception(self, api_client):
        """StudentDetailView의 exception 처리 테스트"""
        url = reverse("student-detail", kwargs={"id": 1})
        # DB 연결 문제 등을 시뮬레이션
        with patch("courses.views.Account.objects.get") as mock_get:
            mock_get.side_effect = Exception("Database error")

            response = api_client.get(url, format="json")

            assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
            assert response.data["success"] is False
            assert "학생 상세 조회 중 오류가 발생했습니다" in response.data["message"]
