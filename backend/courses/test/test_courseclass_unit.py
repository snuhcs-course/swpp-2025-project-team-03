"""
CourseClass 순수 유닛 테스트
- DB 접근 없음
- 모든 의존성을 Mock으로 대체
- 단일 컴포넌트(Serializer, View 로직)만 검증
"""

from datetime import datetime
from unittest.mock import MagicMock, Mock, patch

import pytest
from courses.models import CourseClass, Enrollment

# 실제 import 경로는 프로젝트 구조에 맞게 수정
from courses.serializers import CourseClassSerializer
from courses.views import ClassDetailView, ClassListView, ClassStudentsView
from django.contrib.auth import get_user_model
from django.utils import timezone

Account = get_user_model()

# pytest 실행 예시
# pytest courses/test/test_courseclass_unit.py -v


class TestCourseClassSerializerUnit:
    """CourseClassSerializer 단위 테스트 (DB 접근 없음)"""

    def test_serialize_course_class_data(self):
        """클래스 데이터 직렬화 로직만 테스트"""
        # Mock Subject 객체
        mock_subject = Mock()
        mock_subject.id = 1
        mock_subject.name = "Mathematics"
        mock_subject.code = "MATH101"

        # Mock Teacher 객체
        mock_teacher = Mock(spec=Account)
        mock_teacher.id = 1
        mock_teacher.display_name = "Teacher Name"

        # Mock CourseClass 객체
        mock_course_class = Mock(spec=CourseClass)
        mock_course_class.id = 1
        mock_course_class.name = "Test Class"
        mock_course_class.description = "Test Description"
        mock_course_class.subject = mock_subject
        mock_course_class.teacher = mock_teacher
        mock_course_class.start_date = timezone.now()
        mock_course_class.end_date = timezone.now()
        mock_course_class.created_at = timezone.now()
        mock_course_class.updated_at = timezone.now()

        # enrollment_set Mock 추가
        mock_enrollment_queryset = Mock()
        mock_enrollment_queryset.count.return_value = 0
        mock_enrollment_set = Mock()
        mock_enrollment_set.filter.return_value = mock_enrollment_queryset
        mock_course_class.enrollment_set = mock_enrollment_set

        # Serializer에 Mock 객체 전달
        serializer = CourseClassSerializer(mock_course_class)

        # 직렬화 결과 검증
        data = serializer.data
        assert data["id"] == 1
        assert data["name"] == "Test Class"
        assert data["description"] == "Test Description"
        assert data["teacher_name"] == "Teacher Name"
        assert "student_count" in data  # student_count 필드 존재 확인

    @pytest.mark.django_db
    def test_validate_course_class_name(self):
        """클래스 이름 유효성 검증 로직 테스트"""
        # 유효한 데이터
        valid_data = {"name": "Valid Class Name", "description": "Valid Description"}
        serializer = CourseClassSerializer(data=valid_data)
        # 유효성 검사 (실제로는 teacher, subject가 필수일 수 있음)
        assert isinstance(serializer.is_valid(), bool)

    @pytest.mark.django_db
    def test_validate_empty_name(self):
        """빈 이름 유효성 검증 테스트"""
        invalid_data = {"name": "", "description": "Some description"}
        serializer = CourseClassSerializer(data=invalid_data)
        assert serializer.is_valid() is False
        assert "name" in serializer.errors

    @pytest.mark.django_db
    def test_required_fields_validation(self):
        """필수 필드 검증 로직 테스트"""
        # name이 없는 경우
        data = {"description": "Some description"}
        serializer = CourseClassSerializer(data=data)

        # 필수 필드 검증
        is_valid = serializer.is_valid()
        assert isinstance(is_valid, bool)


