"""
보충 coverage 테스트 (assignments/views.py 단위 테스트)
"""

from unittest.mock import Mock, patch

import pytest
from assignments import views
from assignments.models import Assignment, Material
from assignments.views import AssignmentCreateView
from courses.models import CourseClass
from django.utils import timezone
from rest_framework import status


# to_str of assignment
def test_assignment_str():
    fake_class = CourseClass(name="1학년 국어")
    a = Assignment(title="국어 과제", course_class=fake_class)
    result = str(a)
    assert "국어 과제 / 1학년 국어" in result


# to_str of material
def test_material_str():
    fake_assignment = Assignment(title="과학 과제")
    m = Material(kind=Material.Kind.PDF)
    m.assignment = fake_assignment
    result = str(m)
    assert "과학 과제 [pdf]" in result


# ---------------------------------------------------------------------
# create_api_response()
# ---------------------------------------------------------------------
@pytest.mark.parametrize(
    "kwargs",
    [
        {"success": True, "data": {"ok": 1}, "message": "ok", "status_code": status.HTTP_200_OK},
        {"success": False, "data": None, "error": "err", "message": "fail", "status_code": status.HTTP_400_BAD_REQUEST},
    ],
)
def test_create_api_response(kwargs):
    """create_api_response 정상 작동 및 반환값 확인"""
    r = views.create_api_response(**kwargs)
    assert isinstance(r.data, dict)
    assert r.data["success"] == kwargs["success"]
    assert r.status_code == kwargs["status_code"]


# ---------------------------------------------------------------------
# AssignmentListView
# ---------------------------------------------------------------------
@patch("assignments.views.AssignmentSerializer")
@patch("assignments.views.Assignment.objects")
def test_assignment_listview_normal(mock_objects, mock_serializer):
    """AssignmentListView 정상 경로"""
    fake_assignment = Mock()
    fake_assignment.id = 1
    fake_assignment.title = "T"

    # mock queryset
    mock_qs = Mock()
    mock_qs.filter.return_value = mock_qs
    mock_qs.__iter__ = lambda self: iter([fake_assignment])
    mock_objects.select_related.return_value.prefetch_related.return_value = mock_qs

    mock_serializer.return_value.data = [{"id": 1, "title": "T"}]

    request = Mock()
    request.query_params = {"teacherId": "1", "classId": "2"}

    response = views.AssignmentListView().get(request)

    assert response.status_code == 200
    assert "과제 목록 조회 성공" in response.data["message"]


@patch("assignments.views.logger")
@patch("assignments.views.Assignment.objects")
def test_assignment_listview_exception(mock_objects, mock_logger):
    mock_objects.select_related.side_effect = Exception("DB fail")
    r = views.AssignmentListView().get(Mock())
    assert r.status_code == 500
    mock_logger.error.assert_called_once()


# ---------------------------------------------------------------------
# AssignmentDetailView (GET / PUT / DELETE)
# ---------------------------------------------------------------------
@patch("assignments.views.AssignmentDetailSerializer")
@patch("assignments.views.Assignment.objects")
def test_detailview_get_success(mock_objects, mock_serializer):
    """GET 정상 경로"""
    fake_assignment = Mock()
    fake_assignment.id = 1
    fake_assignment.title = "Mock Assignment"
    mock_qs = mock_objects.select_related.return_value.prefetch_related.return_value
    mock_qs.get.return_value = fake_assignment

    mock_serializer.return_value.data = {"id": 1, "title": "Mock Assignment"}
    response = views.AssignmentDetailView().get(Mock(), id=1)
    assert response.status_code == 200


@patch("assignments.views.Assignment.objects")
def test_detailview_get_notfound(mock_objects):
    mock_qs = mock_objects.select_related.return_value.prefetch_related.return_value
    mock_qs.get.side_effect = views.Assignment.DoesNotExist
    r = views.AssignmentDetailView().get(Mock(), id=999)
    assert r.status_code == 404


@patch("assignments.views.logger")
@patch("assignments.views.Assignment.objects")
def test_detailview_get_exception(mock_objects, mock_logger):
    mock_objects.select_related.side_effect = Exception("Boom")
    r = views.AssignmentDetailView().get(Mock(), id=1)
    assert r.status_code == 500
    mock_logger.error.assert_called_once()


