"""
PersonalAssignmentCompleteView unit tests
- POST /api/personal_assignments/{id}/complete/: 개인 과제 완료
"""

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
from submissions.models import PersonalAssignment

Account = get_user_model()

pytestmark = pytest.mark.django_db


@pytest.fixture
def api_client():
    return APIClient()


@pytest.fixture
def teacher():
    return Account.objects.create_user(
        email="teacher@test.com", password="testpass123", display_name="Teacher", is_student=False
    )


@pytest.fixture
def student():
    return Account.objects.create_user(
        email="student@test.com", password="testpass123", display_name="Student", is_student=True
    )


@pytest.fixture
def subject():
    return Subject.objects.create(name="Math")


@pytest.fixture
def course_class(teacher, subject):
    return CourseClass.objects.create(
        teacher=teacher,
        subject=subject,
        name="Algebra 1",
        description="Desc",
        start_date=timezone.now(),
        end_date=timezone.now() + timedelta(days=30),
    )


@pytest.fixture
def assignment(course_class, subject):
    return Assignment.objects.create(
        course_class=course_class,
        subject=subject,
        title="HW 1",
        description="",
        total_questions=3,
        due_at=timezone.now() + timedelta(days=7),
        grade="",
    )


@pytest.fixture
def personal_assignment_not_started(student, assignment):
    """NOT_STARTED 상태의 PersonalAssignment"""
    return PersonalAssignment.objects.create(
        student=student,
        assignment=assignment,
        status=PersonalAssignment.Status.NOT_STARTED,
        solved_num=0,
    )


@pytest.fixture
def personal_assignment_in_progress(student, assignment):
    """IN_PROGRESS 상태의 PersonalAssignment"""
    return PersonalAssignment.objects.create(
        student=student,
        assignment=assignment,
        status=PersonalAssignment.Status.IN_PROGRESS,
        solved_num=1,
        started_at=timezone.now(),
    )


@pytest.fixture
def personal_assignment_submitted(student, assignment):
    """이미 SUBMITTED 상태인 PersonalAssignment"""
    return PersonalAssignment.objects.create(
        student=student,
        assignment=assignment,
        status=PersonalAssignment.Status.SUBMITTED,
        solved_num=3,
        started_at=timezone.now() - timedelta(hours=1),
        submitted_at=timezone.now(),
    )