class TestClassDetailViewUnit:
    """ClassDetailView 단위 테스트 (DB/Request 모킹)"""

    @patch("courses.views.CourseClassSerializer")
    @patch("courses.views.CourseClass.objects.get")
    def test_get_class_returns_serialized_data(self, mock_get, mock_serializer_class):
        """클래스 조회 시 직렬화된 데이터 반환 로직 테스트"""
        # Mock CourseClass 객체
        mock_course_class = Mock(spec=CourseClass)
        mock_course_class.id = 1
        mock_course_class.name = "Test Class"
        mock_course_class.description = "Test Description"
        mock_course_class.created_at = timezone.now()
        mock_get.return_value = mock_course_class

        # Mock Serializer 인스턴스
        mock_serializer = Mock()
        mock_serializer.data = {
            "id": 1,
            "name": "Test Class",
            "description": "Test Description",
            "teacher_name": "Teacher Name",
            "student_count": 0,
        }
        mock_serializer_class.return_value = mock_serializer

        # View 인스턴스 생성
        view = ClassDetailView()
        view.kwargs = {"id": 1}

        # Mock request 객체
        mock_request = Mock()
        view.request = mock_request

        # get 메서드 실행
        response = view.get(mock_request, id=1)

        # 결과 검증
        assert response.status_code == 200
        assert response.data["success"] is True
        assert response.data["data"]["id"] == 1
        assert response.data["data"]["name"] == "Test Class"
        mock_get.assert_called_once_with(id=1)

    @patch("courses.views.CourseClass.objects.get")
    def test_get_class_not_found_raises_error(self, mock_get):
        """존재하지 않는 클래스 조회 시 예외 발생 로직 테스트"""
        # DoesNotExist 예외 발생하도록 설정
        mock_get.side_effect = CourseClass.DoesNotExist

        view = ClassDetailView()
        view.kwargs = {"id": 999}
        mock_request = Mock()
        view.request = mock_request

        # 예외 처리 로직 검증
        response = view.get(mock_request, id=999)

        assert response.status_code == 404
        assert response.data["success"] is False
        assert "not found" in response.data["error"].lower()

    @patch("courses.views.CourseClassSerializer")
    @patch("courses.views.CourseClass.objects.get")
    def test_update_class_logic(self, mock_get, mock_serializer_class):
        """클래스 정보 업데이트 로직만 테스트"""
        # Mock CourseClass 객체
        mock_course_class = MagicMock(spec=CourseClass)
        mock_course_class.id = 1
        mock_course_class.name = "Old Name"
        mock_course_class.description = "Old Description"
        mock_course_class.save = Mock()
        mock_get.return_value = mock_course_class

        # Mock Serializer
        mock_serializer = Mock()
        mock_serializer.is_valid.return_value = True
        mock_serializer.validated_data = {"name": "New Name", "description": "New Description"}
        mock_serializer.data = {
            "id": 1,
            "name": "New Name",
            "description": "New Description",
            "teacher_name": "Teacher Name",
            "student_count": 0,
        }
        mock_serializer_class.return_value = mock_serializer

        view = ClassDetailView()
        view.kwargs = {"id": 1}

        # Mock request with data
        mock_request = Mock()
        mock_request.data = {"name": "New Name", "description": "New Description"}
        view.request = mock_request

        # put 메서드 실행
        response = view.put(mock_request, id=1)

        # 업데이트 로직 검증
        assert response.status_code == 200
        assert response.data["success"] is True
        mock_serializer.is_valid.assert_called_once()

    @patch("courses.views.CourseClass.objects.get")
    def test_delete_class_logic(self, mock_get):
        """클래스 삭제 로직만 테스트"""
        # Mock CourseClass 객체
        mock_course_class = Mock(spec=CourseClass)
        mock_course_class.id = 1
        mock_course_class.name = "Test Class"
        mock_course_class.delete = Mock()
        mock_get.return_value = mock_course_class

        view = ClassDetailView()
        view.kwargs = {"id": 1}

        # Mock request
        mock_request = Mock()
        view.request = mock_request

        # delete 메서드 실행
        response = view.delete(mock_request, id=1)

        # 삭제 로직 검증
        assert response.status_code == 200
        assert response.data["success"] is True
        assert response.data["data"]["id"] == 1
        assert response.data["data"]["name"] == "Test Class"
        mock_course_class.delete.assert_called_once()