@patch("assignments.views.AssignmentUpdateRequestSerializer")
@patch("assignments.views.AssignmentDetailSerializer")
@patch("assignments.views.Assignment.objects")
def test_detailview_put_success(mock_objects, mock_detail, mock_update):
    """PUT 정상"""
    mock_objects.get.return_value = Mock()
    ser = Mock()
    ser.is_valid.return_value = True
    ser.validated_data = {"title": "New"}
    mock_update.return_value = ser
    mock_objects.select_related.return_value.prefetch_related.return_value.get.return_value = Mock()
    r = views.AssignmentDetailView().put(Mock(), id=1)
    assert r.status_code == 200


@patch("assignments.views.AssignmentUpdateRequestSerializer")
@patch("assignments.views.Assignment.objects")
def test_detailview_put_invalid(mock_objects, mock_update):
    ser = Mock()
    ser.is_valid.return_value = False
    ser.errors = {"title": ["missing"]}
    mock_update.return_value = ser
    mock_objects.get.return_value = Mock()
    r = views.AssignmentDetailView().put(Mock(), id=1)
    assert r.status_code == 400


@patch("assignments.views.Assignment.objects")
def test_detailview_put_notfound(mock_objects):
    mock_objects.get.side_effect = views.Assignment.DoesNotExist
    r = views.AssignmentDetailView().put(Mock(), id=1)
    assert r.status_code == 404


@patch("assignments.views.logger")
@patch("assignments.views.Assignment.objects")
def test_detailview_put_exception(mock_objects, mock_logger):
    mock_objects.get.side_effect = Exception("boom")
    r = views.AssignmentDetailView().put(Mock(), id=1)
    assert r.status_code == 500


@patch("assignments.views.Assignment.objects")
def test_detailview_delete_success(mock_objects):
    obj = Mock()
    mock_objects.get.return_value = obj
    r = views.AssignmentDetailView().delete(Mock(), id=1)
    assert r.status_code == 200
    obj.delete.assert_called_once()


@patch("assignments.views.Assignment.objects")
def test_detailview_delete_notfound(mock_objects):
    mock_objects.get.side_effect = views.Assignment.DoesNotExist
    r = views.AssignmentDetailView().delete(Mock(), id=1)
    assert r.status_code == 404


@patch("assignments.views.logger")
@patch("assignments.views.Assignment.objects")
def test_detailview_delete_exception(mock_objects, mock_logger):
    mock_objects.get.side_effect = Exception("DB crash")
    r = views.AssignmentDetailView().delete(Mock(), id=1)
    assert r.status_code == 500


# ---------------------------------------------------------------------
# AssignmentCreateView (간단 branch coverage)
# ---------------------------------------------------------------------
@patch("assignments.views.CourseClass.objects")
@patch("assignments.views.AssignmentCreateRequestSerializer")
def test_createview_invalid_class(mock_serializer, mock_course):
    ser = Mock()
    ser.is_valid.return_value = True
    ser.validated_data = {"class_id": 999, "subject": "X", "title": "A", "due_at": "2025-10-25"}
    mock_serializer.return_value = ser
    mock_course.get.side_effect = views.CourseClass.DoesNotExist
    r = views.AssignmentCreateView().post(Mock())
    assert r.status_code == 400


@patch("assignments.views.Subject.objects")
@patch("assignments.views.CourseClass.objects")
@patch("assignments.views.AssignmentCreateRequestSerializer")
def test_createview_invalid_due(mock_serializer, mock_course, mock_subject):
    ser = Mock()
    ser.is_valid.return_value = True
    ser.validated_data = {"class_id": 1, "subject": "X", "title": "A", "due_at": "invalid-date"}
    mock_serializer.return_value = ser
    mock_course.get.return_value = Mock()
    mock_subject.get_or_create.return_value = (Mock(), True)
    r = views.AssignmentCreateView().post(Mock())
    assert r.status_code == 400


# ---------------------------------------------------------------------
# 기타 단순 View (Submit / Results / Questions)
# ---------------------------------------------------------------------
@patch("assignments.views.PersonalAssignment.objects")
@patch("assignments.views.AssignmentResultSerializer")
def test_assignment_simple_views_all(mock_serializer, mock_pa_objects):
    v1 = views.AssignmentSubmitView().post(Mock(), id=1)

    # Mock AssignmentResultsView dependencies
    mock_qs = Mock()
    mock_qs.aggregate.return_value = {"total": 0, "submitted": 0}
    mock_pa_objects.filter.return_value = mock_qs

    mock_ser_instance = Mock()
    mock_ser_instance.is_valid.return_value = True
    mock_ser_instance.data = {"assignment_id": 1, "total_students": 0, "submitted_students": 0, "submission_rate": 0.0}
    mock_serializer.return_value = mock_ser_instance

    v2 = views.AssignmentResultsView().get(Mock(), id=1)
    v3 = views.AssignmentQuestionsView().get(Mock(), id=1)
    assert v1.status_code == 201
    assert v2.status_code == 200
    assert v3.status_code == 200


