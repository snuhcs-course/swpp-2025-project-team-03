"""
Assignment 순수 유닛 테스트
- DB 접근 없음
- 모든 의존성을 Mock으로 대체
- 단일 컴포넌트(View 로직)만 검증
"""

from unittest.mock import Mock, patch

from assignments.models import Assignment, Material
from assignments.views import AssignmentCreateView
from courses.models import CourseClass, Enrollment
from django.contrib.auth import get_user_model

Account = get_user_model()

# pytest 실행 예시
# pytest assignments/test/test_assignment_unit.py -v


class TestAssignmentCreateViewUnit:
    """AssignmentCreateView 단위 테스트"""

    @patch("assignments.views.uuid.uuid4")
    @patch("assignments.views.settings")
    @patch("assignments.views.logger")
    @patch("assignments.views.Material.objects")
    @patch("assignments.views.boto3.client")
    @patch("assignments.views.PersonalAssignment")
    @patch("assignments.views.Enrollment.objects")
    @patch("assignments.views.Assignment.objects")
    @patch("assignments.views.CourseClass.objects")
    @patch("assignments.views.AssignmentCreateRequestSerializer")
    def test_create_assignment_logic(
        self,
        mock_serializer_class,
        mock_courseclass_objects,
        mock_assignment_objects,
        mock_enrollment_objects,
        mock_personal_assignment_class,
        mock_boto3,
        mock_material_objects,
        mock_settings,
        mock_logger,
        mock_uuid,
    ):
        """과제 생성 로직 검증"""
        # Mock UUID
        mock_uuid.return_value = "test-uuid-1234"

        # Mock Settings
        mock_settings.AWS_ACCESS_KEY_ID = "test-key"
        mock_settings.AWS_SECRET_ACCESS_KEY = "test-secret"
        mock_settings.AWS_REGION = "us-east-1"
        mock_settings.AWS_STORAGE_BUCKET_NAME = "test-bucket"

        # Mock Serializer
        mock_serializer = Mock()
        mock_serializer.is_valid.return_value = True
        mock_serializer.validated_data = {
            "class_id": 1,
            "title": "Test Assignment",
            "due_at": "2025-10-25T23:59:00+09:00",
            "description": "Test description",
        }
        mock_serializer_class.return_value = mock_serializer

        # Mock CourseClass
        mock_course_class = Mock(spec=CourseClass)
        mock_course_class.id = 1
        mock_courseclass_objects.get.return_value = mock_course_class

        # Mock Assignment
        mock_assignment = Mock(spec=Assignment)
        mock_assignment.id = 10
        mock_assignment_objects.create.return_value = mock_assignment

        # Mock Enrollment (Student)
        mock_student = Mock(spec=Account)
        mock_student.id = 1
        mock_enrollment = Mock(spec=Enrollment)
        mock_enrollment.student = mock_student

        # enrollment filter 체이닝
        mock_enrollment_qs = Mock()
        mock_enrollment_qs.select_related.return_value = [mock_enrollment]
        mock_enrollment_objects.filter.return_value = mock_enrollment_qs

        # PersonalAssignment Mock
        mock_personal_assignment_objects = Mock()
        mock_personal_assignment_class.objects = mock_personal_assignment_objects

        # Mock S3
        mock_s3 = Mock()
        mock_s3.generate_presigned_url.return_value = "https://s3.presigned.url"
        mock_boto3.return_value = mock_s3

        # Mock Material
        mock_material = Mock(spec=Material)
        mock_material.id = 5
        mock_material_objects.create.return_value = mock_material

        # View 실행
        view = AssignmentCreateView()
        mock_request = Mock()
        mock_request.data = {
            "class_id": 1,
            "title": "Test Assignment",
            "due_at": "2025-10-25T23:59:00+09:00",
            "description": "Test description",
        }

        response = view.post(mock_request)

        # 검증
        assert response.status_code == 201
        mock_courseclass_objects.get.assert_called_once_with(id=1)
        mock_assignment_objects.create.assert_called_once()
        mock_personal_assignment_objects.bulk_create.assert_called_once()
        mock_s3.generate_presigned_url.assert_called_once()
        mock_material_objects.create.assert_called_once()

    @patch("assignments.views.AssignmentCreateRequestSerializer")
    @patch("assignments.views.CourseClass.objects")
    def test_create_assignment_invalid_class_id(self, mock_courseclass_objects, mock_serializer_class):
        """존재하지 않는 class_id 처리 로직 검증"""
        # Mock Serializer
        mock_serializer = Mock()
        mock_serializer.is_valid.return_value = True
        mock_serializer.validated_data = {"class_id": 99999, "title": "Test", "due_at": "2025-10-25T23:59:00+09:00"}
        mock_serializer_class.return_value = mock_serializer

        # CourseClass.DoesNotExist 발생
        from courses.models import CourseClass

        mock_courseclass_objects.get.side_effect = CourseClass.DoesNotExist

        view = AssignmentCreateView()
        mock_request = Mock()
        mock_request.data = mock_serializer.validated_data

        response = view.post(mock_request)

        # 검증
        assert response.status_code == 400
        assert response.data["success"] is False
        assert "Invalid class_id" in response.data["error"]

    @patch("assignments.views.AssignmentCreateRequestSerializer")
    @patch("assignments.views.CourseClass.objects")
    def test_create_assignment_invalid_due_at_format(self, mock_courseclass_objects, mock_serializer_class):
        """잘못된 due_at 형식 처리 로직 검증"""
        # Mock Serializer
        mock_serializer = Mock()
        mock_serializer.is_valid.return_value = True
        mock_serializer.validated_data = {"class_id": 1, "title": "Test", "due_at": "invalid-date"}
        mock_serializer_class.return_value = mock_serializer

        # Mock CourseClass
        mock_course_class = Mock()
        mock_courseclass_objects.get.return_value = mock_course_class

        view = AssignmentCreateView()
        mock_request = Mock()
        mock_request.data = mock_serializer.validated_data

        response = view.post(mock_request)

        # 검증
        assert response.status_code == 400
        assert response.data["success"] is False
        assert "Invalid due_at format" in response.data["error"]

    @patch("assignments.views.uuid.uuid4")
    @patch("assignments.views.settings")
    @patch("assignments.views.logger")
    @patch("assignments.views.boto3.client")
    @patch("assignments.views.Enrollment.objects")
    @patch("assignments.views.Assignment.objects")
    @patch("assignments.views.CourseClass.objects")
    @patch("assignments.views.AssignmentCreateRequestSerializer")
    def test_create_assignment_s3_failure_rollback(
        self,
        mock_serializer_class,
        mock_courseclass_objects,
        mock_assignment_objects,
        mock_enrollment_objects,
        mock_boto3,
        mock_logger,
        mock_settings,
        mock_uuid,
    ):
        """S3 실패 시 rollback 로직 검증"""
        # Mock UUID
        mock_uuid.return_value = "test-uuid-1234"

        # Mock Settings
        mock_settings.AWS_ACCESS_KEY_ID = "test-key"
        mock_settings.AWS_SECRET_ACCESS_KEY = "test-secret"
        mock_settings.AWS_REGION = "us-east-1"
        mock_settings.AWS_STORAGE_BUCKET_NAME = "test-bucket"

        # Mock Serializer
        mock_serializer = Mock()
        mock_serializer.is_valid.return_value = True
        mock_serializer.validated_data = {"class_id": 1, "title": "Test", "due_at": "2025-10-25T23:59:00+09:00"}
        mock_serializer_class.return_value = mock_serializer

        # Mock CourseClass
        mock_course_class = Mock()
        mock_courseclass_objects.get.return_value = mock_course_class

        # Mock Enrollment (빈 리스트)
        mock_enrollment_qs = Mock()
        mock_enrollment_qs.select_related.return_value = []
        mock_enrollment_objects.filter.return_value = mock_enrollment_qs

        # Mock Assignment
        mock_assignment = Mock()
        mock_assignment.id = 10
        mock_assignment.delete = Mock()
        mock_assignment_objects.create.return_value = mock_assignment

        # Mock S3 실패
        mock_s3 = Mock()
        mock_s3.generate_presigned_url.side_effect = Exception("S3 Error")
        mock_boto3.return_value = mock_s3

        view = AssignmentCreateView()
        mock_request = Mock()
        mock_request.data = mock_serializer.validated_data

        response = view.post(mock_request)

        # 검증
        assert response.status_code == 500
        assert "Failed to generate presigned URL" in str(response.data["error"])
        mock_assignment.delete.assert_called_once()  # rollback 확인

    @patch("assignments.views.uuid.uuid4")
    @patch("assignments.views.settings")
    @patch("assignments.views.logger")
    @patch("assignments.views.Material.objects")
    @patch("assignments.views.boto3.client")
    @patch("assignments.views.PersonalAssignment")
    @patch("assignments.views.Enrollment.objects")
    @patch("assignments.views.Assignment.objects")
    @patch("assignments.views.CourseClass.objects")
    @patch("assignments.views.AssignmentCreateRequestSerializer")
    def test_create_personal_assignments_for_enrolled_students(
        self,
        mock_serializer_class,
        mock_courseclass_objects,
        mock_assignment_objects,
        mock_enrollment_objects,
        mock_personal_assignment_class,
        mock_boto3,
        mock_material_objects,
        mock_settings,
        mock_logger,
        mock_uuid,
    ):
        """등록된 학생들에게 PersonalAssignment 생성 로직 검증"""
        # Mock UUID
        mock_uuid.return_value = "test-uuid-1234"

        # Mock Settings
        mock_settings.AWS_ACCESS_KEY_ID = "test-key"
        mock_settings.AWS_SECRET_ACCESS_KEY = "test-secret"
        mock_settings.AWS_REGION = "us-east-1"
        mock_settings.AWS_STORAGE_BUCKET_NAME = "test-bucket"

        # Mock Serializer
        mock_serializer = Mock()
        mock_serializer.is_valid.return_value = True
        mock_serializer.validated_data = {"class_id": 1, "title": "Test", "due_at": "2025-10-25T23:59:00+09:00"}
        mock_serializer_class.return_value = mock_serializer

        # Mock Students
        mock_student1 = Mock(spec=Account)
        mock_student1.id = 1
        mock_student2 = Mock(spec=Account)
        mock_student2.id = 2

        # Mock Enrollments
        mock_enrollment1 = Mock(spec=Enrollment)
        mock_enrollment1.student = mock_student1
        mock_enrollment2 = Mock(spec=Enrollment)
        mock_enrollment2.student = mock_student2

        # Mock Enrollment QuerySet
        mock_enrollment_qs = Mock()
        mock_enrollment_qs.select_related.return_value = [mock_enrollment1, mock_enrollment2]
        mock_enrollment_objects.filter.return_value = mock_enrollment_qs

        # Mock CourseClass
        mock_course_class = Mock()
        mock_courseclass_objects.get.return_value = mock_course_class

        # Mock Assignment
        mock_assignment = Mock()
        mock_assignment.id = 10
        mock_assignment_objects.create.return_value = mock_assignment

        # PersonalAssignment Mock
        mock_personal_assignment_objects = Mock()
        mock_personal_assignment_class.objects = mock_personal_assignment_objects

        # Mock S3
        mock_s3 = Mock()
        mock_s3.generate_presigned_url.return_value = "https://s3.presigned.url"
        mock_boto3.return_value = mock_s3

        # Mock Material
        mock_material = Mock(spec=Material)
        mock_material.id = 5
        mock_material_objects.create.return_value = mock_material

        view = AssignmentCreateView()
        mock_request = Mock()
        mock_request.data = mock_serializer.validated_data

        response = view.post(mock_request)

        # bulk_create 호출 확인
        assert response.status_code == 201
        mock_personal_assignment_objects.bulk_create.assert_called_once()

        # bulk_create에 전달된 인자 확인
        call_args = mock_personal_assignment_objects.bulk_create.call_args[0][0]
        assert len(call_args) == 2  # 2명의 학생
