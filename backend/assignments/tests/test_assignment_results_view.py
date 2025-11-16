"""
AssignmentResultsView 테스트
- 과제별 제출 통계 조회 API 테스트
- total_students, submitted_students, submission_rate 필드 검증
"""

from datetime import timedelta
from unittest.mock import Mock, patch

import pytest
from assignments.models import Assignment
from catalog.models import Subject
from courses.models import CourseClass, Enrollment
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
    return Account.objects.create_user(email="teacher_res@test.com", password="pass1234", is_student=False)


@pytest.fixture
def subject():
    return Subject.objects.create(name="ResultMath")


@pytest.fixture
def course_class(teacher, subject):
    return CourseClass.objects.create(
        teacher=teacher,
        subject=subject,
        name="Result Class",
        description="Desc",
    )


@pytest.fixture
def assignment(course_class, subject):
    return Assignment.objects.create(
        course_class=course_class,
        subject=subject,
        title="Result Assignment",
        description="Desc",
        due_at=timezone.now() + timedelta(days=7),
    )


def create_student(idx: int):
    """Helper to create unique student accounts"""
    return Account.objects.create_user(email=f"result_student_{idx}@test.com", password="pass1234", is_student=True)


def enroll(student, course_class):
    """Helper to enroll student in class"""
    return Enrollment.objects.create(student=student, course_class=course_class, status=Enrollment.Status.ENROLLED)


