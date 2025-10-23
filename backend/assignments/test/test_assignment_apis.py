from datetime import timedelta
from unittest.mock import Mock, patch

import pytest
from assignments.models import Assignment, Material
from catalog.models import Subject
from courses.models import CourseClass, Enrollment
from django.contrib.auth import get_user_model
from django.urls import reverse
from django.utils import timezone
from rest_framework import status
from rest_framework.test import APIClient
from submissions.models import PersonalAssignment

Account = get_user_model()

# 모든 테스트에서 DB 접근 허용
pytestmark = pytest.mark.django_db

# pytest 실행 예시
# pytest assignments/test/test_assignment_apis.py -v


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
    return Subject.objects.create(name="Mathematics", code="MATH101")


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
    return Enrollment.objects.create(student=student, course_class=course_class, status=Enrollment.Status.ENROLLED)


@pytest.fixture
def assignment(course_class):
    return Assignment.objects.create(
        course_class=course_class,
        title="Test Assignment",
        description="Test Description",
        visible_from=timezone.now(),
        due_at=timezone.now() + timedelta(days=7),
    )


class TestAssignmentCreateView:
    """과제 생성 API 테스트"""

    @patch("assignments.views.boto3.client")
    def test_create_assignment_success(self, mock_boto3, api_client, course_class, student, enrollment):
        """과제 생성 성공 테스트"""
        # Mock S3 presigned URL
        mock_s3 = Mock()
        mock_s3.generate_presigned_url.return_value = "https://s3.amazonaws.com/presigned-url"
        mock_boto3.return_value = mock_s3

        url = reverse("assignment-create")
        data = {
            "class_id": course_class.id,
            "title": "Midterm Exam",
            "due_at": (timezone.now() + timedelta(days=7)).isoformat(),
            "description": "Midterm exam assignment",
        }

        response = api_client.post(url, data, format="json")

        assert response.status_code == status.HTTP_201_CREATED
        assert "assignment_id" in response.data
        assert "material_id" in response.data
        assert "s3_key" in response.data
        assert "upload_url" in response.data

        # DB 확인
        assignment = Assignment.objects.get(id=response.data["assignment_id"])
        assert assignment.title == "Midterm Exam"
        assert assignment.course_class == course_class

        # Material 확인
        material = Material.objects.get(id=response.data["material_id"])
        assert material.assignment == assignment
        assert material.kind == Material.Kind.PDF

        # PersonalAssignment 확인 (등록된 학생에게 생성되어야 함)
        personal_assignment = PersonalAssignment.objects.filter(assignment=assignment, student=student)
        assert personal_assignment.exists()
        assert personal_assignment.first().status == PersonalAssignment.Status.NOT_STARTED

    @patch("assignments.views.boto3.client")
    def test_create_assignment_multiple_students(self, mock_boto3, api_client, course_class):
        """여러 학생이 등록된 클래스에 과제 생성 테스트"""
        # Mock S3
        mock_s3 = Mock()
        mock_s3.generate_presigned_url.return_value = "https://s3.amazonaws.com/presigned-url"
        mock_boto3.return_value = mock_s3

        # 3명의 학생 등록
        students = []
        for i in range(3):
            student = Account.objects.create_user(email=f"student{i}@test.com", password="testpass123", is_student=True)
            Enrollment.objects.create(student=student, course_class=course_class, status=Enrollment.Status.ENROLLED)
            students.append(student)

        url = reverse("assignment-create")
        data = {
            "class_id": course_class.id,
            "title": "Group Assignment",
            "due_at": (timezone.now() + timedelta(days=7)).isoformat(),
        }

        response = api_client.post(url, data, format="json")

        assert response.status_code == status.HTTP_201_CREATED

        # 3명 모두에게 PersonalAssignment 생성되었는지 확인
        assignment = Assignment.objects.get(id=response.data["assignment_id"])
        personal_assignments = PersonalAssignment.objects.filter(assignment=assignment)
        assert personal_assignments.count() == 3

        for student in students:
            assert personal_assignments.filter(student=student).exists()

    @patch("assignments.views.boto3.client")
    def test_create_assignment_no_enrolled_students(self, mock_boto3, api_client, course_class):
        """등록된 학생이 없는 클래스에 과제 생성 테스트"""
        # Mock S3
        mock_s3 = Mock()
        mock_s3.generate_presigned_url.return_value = "https://s3.amazonaws.com/presigned-url"
        mock_boto3.return_value = mock_s3

        url = reverse("assignment-create")
        data = {
            "class_id": course_class.id,
            "title": "Assignment for Empty Class",
            "due_at": (timezone.now() + timedelta(days=7)).isoformat(),
        }

        response = api_client.post(url, data, format="json")

        assert response.status_code == status.HTTP_201_CREATED

        # PersonalAssignment가 생성되지 않아야 함
        assignment = Assignment.objects.get(id=response.data["assignment_id"])
        assert PersonalAssignment.objects.filter(assignment=assignment).count() == 0

    @patch("assignments.views.boto3.client")
    def test_create_assignment_only_enrolled_students(self, mock_boto3, api_client, course_class):
        """ENROLLED 상태 학생만 PersonalAssignment 생성 테스트"""
        # Mock S3
        mock_s3 = Mock()
        mock_s3.generate_presigned_url.return_value = "https://s3.amazonaws.com/presigned-url"
        mock_boto3.return_value = mock_s3

        # ENROLLED 학생
        enrolled_student = Account.objects.create_user(
            email="enrolled@test.com", password="testpass123", is_student=True
        )
        Enrollment.objects.create(
            student=enrolled_student, course_class=course_class, status=Enrollment.Status.ENROLLED
        )

        # DROPPED 학생
        dropped_student = Account.objects.create_user(email="dropped@test.com", password="testpass123", is_student=True)
        Enrollment.objects.create(student=dropped_student, course_class=course_class, status=Enrollment.Status.DROPPED)

        url = reverse("assignment-create")
        data = {
            "class_id": course_class.id,
            "title": "Test Assignment",
            "due_at": (timezone.now() + timedelta(days=7)).isoformat(),
        }

        response = api_client.post(url, data, format="json")

        assert response.status_code == status.HTTP_201_CREATED

        # ENROLLED 학생만 PersonalAssignment 생성
        assignment = Assignment.objects.get(id=response.data["assignment_id"])
        personal_assignments = PersonalAssignment.objects.filter(assignment=assignment)
        assert personal_assignments.count() == 1
        assert personal_assignments.first().student == enrolled_student

    def test_create_assignment_invalid_class_id(self, api_client):
        """존재하지 않는 class_id로 과제 생성 테스트"""
        url = reverse("assignment-create")
        data = {
            "class_id": 99999,
            "title": "Invalid Assignment",
            "due_at": (timezone.now() + timedelta(days=7)).isoformat(),
        }

        response = api_client.post(url, data, format="json")

        assert response.status_code == status.HTTP_400_BAD_REQUEST
        assert response.data["success"] is False
        assert "Invalid class_id" in response.data["error"]

    @patch("assignments.views.boto3.client")
    def test_create_assignment_invalid_due_at(self, mock_boto3, api_client, course_class):
        """잘못된 due_at 형식으로 과제 생성 테스트"""
        url = reverse("assignment-create")
        data = {
            "class_id": course_class.id,
            "title": "Invalid Date Assignment",
            "due_at": "invalid-date-format",
        }

        response = api_client.post(url, data, format="json")

        assert response.status_code == status.HTTP_400_BAD_REQUEST
        assert response.data["success"] is False
        assert "Invalid due_at format" in response.data["error"]

    @patch("assignments.views.boto3.client")
    def test_create_assignment_missing_required_fields(self, mock_boto3, api_client):
        """필수 필드 누락 시 과제 생성 테스트"""
        url = reverse("assignment-create")
        data = {
            "title": "Incomplete Assignment",
            # class_id, due_at 누락
        }

        response = api_client.post(url, data, format="json")

        assert response.status_code == status.HTTP_400_BAD_REQUEST

    @patch("assignments.views.boto3.client")
    def test_create_assignment_s3_failure(self, mock_boto3, api_client, course_class):
        """S3 presigned URL 생성 실패 시 테스트"""
        # Mock S3 에러
        mock_s3 = Mock()
        mock_s3.generate_presigned_url.side_effect = Exception("S3 Error")
        mock_boto3.return_value = mock_s3

        url = reverse("assignment-create")
        data = {
            "class_id": course_class.id,
            "title": "S3 Failed Assignment",
            "due_at": (timezone.now() + timedelta(days=7)).isoformat(),
        }

        response = api_client.post(url, data, format="json")

        assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
        assert "Failed to generate presigned URL" in response.data["error"]

        # Assignment가 rollback 되었는지 확인
        assert Assignment.objects.filter(title="S3 Failed Assignment").count() == 0

    @patch("assignments.views.boto3.client")
    def test_create_assignment_with_description(self, mock_boto3, api_client, course_class):
        """설명이 포함된 과제 생성 테스트"""
        # Mock S3
        mock_s3 = Mock()
        mock_s3.generate_presigned_url.return_value = "https://s3.amazonaws.com/presigned-url"
        mock_boto3.return_value = mock_s3

        url = reverse("assignment-create")
        description = "This is a detailed assignment description with special characters: 한글, @#$%"
        data = {
            "class_id": course_class.id,
            "title": "Assignment with Description",
            "due_at": (timezone.now() + timedelta(days=7)).isoformat(),
            "description": description,
        }

        response = api_client.post(url, data, format="json")

        assert response.status_code == status.HTTP_201_CREATED

        assignment = Assignment.objects.get(id=response.data["assignment_id"])
        assert assignment.description == description

    @patch("assignments.views.boto3.client")
    def test_create_assignment_response_format(self, mock_boto3, api_client, course_class):
        """응답 형식 검증 테스트"""
        # Mock S3
        mock_s3 = Mock()
        mock_s3.generate_presigned_url.return_value = "https://s3.amazonaws.com/presigned-url"
        mock_boto3.return_value = mock_s3

        url = reverse("assignment-create")
        data = {
            "class_id": course_class.id,
            "title": "Response Format Test",
            "due_at": (timezone.now() + timedelta(days=7)).isoformat(),
        }

        response = api_client.post(url, data, format="json")

        assert response.status_code == status.HTTP_201_CREATED

        # 응답 필드 확인
        assert "assignment_id" in response.data
        assert "material_id" in response.data
        assert "s3_key" in response.data
        assert "upload_url" in response.data

        # 타입 확인
        assert isinstance(response.data["assignment_id"], int)
        assert isinstance(response.data["material_id"], int)
        assert isinstance(response.data["s3_key"], str)
        assert isinstance(response.data["upload_url"], str)
        assert response.data["upload_url"].startswith("https://")
