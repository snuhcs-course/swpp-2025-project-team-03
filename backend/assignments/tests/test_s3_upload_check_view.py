from datetime import timedelta
from unittest.mock import Mock, patch

import pytest
from assignments.models import Assignment, Material
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
def assignment(course_class, subject):
    return Assignment.objects.create(
        course_class=course_class,
        subject=subject,
        title="Test Assignment",
        description="Test Description",
        due_at=timezone.now() + timedelta(days=7),
    )


@pytest.fixture
def pdf_material(assignment):
    return Material.objects.create(
        assignment=assignment,
        kind=Material.Kind.PDF,
        s3_key="test-assignment-1/test.pdf",
        bytes=1024,
    )


class TestS3UploadCheckView:
    """S3UploadCheckView 테스트"""

    @patch("assignments.views.boto3.client")
    @patch("assignments.views.settings")
    def test_s3_check_file_exists(self, mock_settings, mock_boto3, api_client, assignment, pdf_material):
        """S3에 파일이 존재하는 경우 테스트"""
        # Mock settings
        mock_settings.AWS_ACCESS_KEY_ID = "test-key"
        mock_settings.AWS_SECRET_ACCESS_KEY = "test-secret"
        mock_settings.AWS_REGION = "us-east-1"
        mock_settings.AWS_STORAGE_BUCKET_NAME = "test-bucket"

        # Mock S3 client
        mock_s3_client = Mock()
        mock_response = {
            "ContentLength": 2048,
            "ContentType": "application/pdf",
            "LastModified": timezone.now(),
        }
        mock_s3_client.head_object.return_value = mock_response
        mock_boto3.return_value = mock_s3_client

        url = reverse("s3-upload-check", kwargs={"assignment_id": assignment.id})
        response = api_client.get(url, format="json")

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert response.data["data"]["file_exists"] is True
        assert response.data["data"]["file_size"] == 2048
        assert response.data["data"]["content_type"] == "application/pdf"
        assert response.data["data"]["assignment_id"] == assignment.id
        assert response.data["data"]["material_id"] == pdf_material.id

        # S3 client 호출 확인
        mock_s3_client.head_object.assert_called_once_with(Bucket="test-bucket", Key=pdf_material.s3_key)

    @patch("assignments.views.boto3.client")
    @patch("assignments.views.settings")
    def test_s3_check_file_not_exists(self, mock_settings, mock_boto3, api_client, assignment, pdf_material):
        """S3에 파일이 존재하지 않는 경우 테스트"""
        # Mock settings
        mock_settings.AWS_ACCESS_KEY_ID = "test-key"
        mock_settings.AWS_SECRET_ACCESS_KEY = "test-secret"
        mock_settings.AWS_REGION = "us-east-1"
        mock_settings.AWS_STORAGE_BUCKET_NAME = "test-bucket"

        # Mock S3 client - NoSuchKey 예외 발생
        mock_s3_client = Mock()
        mock_s3_client.exceptions.NoSuchKey = type("NoSuchKey", (Exception,), {})
        mock_s3_client.head_object.side_effect = mock_s3_client.exceptions.NoSuchKey()
        mock_boto3.return_value = mock_s3_client

        url = reverse("s3-upload-check", kwargs={"assignment_id": assignment.id})
        response = api_client.get(url, format="json")

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert response.data["data"]["file_exists"] is False
        assert response.data["data"]["assignment_id"] == assignment.id
        assert response.data["data"]["material_id"] == pdf_material.id

    def test_s3_check_assignment_not_found(self, api_client):
        """과제가 존재하지 않는 경우 테스트"""
        url = reverse("s3-upload-check", kwargs={"assignment_id": 99999})
        response = api_client.get(url, format="json")

        assert response.status_code == status.HTTP_404_NOT_FOUND
        assert response.data["success"] is False
        assert "과제를 찾을 수 없습니다" in response.data["message"]

    def test_s3_check_no_pdf_material(self, api_client, assignment):
        """PDF Material이 없는 경우 테스트"""
        # 다른 종류의 Material 생성 (PDF가 아님)
        Material.objects.create(
            assignment=assignment,
            kind=Material.Kind.TEXT,
            summary="Some text content",
            s3_key="dummy-key",
            bytes=100,
        )

        url = reverse("s3-upload-check", kwargs={"assignment_id": assignment.id})
        response = api_client.get(url, format="json")

        assert response.status_code == status.HTTP_404_NOT_FOUND
        assert response.data["success"] is False
        assert "PDF 자료가 없습니다" in response.data["message"]

    @patch("assignments.views.boto3.client")
    @patch("assignments.views.settings")
    def test_s3_check_exception_handling(self, mock_settings, mock_boto3, api_client, assignment, pdf_material):
        """S3 확인 중 예외 발생 테스트"""
        # Mock settings
        mock_settings.AWS_ACCESS_KEY_ID = "test-key"
        mock_settings.AWS_SECRET_ACCESS_KEY = "test-secret"
        mock_settings.AWS_REGION = "us-east-1"
        mock_settings.AWS_STORAGE_BUCKET_NAME = "test-bucket"

        # Mock S3 client - 일반 예외 발생
        mock_s3_client = Mock()
        mock_s3_client.head_object.side_effect = Exception("S3 connection error")
        mock_boto3.return_value = mock_s3_client

        url = reverse("s3-upload-check", kwargs={"assignment_id": assignment.id})
        response = api_client.get(url, format="json")

        assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
        assert response.data["success"] is False
        assert "S3 확인 중 오류가 발생했습니다" in response.data["message"]