@patch("assignments.views.Material.objects.create")
@patch("assignments.views.AssignmentCreateRequestSerializer")
@patch("assignments.views.CourseClass.objects")
@patch("assignments.views.Subject.objects")
@patch("assignments.views.Assignment.objects")
@patch("assignments.views.Enrollment.objects")
@patch("assignments.views.PersonalAssignment")
@patch("assignments.views.boto3.client")
def test_due_at_make_aware_branch(
    mock_boto3,
    mock_personal_assignment,
    mock_enrollments,
    mock_assignment_objects,
    mock_subject_objects,
    mock_courseclass_objects,
    mock_serializer_class,
    mock_material_create,
):
    # Serializer가 naive datetime 문자열을 주도록
    mock_serializer = Mock()
    mock_serializer.is_valid.return_value = True
    mock_serializer.validated_data = {
        "class_id": 1,
        "title": "T",
        "subject": "Math",
        "due_at": "2025-10-25 23:59:00",  # <- timezone 정보 없는 naive 포맷
        "description": "D",
    }
    mock_serializer_class.return_value = mock_serializer

    # CourseClass / Subject / Assignment / Material mock
    mock_course = Mock()
    mock_course.id = 1
    mock_courseclass_objects.get.return_value = mock_course
    mock_material_create.return_value = Mock(id=5)

    mock_subject = Mock()
    mock_subject.id = 7
    mock_subject_objects.get_or_create.return_value = (mock_subject, True)

    mock_assignment = Mock()
    mock_assignment.id = 123
    mock_assignment_objects.create.return_value = mock_assignment

    # ENROLLED 학생 1명만 세팅
    enrollment = Mock()
    enrollment.student = Mock()
    qs = Mock()
    qs.select_related.return_value = [enrollment]
    mock_enrollments.filter.return_value = qs

    # S3 presigned URL mock
    mock_s3 = Mock()
    mock_s3.generate_presigned_url.return_value = "https://example.com/put"
    mock_boto3.return_value = mock_s3

    # make_aware 스파이용 패치
    with patch("assignments.views.timezone.make_aware", wraps=timezone.make_aware) as spy_make_aware:
        view = AssignmentCreateView()
        req = Mock()
        req.data = mock_serializer.validated_data
        r = view.post(req)

    assert r.status_code == 201
    assert spy_make_aware.called


@patch("assignments.views.AssignmentCreateRequestSerializer")
@patch("assignments.views.CourseClass.objects")
@patch("assignments.views.Subject.objects")
@patch("assignments.views.Assignment.objects")
@patch("assignments.views.Enrollment.objects")
@patch("assignments.views.PersonalAssignment.objects.bulk_create")
def test_personal_assignment_block_raises_and_rolls_back(
    mock_bulk_create,
    mock_enrollments,
    mock_assignment_objects,
    mock_subject_objects,
    mock_courseclass_objects,
    mock_serializer_class,
):
    # Serializer OK
    mock_serializer = Mock()
    mock_serializer.is_valid.return_value = True
    mock_serializer.validated_data = {
        "class_id": 1,
        "title": "T",
        "subject": "Math",
        "due_at": "2025-10-25T23:59:00+09:00",
        "description": "D",
    }
    mock_serializer_class.return_value = mock_serializer

    # Course / Subject / Assignment
    mock_course = Mock()
    mock_course.id = 1
    mock_courseclass_objects.get.return_value = mock_course

    mock_subject = Mock()
    mock_subject.id = 7
    mock_subject_objects.get_or_create.return_value = (mock_subject, True)

    mock_assignment = Mock()
    mock_assignment.id = 999
    mock_assignment_objects.create.return_value = mock_assignment

    # ENROLLED 1명 세팅
    enrollment = Mock()
    enrollment.student = Mock()
    qs = Mock()
    qs.select_related.return_value = [enrollment]
    mock_enrollments.filter.return_value = qs

    # bulk_create에서 예외 발생시켜 except 블록 진입
    mock_bulk_create.side_effect = Exception("boom")

    view = AssignmentCreateView()
    req = Mock()
    req.data = mock_serializer.validated_data
    r = view.post(req)

    assert r.status_code == 500
    assert r.data["error"] == "Failed to create PersonalAssignments"

    # 롤백(assignment.delete) 호출되었는지 확인
    assert mock_assignment.delete.called
