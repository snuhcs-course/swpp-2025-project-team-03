from datetime import timedelta
from unittest.mock import patch

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


class TestAssignmentListView:
    """AssignmentListView 테스트"""

    def test_list_assignments_all(self, api_client, course_class, subject):
        """모든 과제 조회 테스트"""
        # 현재 시간 기준으로 과제 생성
        now = timezone.now()
        in_progress = Assignment.objects.create(
            course_class=course_class,
            title="In Progress Assignment",
            subject=subject,
            visible_from=now,
            due_at=now + timedelta(days=7),
            total_questions=10,
        )
        completed = Assignment.objects.create(
            course_class=course_class,
            title="Completed Assignment",
            subject=subject,
            visible_from=now - timedelta(days=2),
            due_at=now - timedelta(days=1),
            total_questions=10,
        )

        url = reverse("assignment-list")
        response = api_client.get(url, format="json")

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert len(response.data["data"]) >= 2

        assignment_ids = [a["id"] for a in response.data["data"]]
        assert in_progress.id in assignment_ids
        assert completed.id in assignment_ids

    def test_list_assignments_filter_in_progress(self, api_client, course_class, subject):
        """진행중인 과제만 필터링 테스트"""
        now = timezone.now()
        in_progress = Assignment.objects.create(
            course_class=course_class,
            title="In Progress Assignment",
            subject=subject,
            visible_from=now,
            due_at=now + timedelta(days=7),
            total_questions=10,
        )
        completed = Assignment.objects.create(
            course_class=course_class,
            title="Completed Assignment",
            subject=subject,
            visible_from=now - timedelta(days=2),
            due_at=now - timedelta(days=1),
            total_questions=10,
        )

        url = reverse("assignment-list")
        response = api_client.get(url, {"status": "IN_PROGRESS"}, format="json")

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True

        assignment_ids = [a["id"] for a in response.data["data"]]
        assert in_progress.id in assignment_ids
        assert completed.id not in assignment_ids

    def test_list_assignments_filter_completed(self, api_client, course_class, subject):
        """완료된 과제만 필터링 테스트"""
        now = timezone.now()
        in_progress = Assignment.objects.create(
            course_class=course_class,
            title="In Progress Assignment",
            subject=subject,
            visible_from=now,
            due_at=now + timedelta(days=7),
            total_questions=10,
        )
        completed = Assignment.objects.create(
            course_class=course_class,
            title="Completed Assignment",
            subject=subject,
            visible_from=now - timedelta(days=2),
            due_at=now - timedelta(days=1),
            total_questions=10,
        )

        url = reverse("assignment-list")
        response = api_client.get(url, {"status": "COMPLETED"}, format="json")

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True

        assignment_ids = [a["id"] for a in response.data["data"]]
        assert in_progress.id not in assignment_ids
        assert completed.id in assignment_ids

    def test_list_assignments_filter_by_teacher(self, api_client, course_class, subject):
        """선생님으로 필터링 테스트"""
        other_teacher = Account.objects.create_user(email="other@test.com", password="testpass123", is_student=False)
        other_class = CourseClass.objects.create(
            teacher=other_teacher,
            subject=course_class.subject,
            name="Other Class",
            start_date=timezone.now(),
            end_date=timezone.now() + timedelta(days=90),
        )

        assignment1 = Assignment.objects.create(
            course_class=course_class,
            title="Teacher Assignment",
            subject=subject,
            visible_from=timezone.now(),
            due_at=timezone.now() + timedelta(days=7),
            total_questions=10,
        )
        assignment2 = Assignment.objects.create(
            course_class=other_class,
            title="Other Teacher Assignment",
            subject=subject,
            visible_from=timezone.now(),
            due_at=timezone.now() + timedelta(days=7),
            total_questions=10,
        )

        url = reverse("assignment-list")
        response = api_client.get(url, {"teacherId": course_class.teacher.id}, format="json")

        assert response.status_code == status.HTTP_200_OK
        assignment_ids = [a["id"] for a in response.data["data"]]
        assert assignment1.id in assignment_ids
        assert assignment2.id not in assignment_ids

    def test_list_assignments_filter_by_class(self, api_client, course_class, subject):
        """클래스로 필터링 테스트"""
        other_class = CourseClass.objects.create(
            teacher=course_class.teacher,
            subject=course_class.subject,
            name="Other Class",
            start_date=timezone.now(),
            end_date=timezone.now() + timedelta(days=90),
        )

        assignment1 = Assignment.objects.create(
            course_class=course_class,
            title="Class Assignment",
            subject=subject,
            visible_from=timezone.now(),
            due_at=timezone.now() + timedelta(days=7),
            total_questions=10,
        )
        assignment2 = Assignment.objects.create(
            course_class=other_class,
            title="Other Class Assignment",
            subject=subject,
            visible_from=timezone.now(),
            due_at=timezone.now() + timedelta(days=7),
            total_questions=10,
        )

        url = reverse("assignment-list")
        response = api_client.get(url, {"classId": course_class.id}, format="json")

        assert response.status_code == status.HTTP_200_OK
        assignment_ids = [a["id"] for a in response.data["data"]]
        assert assignment1.id in assignment_ids
        assert assignment2.id not in assignment_ids

    def test_list_assignments_multiple_filters(self, api_client, course_class, subject):
        """여러 필터 조합 테스트"""
        now = timezone.now()
        assignment = Assignment.objects.create(
            course_class=course_class,
            title="Filtered Assignment",
            subject=subject,
            visible_from=now,
            due_at=now + timedelta(days=7),
            total_questions=10,
        )

        url = reverse("assignment-list")
        response = api_client.get(
            url,
            {"teacherId": course_class.teacher.id, "classId": course_class.id, "status": "IN_PROGRESS"},
            format="json",
        )

        assert response.status_code == status.HTTP_200_OK
        assignment_ids = [a["id"] for a in response.data["data"]]
        assert assignment.id in assignment_ids

    def test_list_assignments_exception_handling(self, api_client):
        """예외 처리 테스트"""
        url = reverse("assignment-list")
        # DB 연결 문제 등을 시뮬레이션하기 위해 Mock 사용
        with patch("assignments.views.Assignment.objects.select_related") as mock_assignments:
            mock_assignments.side_effect = Exception("Database error")

            response = api_client.get(url, format="json")

            assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
            assert response.data["success"] is False
            assert "과제 목록 조회 중 오류가 발생했습니다" in response.data["message"]