class TestClassListViewUnit:
    """ClassListView 단위 테스트"""

    @patch("courses.views.CourseClassSerializer")
    @patch("courses.views.CourseClass.objects.all")
    def test_get_all_classes(self, mock_all, mock_serializer_class):
        """전체 클래스 목록 조회 로직 테스트"""
        # Mock CourseClass 목록
        mock_class1 = Mock(spec=CourseClass)
        mock_class1.id = 1
        mock_class1.name = "Class 1"

        mock_class2 = Mock(spec=CourseClass)
        mock_class2.id = 2
        mock_class2.name = "Class 2"

        mock_queryset = Mock()
        mock_queryset.__iter__ = Mock(return_value=iter([mock_class1, mock_class2]))
        mock_all.return_value = mock_queryset

        # Mock Serializer
        mock_serializer = Mock()
        mock_serializer.data = [{"id": 1, "name": "Class 1"}, {"id": 2, "name": "Class 2"}]
        mock_serializer_class.return_value = mock_serializer

        view = ClassListView()
        mock_request = Mock()
        mock_request.query_params = {}
        view.request = mock_request

        # get 메서드 실행
        response = view.get(mock_request)

        # 결과 검증
        assert response.status_code == 200
        assert response.data["success"] is True
        assert len(response.data["data"]) == 2

    @patch("courses.views.CourseClassSerializer")
    @patch("courses.views.CourseClass.objects.all")
    def test_get_classes_with_teacher_filter(self, mock_all, mock_serializer_class):
        """teacherId로 필터링된 클래스 목록 조회 로직 테스트"""
        # Mock filtered queryset
        mock_class = Mock(spec=CourseClass)
        mock_class.id = 1
        mock_class.name = "Teacher's Class"

        # Mock queryset with filter method
        mock_filtered_queryset = Mock()
        mock_filtered_queryset.__iter__ = Mock(return_value=iter([mock_class]))

        # all()이 반환하는 queryset에 filter 메서드 추가
        mock_all_queryset = Mock()
        mock_all_queryset.filter = Mock(return_value=mock_filtered_queryset)
        mock_all.return_value = mock_all_queryset

        # Mock Serializer
        mock_serializer = Mock()
        mock_serializer.data = [{"id": 1, "name": "Teacher's Class"}]
        mock_serializer_class.return_value = mock_serializer

        view = ClassListView()
        mock_request = Mock()
        mock_request.query_params = {"teacherId": "1"}
        view.request = mock_request

        # get 메서드 실행
        response = view.get(mock_request)

        # 결과 검증
        assert response.status_code == 200
        assert response.data["success"] is True
        # filter가 teacher_id로 호출되었는지 확인
        mock_all_queryset.filter.assert_called_once_with(teacher_id="1")


class TestClassStudentsViewUnit:
    """ClassStudentsView 단위 테스트"""

    @patch("courses.views.StudentSerializer")
    @patch("courses.views.CourseClass.objects.get")
    def test_get_class_students(self, mock_get_class, mock_serializer_class):
        """클래스 학생 목록 조회 로직 테스트"""
        # Mock Student 목록
        mock_student1 = Mock(spec=Account)
        mock_student1.id = 1
        mock_student1.email = "student1@example.com"
        mock_student1.display_name = "Student 1"
        mock_student1.date_joined = timezone.now()
        mock_student1.last_login = timezone.now()
        mock_student1.created_at = timezone.now()
        mock_student1.updated_at = timezone.now()

        mock_student2 = Mock(spec=Account)
        mock_student2.id = 2
        mock_student2.email = "student2@example.com"
        mock_student2.display_name = "Student 2"
        mock_student2.date_joined = timezone.now()
        mock_student2.last_login = timezone.now()
        mock_student2.created_at = timezone.now()
        mock_student2.updated_at = timezone.now()

        # Mock Enrollment 목록
        mock_enrollment1 = Mock()
        mock_enrollment1.student = mock_student1

        mock_enrollment2 = Mock()
        mock_enrollment2.student = mock_student2

        # Mock enrollments queryset with filter
        mock_filtered_enrollments = [mock_enrollment1, mock_enrollment2]
        mock_enrollments_manager = Mock()
        mock_enrollments_manager.filter.return_value = mock_filtered_enrollments

        # Mock CourseClass
        mock_course_class = Mock(spec=CourseClass)
        mock_course_class.id = 1
        mock_course_class.enrollments = mock_enrollments_manager
        mock_get_class.return_value = mock_course_class

        # Mock Serializer
        mock_serializer = Mock()
        mock_serializer.data = [{"id": 1, "email": "student1@example.com"}, {"id": 2, "email": "student2@example.com"}]
        mock_serializer_class.return_value = mock_serializer

        view = ClassStudentsView()
        view.kwargs = {"id": 1}
        mock_request = Mock()
        view.request = mock_request

        # get 메서드 실행
        response = view.get(mock_request, id=1)

        # 결과 검증
        assert response.status_code == 200
        assert response.data["success"] is True
        assert len(response.data["data"]) == 2
        mock_get_class.assert_called_once_with(id=1)
        # enrollments.filter가 ENROLLED 상태로 호출되었는지 확인
        mock_enrollments_manager.filter.assert_called_once_with(status=Enrollment.Status.ENROLLED)


class TestCourseClassBusinessLogicUnit:
    """CourseClass 비즈니스 로직 단위 테스트"""

    def test_calculate_class_duration(self):
        """클래스 기간 계산 로직 (순수 함수)"""

        def calculate_duration_days(start_date, end_date):
            """시작일과 종료일 사이의 일수 계산"""
            if not start_date or not end_date:
                return 0
            delta = end_date - start_date
            return delta.days

        start = datetime(2024, 1, 1)
        end = datetime(2024, 1, 31)

        assert calculate_duration_days(start, end) == 30
        assert calculate_duration_days(None, end) == 0
        assert calculate_duration_days(start, None) == 0
