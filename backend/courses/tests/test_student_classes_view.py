from unittest.mock import patch

import pytest
from courses.models import Enrollment
from django.contrib.auth import get_user_model
from django.urls import reverse
from rest_framework import status
from rest_framework.test import APIClient

from .test_courseclass_factories import CourseClassFactory, EnrollmentFactory, StudentFactory, TeacherFactory

Account = get_user_model()

# Allow DB access for all tests in this module
pytestmark = pytest.mark.django_db


@pytest.fixture
def api_client():
    return APIClient()


@pytest.fixture
def teacher():
    return TeacherFactory()


@pytest.fixture
def student():
    return StudentFactory()


class TestStudentClassesView:
    """학생이 수강 중인 클래스 목록 조회 API 테스트 (/courses/students/{id}/classes/)"""

    def test_returns_enrolled_classes(self, api_client, student, teacher):
        # Given: 학생이 ENROLLED 상태로 2개 반에 등록됨
        class1 = CourseClassFactory(teacher=teacher)
        class2 = CourseClassFactory(teacher=teacher)
        EnrollmentFactory(student=student, course_class=class1, status=Enrollment.Status.ENROLLED)
        EnrollmentFactory(student=student, course_class=class2, status=Enrollment.Status.ENROLLED)
        # And: DROPPED 상태 반은 제외되어야 함
        dropped_class = CourseClassFactory(teacher=teacher)
        EnrollmentFactory(student=student, course_class=dropped_class, status=Enrollment.Status.DROPPED)

        # When
        url = reverse("student-classes", kwargs={"id": student.id})
        resp = api_client.get(url)

        # Then
        assert resp.status_code == status.HTTP_200_OK
        assert resp.data["success"] is True
        data = resp.data["data"]
        assert isinstance(data, list)
        assert len(data) == 2
        returned_ids = {c["id"] for c in data}
        assert {class1.id, class2.id} == returned_ids
        # 필수 필드 몇 가지 스냅샷
        assert {"id", "name", "subject", "teacher_name", "student_count"}.issubset(set(data[0].keys()))

    def test_returns_empty_when_no_enrollments(self, api_client, student):
        # When
        url = reverse("student-classes", kwargs={"id": student.id})
        resp = api_client.get(url)

        # Then
        assert resp.status_code == status.HTTP_200_OK
        assert resp.data["success"] is True
        assert resp.data["data"] == []

    def test_excludes_non_enrolled_statuses(self, api_client, student, teacher):
        # Given: DROPPED/COMPLETED 만 존재
        class1 = CourseClassFactory(teacher=teacher)
        class2 = CourseClassFactory(teacher=teacher)
        EnrollmentFactory(student=student, course_class=class1, status=Enrollment.Status.DROPPED)
        EnrollmentFactory(student=student, course_class=class2, status=Enrollment.Status.COMPLETED)

        # When
        url = reverse("student-classes", kwargs={"id": student.id})
        resp = api_client.get(url)

        # Then
        assert resp.status_code == status.HTTP_200_OK
        assert resp.data["success"] is True
        assert resp.data["data"] == []

    def test_student_not_found(self, api_client):
        # When
        url = reverse("student-classes", kwargs={"id": 999999})
        resp = api_client.get(url)

        # Then
        assert resp.status_code == status.HTTP_404_NOT_FOUND
        assert resp.data["success"] is False
        assert "Student not found" in resp.data["error"]

    def test_teacher_id_returns_404(self, api_client, teacher):
        # When: 학생이 아닌 id로 호출하면 404
        url = reverse("student-classes", kwargs={"id": teacher.id})
        resp = api_client.get(url)

        # Then
        assert resp.status_code == status.HTTP_404_NOT_FOUND
        assert resp.data["success"] is False

    def test_unexpected_exception_returns_500(self, api_client, student):
        url = reverse("student-classes", kwargs={"id": student.id})
        # Patch filter to raise exception inside view
        with patch("courses.views.Enrollment.objects.filter") as mock_filter:
            mock_filter.side_effect = Exception("DB error")
            resp = api_client.get(url)

        assert resp.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
        assert resp.data["success"] is False
        assert "학생 클래스 목록 조회 중 오류가 발생했습니다" in resp.data["message"]