class TestAssignmentResultsView:
    """AssignmentResultsView 통합 테스트"""

    def test_results_empty(self, api_client, assignment):
        """PersonalAssignment가 없는 경우 -> 모든 값 0"""
        url = reverse("assignment-results", kwargs={"id": assignment.id})
        resp = api_client.get(url)

        assert resp.status_code == status.HTTP_200_OK
        assert resp.data["success"] is True
        assert resp.data["message"] == "과제 결과 조회 성공"

        data = resp.data["data"]
        assert data["assignment_id"] == assignment.id
        assert data["total_students"] == 0
        assert data["submitted_students"] == 0
        assert data["submission_rate"] == 0.0

    def test_results_mixed_statuses(self, api_client, assignment, course_class):
        """여러 상태 혼합: 5명 중 2명 제출 -> 40.0%"""
        students = [create_student(i) for i in range(5)]
        for s in students:
            enroll(s, course_class)

        # 5명의 PersonalAssignment 생성 (NOT_STARTED)
        for s in students:
            PersonalAssignment.objects.create(
                student=s, assignment=assignment, status=PersonalAssignment.Status.NOT_STARTED
            )

        # 2명을 SUBMITTED로 변경
        submitted_subset = students[:2]
        PersonalAssignment.objects.filter(student__in=submitted_subset, assignment=assignment).update(
            status=PersonalAssignment.Status.SUBMITTED
        )

        url = reverse("assignment-results", kwargs={"id": assignment.id})
        resp = api_client.get(url)

        assert resp.status_code == status.HTTP_200_OK
        assert resp.data["success"] is True

        data = resp.data["data"]
        assert data["assignment_id"] == assignment.id
        assert data["total_students"] == 5
        assert data["submitted_students"] == 2
        assert data["submission_rate"] == 40.0

    def test_results_rounding(self, api_client, assignment, course_class):
        """소수점 2자리 반올림: 3명 중 1명 제출 -> 33.33%"""
        students = [create_student(i) for i in range(3)]
        for s in students:
            enroll(s, course_class)

        for s in students:
            PersonalAssignment.objects.create(
                student=s, assignment=assignment, status=PersonalAssignment.Status.NOT_STARTED
            )

        # 1명만 제출
        PersonalAssignment.objects.filter(student=students[0], assignment=assignment).update(
            status=PersonalAssignment.Status.SUBMITTED
        )

        url = reverse("assignment-results", kwargs={"id": assignment.id})
        resp = api_client.get(url)

        assert resp.status_code == status.HTTP_200_OK
        data = resp.data["data"]
        assert data["total_students"] == 3
        assert data["submitted_students"] == 1
        assert data["submission_rate"] == 33.33

    def test_results_all_submitted(self, api_client, assignment, course_class):
        """모든 학생 제출 -> 100.0%"""
        students = [create_student(i) for i in range(4)]
        for s in students:
            enroll(s, course_class)

        # 모두 SUBMITTED 상태로 생성
        for s in students:
            PersonalAssignment.objects.create(
                student=s, assignment=assignment, status=PersonalAssignment.Status.SUBMITTED
            )

        url = reverse("assignment-results", kwargs={"id": assignment.id})
        resp = api_client.get(url)

        assert resp.status_code == status.HTTP_200_OK
        data = resp.data["data"]
        assert data["total_students"] == 4
        assert data["submitted_students"] == 4
        assert data["submission_rate"] == 100.0

    def test_results_nonexistent_assignment(self, api_client):
        """존재하지 않는 assignment_id -> 빈 결과 반환"""
        url = reverse("assignment-results", kwargs={"id": 999999})
        resp = api_client.get(url)

        assert resp.status_code == status.HTTP_200_OK
        assert resp.data["success"] is True

        data = resp.data["data"]
        assert data["assignment_id"] == 999999
        assert data["total_students"] == 0
        assert data["submitted_students"] == 0
        assert data["submission_rate"] == 0.0

    def test_results_in_progress_status(self, api_client, assignment, course_class):
        """NOT_STARTED, IN_PROGRESS, SUBMITTED 혼합 - SUBMITTED만 카운트"""
        students = [create_student(i) for i in range(6)]
        for s in students:
            enroll(s, course_class)

        # 2명 NOT_STARTED
        PersonalAssignment.objects.create(
            student=students[0], assignment=assignment, status=PersonalAssignment.Status.NOT_STARTED
        )
        PersonalAssignment.objects.create(
            student=students[1], assignment=assignment, status=PersonalAssignment.Status.NOT_STARTED
        )

        # 2명 IN_PROGRESS
        PersonalAssignment.objects.create(
            student=students[2], assignment=assignment, status=PersonalAssignment.Status.IN_PROGRESS
        )
        PersonalAssignment.objects.create(
            student=students[3], assignment=assignment, status=PersonalAssignment.Status.IN_PROGRESS
        )

        # 2명 SUBMITTED
        PersonalAssignment.objects.create(
            student=students[4], assignment=assignment, status=PersonalAssignment.Status.SUBMITTED
        )
        PersonalAssignment.objects.create(
            student=students[5], assignment=assignment, status=PersonalAssignment.Status.SUBMITTED
        )

        url = reverse("assignment-results", kwargs={"id": assignment.id})
        resp = api_client.get(url)

        assert resp.status_code == status.HTTP_200_OK
        data = resp.data["data"]
        assert data["total_students"] == 6
        assert data["submitted_students"] == 2
        assert data["submission_rate"] == 33.33

    def test_results_single_student(self, api_client, assignment, course_class):
        """학생 1명 제출 -> 100.0%"""
        student = create_student(0)
        enroll(student, course_class)

        PersonalAssignment.objects.create(
            student=student, assignment=assignment, status=PersonalAssignment.Status.SUBMITTED
        )

        url = reverse("assignment-results", kwargs={"id": assignment.id})
        resp = api_client.get(url)

        assert resp.status_code == status.HTTP_200_OK
        data = resp.data["data"]
        assert data["total_students"] == 1
        assert data["submitted_students"] == 1
        assert data["submission_rate"] == 100.0

    def test_results_response_structure(self, api_client, assignment):
        """응답 구조 검증"""
        url = reverse("assignment-results", kwargs={"id": assignment.id})
        resp = api_client.get(url)

        assert resp.status_code == status.HTTP_200_OK

        # 최상위 필드 확인
        assert "success" in resp.data
        assert "data" in resp.data
        assert "message" in resp.data

        # data 필드 구조 확인
        data = resp.data["data"]
        assert "assignment_id" in data
        assert "total_students" in data
        assert "submitted_students" in data
        assert "submission_rate" in data

        # 타입 검증
        assert isinstance(data["assignment_id"], int)
        assert isinstance(data["total_students"], int)
        assert isinstance(data["submitted_students"], int)
        assert isinstance(data["submission_rate"], float)

    @patch("assignments.views.PersonalAssignment.objects")
    def test_results_exception_handling(self, mock_pa_objects, api_client, assignment):
        """예외 발생 시 500 에러 반환"""
        mock_qs = Mock()
        mock_qs.aggregate.side_effect = Exception("DB error")
        mock_pa_objects.filter.return_value = mock_qs

        url = reverse("assignment-results", kwargs={"id": assignment.id})
        resp = api_client.get(url)

        assert resp.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
        assert resp.data["success"] is False
        assert "과제 결과 조회" in resp.data["message"]

    def test_results_large_dataset(self, api_client, assignment, course_class):
        """많은 학생 수 처리: 100명 중 67명 제출 -> 67.0%"""
        students = [create_student(i) for i in range(100)]
        for s in students:
            enroll(s, course_class)

        # 100명 생성
        for s in students:
            PersonalAssignment.objects.create(
                student=s, assignment=assignment, status=PersonalAssignment.Status.NOT_STARTED
            )

        # 67명 제출
        PersonalAssignment.objects.filter(student__in=students[:67], assignment=assignment).update(
            status=PersonalAssignment.Status.SUBMITTED
        )

        url = reverse("assignment-results", kwargs={"id": assignment.id})
        resp = api_client.get(url)

        assert resp.status_code == status.HTTP_200_OK
        data = resp.data["data"]
        assert data["total_students"] == 100
        assert data["submitted_students"] == 67
        assert data["submission_rate"] == 67.0

    def test_results_zero_division_safe(self, api_client, assignment):
        """0으로 나누기 방지 확인 (total=0일 때)"""
        # PersonalAssignment 없는 상태
        url = reverse("assignment-results", kwargs={"id": assignment.id})
        resp = api_client.get(url)

        assert resp.status_code == status.HTTP_200_OK
        data = resp.data["data"]
        assert data["submission_rate"] == 0.0  # ZeroDivisionError 없이 0.0 반환

    def test_results_message_field(self, api_client, assignment):
        """성공 메시지 확인"""
        url = reverse("assignment-results", kwargs={"id": assignment.id})
        resp = api_client.get(url)

        assert resp.status_code == status.HTTP_200_OK
        assert resp.data["message"] == "과제 결과 조회 성공"


