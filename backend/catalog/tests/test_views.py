import pytest
from catalog.models import Subject
from django.urls import reverse
from rest_framework import status
from rest_framework.test import APIClient

pytestmark = pytest.mark.django_db


@pytest.fixture
def api_client():
    return APIClient()


class TestSubjectListView:
    """SubjectListView 테스트"""

    def test_get_subject_list_success(self, api_client):
        """과목 목록 조회 성공 테스트"""
        subject1 = Subject.objects.create(name="Mathematics")
        subject2 = Subject.objects.create(name="Science")
        subject3 = Subject.objects.create(name="English")

        url = reverse("catalog:subject-list")
        response = api_client.get(url, format="json")

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert response.data["message"] == "과목 목록 조회 성공"
        assert len(response.data["data"]) == 3
        assert response.data["data"][0]["id"] == subject1.id
        assert response.data["data"][0]["name"] == "Mathematics"
        assert response.data["data"][1]["id"] == subject2.id
        assert response.data["data"][1]["name"] == "Science"
        assert response.data["data"][2]["id"] == subject3.id
        assert response.data["data"][2]["name"] == "English"

    def test_get_subject_list_empty(self, api_client):
        """과목이 없는 경우 테스트"""
        url = reverse("catalog:subject-list")
        response = api_client.get(url, format="json")

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert response.data["message"] == "과목 목록 조회 성공"
        assert len(response.data["data"]) == 0

    def test_get_subject_list_exception(self, api_client):
        """예외 발생 시 테스트"""
        from unittest.mock import patch

        url = reverse("catalog:subject-list")
        with patch("catalog.views.Subject.objects.all") as mock_all:
            mock_all.side_effect = Exception("DB Error")
            response = api_client.get(url, format="json")

            assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
            assert response.data["success"] is False
            assert "DB Error" in response.data["error"]
            assert "과목 목록 조회 중 오류가 발생했습니다" in response.data["message"]
