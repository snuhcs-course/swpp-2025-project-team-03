from datetime import timedelta

import pytest
from assignments.models import Assignment
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


class TestAssignmentDetailView:
    """AssignmentDetailView 테스트"""

    def test_update_assignment_with_subject(self, api_client, assignment):
        """과제 수정 시 subject 처리 테스트"""
        new_subject_name = "Science"
        url = reverse("assignment-detail", kwargs={"id": assignment.id})
        data = {
            "title": "Updated Assignment",
            "subject": {"name": new_subject_name},
        }

        response = api_client.put(url, data, format="json")

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True

        # Subject가 생성되고 할당되었는지 확인
        assignment.refresh_from_db()
        assert assignment.subject.name == new_subject_name

        # 새로운 Subject가 생성되었는지 확인
        new_subject = Subject.objects.get(name=new_subject_name)
        assert assignment.subject == new_subject

    def test_update_assignment_with_existing_subject(self, api_client, assignment, subject):
        """기존 Subject로 과제 수정 테스트"""
        url = reverse("assignment-detail", kwargs={"id": assignment.id})
        data = {
            "title": "Updated Assignment",
            "subject": {"name": subject.name},
        }

        response = api_client.put(url, data, format="json")

        assert response.status_code == status.HTTP_200_OK

        # 기존 Subject가 사용되었는지 확인
        assignment.refresh_from_db()
        assert assignment.subject == subject

    def test_update_assignment_without_subject(self, api_client, assignment):
        """subject 없이 과제 수정 테스트"""
        original_subject = assignment.subject
        url = reverse("assignment-detail", kwargs={"id": assignment.id})
        data = {
            "title": "Updated Assignment",
        }

        response = api_client.put(url, data, format="json")

        assert response.status_code == status.HTTP_200_OK

        # Subject가 변경되지 않았는지 확인
        assignment.refresh_from_db()
        assert assignment.subject == original_subject
