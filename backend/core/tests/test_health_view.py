import pytest
from django.urls import reverse
from rest_framework import status
from rest_framework.test import APIClient

pytestmark = pytest.mark.django_db


@pytest.fixture
def api_client():
    return APIClient()


class TestHealthView:
    """HealthView 테스트"""

    def test_get_health_check_success(self, api_client):
        """Health check 엔드포인트 성공 테스트"""
        url = reverse("core:health")
        response = api_client.get(url, format="json")

        assert response.status_code == status.HTTP_200_OK
        assert response.data["message"] == "Hello, World!"