class TestPersonalAssignmentCompleteView:
    """PersonalAssignmentCompleteView API 테스트"""

    def test_not_found_personal_assignment(self, api_client):
        """존재하지 않는 PersonalAssignment ID로 완료 요청 시 404 에러"""
        url = reverse("personal-assignment-complete", kwargs={"id": 999999})
        resp = api_client.post(url)
        assert resp.status_code == status.HTTP_404_NOT_FOUND
        assert resp.data["success"] is False
        assert "찾을 수 없습니다" in resp.data["message"]

    def test_complete_not_started_assignment(self, api_client, personal_assignment_not_started):
        """NOT_STARTED 상태의 과제 완료"""
        url = reverse("personal-assignment-complete", kwargs={"id": personal_assignment_not_started.id})
        resp = api_client.post(url)

        assert resp.status_code == status.HTTP_200_OK
        assert resp.data["success"] is True
        assert resp.data["message"] == "과제가 성공적으로 완료되었습니다."

        # DB에서 상태 확인
        personal_assignment_not_started.refresh_from_db()
        assert personal_assignment_not_started.status == PersonalAssignment.Status.SUBMITTED
        assert personal_assignment_not_started.submitted_at is not None

    def test_complete_in_progress_assignment(self, api_client, personal_assignment_in_progress):
        """IN_PROGRESS 상태의 과제 완료"""
        url = reverse("personal-assignment-complete", kwargs={"id": personal_assignment_in_progress.id})
        resp = api_client.post(url)

        assert resp.status_code == status.HTTP_200_OK
        assert resp.data["success"] is True

        # DB에서 상태 확인
        personal_assignment_in_progress.refresh_from_db()
        assert personal_assignment_in_progress.status == PersonalAssignment.Status.SUBMITTED
        assert personal_assignment_in_progress.submitted_at is not None

    def test_complete_already_submitted_assignment(self, api_client, personal_assignment_submitted):
        """이미 SUBMITTED 상태인 과제를 다시 완료 요청"""
        original_submitted_at = personal_assignment_submitted.submitted_at

        url = reverse("personal-assignment-complete", kwargs={"id": personal_assignment_submitted.id})
        resp = api_client.post(url)

        assert resp.status_code == status.HTTP_200_OK
        assert resp.data["success"] is True

        # submitted_at이 업데이트됨
        personal_assignment_submitted.refresh_from_db()
        assert personal_assignment_submitted.status == PersonalAssignment.Status.SUBMITTED
        assert personal_assignment_submitted.submitted_at > original_submitted_at

    def test_submitted_at_timestamp_updated(self, api_client, personal_assignment_in_progress):
        """submitted_at이 현재 시간으로 설정되는지 확인"""
        before_request = timezone.now()

        url = reverse("personal-assignment-complete", kwargs={"id": personal_assignment_in_progress.id})
        resp = api_client.post(url)

        after_request = timezone.now()

        assert resp.status_code == status.HTTP_200_OK

        personal_assignment_in_progress.refresh_from_db()
        assert personal_assignment_in_progress.submitted_at is not None
        assert before_request <= personal_assignment_in_progress.submitted_at <= after_request

    def test_status_changed_to_submitted(self, api_client, personal_assignment_not_started):
        """상태가 SUBMITTED로 정확히 변경되는지 확인"""
        url = reverse("personal-assignment-complete", kwargs={"id": personal_assignment_not_started.id})
        resp = api_client.post(url)

        assert resp.status_code == status.HTTP_200_OK

        personal_assignment_not_started.refresh_from_db()
        assert personal_assignment_not_started.status == PersonalAssignment.Status.SUBMITTED
        # Enum 값 직접 비교
        assert personal_assignment_not_started.status == "SUBMITTED"

    def test_other_fields_unchanged(self, api_client, personal_assignment_in_progress):
        """완료 처리 시 status와 submitted_at 외의 다른 필드는 변경되지 않음"""
        original_student = personal_assignment_in_progress.student
        original_assignment = personal_assignment_in_progress.assignment
        original_solved_num = personal_assignment_in_progress.solved_num
        original_started_at = personal_assignment_in_progress.started_at

        url = reverse("personal-assignment-complete", kwargs={"id": personal_assignment_in_progress.id})
        resp = api_client.post(url)

        assert resp.status_code == status.HTTP_200_OK

        personal_assignment_in_progress.refresh_from_db()
        assert personal_assignment_in_progress.student == original_student
        assert personal_assignment_in_progress.assignment == original_assignment
        assert personal_assignment_in_progress.solved_num == original_solved_num
        assert personal_assignment_in_progress.started_at == original_started_at

    def test_success_response_structure(self, api_client, personal_assignment_in_progress):
        """성공 응답의 구조 확인"""
        url = reverse("personal-assignment-complete", kwargs={"id": personal_assignment_in_progress.id})
        resp = api_client.post(url)

        assert resp.status_code == status.HTTP_200_OK
        assert "success" in resp.data
        assert "data" in resp.data
        assert "message" in resp.data
        assert resp.data["success"] is True
        assert resp.data["data"] is None
        assert isinstance(resp.data["message"], str)

    def test_error_response_structure(self, api_client):
        """에러 응답의 구조 확인"""
        url = reverse("personal-assignment-complete", kwargs={"id": 999999})
        resp = api_client.post(url)

        assert resp.status_code == status.HTTP_404_NOT_FOUND
        assert "success" in resp.data
        assert "error" in resp.data
        assert "message" in resp.data
        assert resp.data["success"] is False
        assert resp.data["error"] is not None

    def test_multiple_assignments_independent(self, api_client, student, assignment, teacher, subject):
        """여러 PersonalAssignment가 독립적으로 완료 처리되는지 확인"""
        # 같은 학생에 대한 다른 assignment
        assignment2 = Assignment.objects.create(
            course_class=assignment.course_class,
            subject=subject,
            title="HW 2",
            description="",
            total_questions=5,
            due_at=timezone.now() + timedelta(days=14),
            grade="",
        )

        pa1 = PersonalAssignment.objects.create(
            student=student,
            assignment=assignment,
            status=PersonalAssignment.Status.IN_PROGRESS,
            solved_num=2,
        )

        pa2 = PersonalAssignment.objects.create(
            student=student,
            assignment=assignment2,
            status=PersonalAssignment.Status.NOT_STARTED,
            solved_num=0,
        )

        # pa1만 완료 처리
        url = reverse("personal-assignment-complete", kwargs={"id": pa1.id})
        resp = api_client.post(url)

        assert resp.status_code == status.HTTP_200_OK

        # pa1은 SUBMITTED, pa2는 NOT_STARTED 유지
        pa1.refresh_from_db()
        pa2.refresh_from_db()
        assert pa1.status == PersonalAssignment.Status.SUBMITTED
        assert pa2.status == PersonalAssignment.Status.NOT_STARTED

    def test_idempotent_completion(self, api_client, personal_assignment_in_progress):
        """같은 과제를 여러 번 완료 요청해도 안전한지 확인 (멱등성)"""
        url = reverse("personal-assignment-complete", kwargs={"id": personal_assignment_in_progress.id})

        # 첫 번째 완료
        resp1 = api_client.post(url)
        assert resp1.status_code == status.HTTP_200_OK

        personal_assignment_in_progress.refresh_from_db()
        first_submitted_at = personal_assignment_in_progress.submitted_at

        # 두 번째 완료 (약간의 시간 간격)
        import time

        time.sleep(0.1)

        resp2 = api_client.post(url)
        assert resp2.status_code == status.HTTP_200_OK

        personal_assignment_in_progress.refresh_from_db()
        second_submitted_at = personal_assignment_in_progress.submitted_at

        # 둘 다 성공하며, submitted_at은 업데이트됨
        assert second_submitted_at >= first_submitted_at

    def test_complete_with_zero_solved(self, api_client, personal_assignment_not_started):
        """solved_num이 0인 과제도 완료 가능"""
        assert personal_assignment_not_started.solved_num == 0

        url = reverse("personal-assignment-complete", kwargs={"id": personal_assignment_not_started.id})
        resp = api_client.post(url)

        assert resp.status_code == status.HTTP_200_OK

        personal_assignment_not_started.refresh_from_db()
        assert personal_assignment_not_started.status == PersonalAssignment.Status.SUBMITTED

    def test_complete_preserves_solved_num(self, api_client, personal_assignment_in_progress):
        """완료 처리 시 solved_num이 유지되는지 확인"""
        original_solved_num = personal_assignment_in_progress.solved_num

        url = reverse("personal-assignment-complete", kwargs={"id": personal_assignment_in_progress.id})
        resp = api_client.post(url)

        assert resp.status_code == status.HTTP_200_OK

        personal_assignment_in_progress.refresh_from_db()
        assert personal_assignment_in_progress.solved_num == original_solved_num

    def test_complete_unexpected_exception(self, api_client, personal_assignment_in_progress, monkeypatch):
        """완료 처리 중 예상치 못한 예외 발생 시 500 에러"""

        def mock_save(*args, **kwargs):
            raise Exception("Database save failed")

        url = reverse("personal-assignment-complete", kwargs={"id": personal_assignment_in_progress.id})
        monkeypatch.setattr(PersonalAssignment, "save", mock_save)
        resp = api_client.post(url)

        assert resp.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
        assert resp.data["success"] is False