class TestAssignmentResultsViewUnit:
    """AssignmentResultsView 단위 테스트 - Mock 사용하여 로직 검증"""

    @patch("assignments.views.PersonalAssignment.objects")
    @patch("assignments.views.AssignmentResultSerializer")
    def test_aggregate_logic(self, mock_serializer_class, mock_pa_objects):
        """집계 로직 정상 동작 검증"""
        from assignments.views import AssignmentResultsView

        # Mock aggregate result
        mock_qs = Mock()
        mock_qs.aggregate.return_value = {"total": 10, "submitted": 7}
        mock_pa_objects.filter.return_value = mock_qs

        # Mock serializer
        mock_serializer = Mock()
        mock_serializer.data = {
            "assignment_id": 1,
            "total_students": 10,
            "submitted_students": 7,
            "submission_rate": 70.0,
        }
        mock_serializer_class.return_value = mock_serializer

        view = AssignmentResultsView()
        request = Mock()
        response = view.get(request, id=1)

        assert response.status_code == 200
        mock_pa_objects.filter.assert_called_once_with(assignment_id=1)
        mock_qs.aggregate.assert_called_once()

    @patch("assignments.views.PersonalAssignment.objects")
    @patch("assignments.views.AssignmentResultSerializer")
    def test_zero_total_handling(self, mock_serializer_class, mock_pa_objects):
        """total=0일 때 ZeroDivisionError 방지 확인"""
        from assignments.views import AssignmentResultsView

        mock_qs = Mock()
        mock_qs.aggregate.return_value = {"total": 0, "submitted": 0}
        mock_pa_objects.filter.return_value = mock_qs

        mock_serializer = Mock()
        mock_serializer.data = {
            "assignment_id": 1,
            "total_students": 0,
            "submitted_students": 0,
            "submission_rate": 0.0,
        }
        mock_serializer_class.return_value = mock_serializer

        view = AssignmentResultsView()
        request = Mock()
        response = view.get(request, id=1)

        assert response.status_code == 200
        # serializer에 전달된 데이터 확인
        call_args = mock_serializer_class.call_args
        assert call_args[1]["data"]["submission_rate"] == 0.0

    @patch("assignments.views.PersonalAssignment.objects")
    @patch("assignments.views.AssignmentResultSerializer")
    def test_rounding_precision(self, mock_serializer_class, mock_pa_objects):
        """소수점 2자리 반올림 검증"""
        from assignments.views import AssignmentResultsView

        mock_qs = Mock()
        mock_qs.aggregate.return_value = {"total": 3, "submitted": 1}
        mock_pa_objects.filter.return_value = mock_qs

        mock_serializer = Mock()
        mock_serializer.data = {
            "assignment_id": 1,
            "total_students": 3,
            "submitted_students": 1,
            "submission_rate": 33.33,
        }
        mock_serializer_class.return_value = mock_serializer

        view = AssignmentResultsView()
        request = Mock()
        response = view.get(request, id=1)

        # 전달된 데이터의 반올림 확인
        call_args = mock_serializer_class.call_args
        submitted_rate = call_args[1]["data"]["submission_rate"]
        assert submitted_rate == 33.33
        assert isinstance(submitted_rate, float)

    @patch("assignments.views.logger")
    @patch("assignments.views.PersonalAssignment.objects")
    def test_does_not_exist_exception(self, mock_pa_objects, mock_logger):
        """PersonalAssignment.DoesNotExist 예외 처리"""
        from assignments.views import AssignmentResultsView
        from submissions.models import PersonalAssignment

        mock_qs = Mock()
        mock_qs.aggregate.side_effect = PersonalAssignment.DoesNotExist("Not found")
        mock_pa_objects.filter.return_value = mock_qs

        view = AssignmentResultsView()
        request = Mock()
        response = view.get(request, id=999)

        assert response.status_code == 404
        assert response.data["success"] is False
        assert "개인 과제가 없습니다" in response.data["message"]
        mock_logger.error.assert_called()

    @patch("assignments.views.logger")
    @patch("assignments.views.PersonalAssignment.objects")
    def test_general_exception_handling(self, mock_pa_objects, mock_logger):
        """일반 예외 처리 - 500 에러 반환"""
        from assignments.views import AssignmentResultsView

        mock_qs = Mock()
        mock_qs.aggregate.side_effect = Exception("Database connection failed")
        mock_pa_objects.filter.return_value = mock_qs

        view = AssignmentResultsView()
        request = Mock()
        response = view.get(request, id=1)

        assert response.status_code == 500
        assert response.data["success"] is False
        mock_logger.error.assert_called()

    @patch("assignments.views.PersonalAssignment.objects")
    @patch("assignments.views.AssignmentResultSerializer")
    def test_filter_with_correct_assignment_id(self, mock_serializer_class, mock_pa_objects):
        """올바른 assignment_id로 필터링하는지 확인"""
        from assignments.views import AssignmentResultsView

        mock_qs = Mock()
        mock_qs.aggregate.return_value = {"total": 5, "submitted": 3}
        mock_pa_objects.filter.return_value = mock_qs

        mock_serializer = Mock()
        mock_serializer.data = {}
        mock_serializer_class.return_value = mock_serializer

        view = AssignmentResultsView()
        request = Mock()
        view.get(request, id=42)

        # assignment_id=42로 필터링했는지 확인
        mock_pa_objects.filter.assert_called_once_with(assignment_id=42)

    @patch("assignments.views.PersonalAssignment.objects")
    @patch("assignments.views.AssignmentResultSerializer")
    def test_aggregate_uses_q_object(self, mock_serializer_class, mock_pa_objects):
        """aggregate에서 Q 객체를 사용하여 SUBMITTED 상태만 카운트"""
        from assignments.views import AssignmentResultsView

        mock_qs = Mock()
        mock_qs.aggregate.return_value = {"total": 10, "submitted": 6}
        mock_pa_objects.filter.return_value = mock_qs

        mock_serializer = Mock()
        mock_serializer.data = {}
        mock_serializer_class.return_value = mock_serializer

        view = AssignmentResultsView()
        request = Mock()
        view.get(request, id=1)

        # aggregate가 호출되었는지 확인
        assert mock_qs.aggregate.called
        call_kwargs = mock_qs.aggregate.call_args[1]

        # 'total'과 'submitted' 키가 있는지 확인
        assert "total" in call_kwargs
        assert "submitted" in call_kwargs

    @patch("assignments.views.PersonalAssignment.objects")
    @patch("assignments.views.AssignmentResultSerializer")
    def test_serializer_receives_correct_data(self, mock_serializer_class, mock_pa_objects):
        """Serializer에 올바른 데이터가 전달되는지 확인"""
        from assignments.views import AssignmentResultsView

        mock_qs = Mock()
        mock_qs.aggregate.return_value = {"total": 20, "submitted": 15}
        mock_pa_objects.filter.return_value = mock_qs

        mock_serializer = Mock()
        mock_serializer.data = {}
        mock_serializer_class.return_value = mock_serializer

        view = AssignmentResultsView()
        request = Mock()
        view.get(request, id=123)

        # serializer 생성 시 전달된 데이터 확인
        call_args = mock_serializer_class.call_args
        data = call_args[1]["data"]

        assert data["assignment_id"] == 123
        assert data["total_students"] == 20
        assert data["submitted_students"] == 15
        assert data["submission_rate"] == 75.0

    @patch("assignments.views.PersonalAssignment.objects")
    @patch("assignments.views.AssignmentResultSerializer")
    def test_none_aggregate_values_handled(self, mock_serializer_class, mock_pa_objects):
        """aggregate가 None 반환 시 0으로 처리"""
        from assignments.views import AssignmentResultsView

        mock_qs = Mock()
        mock_qs.aggregate.return_value = {"total": None, "submitted": None}
        mock_pa_objects.filter.return_value = mock_qs

        mock_serializer = Mock()
        mock_serializer.data = {}
        mock_serializer_class.return_value = mock_serializer

        view = AssignmentResultsView()
        request = Mock()
        view.get(request, id=1)

        call_args = mock_serializer_class.call_args
        data = call_args[1]["data"]

        # None이 0으로 변환되었는지 확인
        assert data["total_students"] == 0
        assert data["submitted_students"] == 0
        assert data["submission_rate"] == 0.0

    @patch("assignments.views.create_api_response")
    @patch("assignments.views.PersonalAssignment.objects")
    @patch("assignments.views.AssignmentResultSerializer")
    def test_success_response_format(self, mock_serializer_class, mock_pa_objects, mock_create_api_response):
        """성공 응답 형식 확인"""
        from assignments.views import AssignmentResultsView

        mock_qs = Mock()
        mock_qs.aggregate.return_value = {"total": 5, "submitted": 5}
        mock_pa_objects.filter.return_value = mock_qs

        mock_serializer = Mock()
        mock_serializer.data = {"test": "data"}
        mock_serializer_class.return_value = mock_serializer

        mock_create_api_response.return_value = Mock(status_code=200)

        view = AssignmentResultsView()
        request = Mock()
        view.get(request, id=1)

        # create_api_response가 올바른 인자로 호출되었는지 확인
        mock_create_api_response.assert_called_once()
        call_kwargs = mock_create_api_response.call_args[1]

        assert call_kwargs["success"] is True
        assert call_kwargs["message"] == "과제 결과 조회 성공"
        assert call_kwargs["status_code"] == 200
