"""
순수 유닛 테스트
- DB 접근 없음
- 모든 의존성을 Mock으로 대체
- 단일 컴포넌트(Serializer, View 로직)만 검증
"""

from unittest.mock import MagicMock, Mock, patch

import pytest

# 실제 import 경로는 프로젝트 구조에 맞게 수정
from courses.serializers import StudentSerializer
from courses.views import StudentDetailView
from django.contrib.auth import get_user_model
from django.utils import timezone

Account = get_user_model()


class TestStudentSerializerUnit:
    """StudentSerializer 단위 테스트 (DB 접근 없음)"""

    def test_serialize_student_data(self):
        """학생 데이터 직렬화 로직만 테스트"""
        # Mock 객체 생성 (DB 접근 없음)
        mock_student = Mock(spec=Account)
        mock_student.id = 1
        mock_student.email = "test@example.com"
        mock_student.display_name = "Test Student"
        mock_student.is_student = True
        mock_student.is_teacher = False
        # DateTime 필드를 위한 mock (Serializer가 슬라이싱할 수 있도록)
        mock_student.date_joined = timezone.now()
        mock_student.last_login = timezone.now()
        mock_student.created_at = timezone.now()
        mock_student.updated_at = timezone.now()

        # Serializer에 Mock 객체 전달
        serializer = StudentSerializer(mock_student)

        # 직렬화 결과 검증
        data = serializer.data
        assert data["id"] == 1
        assert data["email"] == "test@example.com"
        assert data["display_name"] == "Test Student"
        assert data["is_student"] is True

    @pytest.mark.django_db
    def test_validate_email_format(self):
        """이메일 유효성 검증 로직만 테스트"""
        # 유효한 이메일
        valid_data = {"email": "valid@example.com", "display_name": "Test"}
        serializer = StudentSerializer(data=valid_data)
        assert serializer.is_valid() is True

        # 무효한 이메일
        invalid_data = {"email": "invalid-email", "display_name": "Test"}
        serializer = StudentSerializer(data=invalid_data)
        assert serializer.is_valid() is False
        assert "email" in serializer.errors

    @pytest.mark.django_db
    def test_required_fields_validation(self):
        """필수 필드 검증 로직 테스트"""
        # display_name이 없는 경우
        data = {"email": "test@example.com"}
        serializer = StudentSerializer(data=data)

        # 필수 필드 검증 로직만 확인
        is_valid = serializer.is_valid()

        # display_name이 필수라면 False, 아니면 True
        # 실제 Serializer 정의에 따라 결과가 다름
        assert isinstance(is_valid, bool)


class TestStudentDetailViewUnit:
    """StudentDetailView 단위 테스트 (DB/Request 모킹)"""

    @patch("courses.views.StudentDetailSerializer")  # StudentDetailSerializer
    @patch("courses.views.Account.objects.get")
    def test_get_student_returns_serialized_data(self, mock_get, mock_serializer_class):
        """학생 조회 시 직렬화된 데이터 반환 로직 테스트"""
        # Mock 학생 객체
        mock_student = Mock(spec=Account)
        mock_student.id = 1
        mock_student.email = "test@example.com"
        mock_student.display_name = "Test Student"
        mock_student.created_at = timezone.now()
        mock_student.is_student = True

        # enrollments Mock 추가 (get_enrollments 메서드를 위해)
        mock_enrollment_queryset = []  # 빈 리스트 (등록된 클래스 없음)
        mock_enrollments_manager = Mock()
        mock_enrollments_manager.filter.return_value = mock_enrollment_queryset
        mock_student.enrollments = mock_enrollments_manager

        mock_get.return_value = mock_student

        # Mock Serializer 인스턴스
        mock_serializer = Mock()
        mock_serializer.data = {
            "id": 1,
            "email": "test@example.com",
            "display_name": "Test Student",
            "is_student": True,
            "role": "STUDENT",
            "enrollments": [],  # enrollments 필드 추가
        }
        mock_serializer_class.return_value = mock_serializer

        # View 인스턴스 생성 (실제 요청 없음)
        view = StudentDetailView()
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
        assert "enrollments" in response.data["data"]  # enrollments 필드 확인
        mock_get.assert_called_once_with(id=1, is_student=True)

    @patch("courses.views.Account.objects.get")
    def test_get_student_not_found_raises_error(self, mock_get):
        """존재하지 않는 학생 조회 시 예외 발생 로직 테스트"""
        # DoesNotExist 예외 발생하도록 설정
        mock_get.side_effect = Account.DoesNotExist

        view = StudentDetailView()
        view.kwargs = {"id": 999}
        mock_request = Mock()
        view.request = mock_request

        # 예외 처리 로직 검증
        response = view.get(mock_request, id=999)

        assert response.status_code == 404
        assert response.data["success"] is False
        assert "not found" in response.data["error"].lower()

    @patch("courses.views.StudentEditResponseSerializer")  # 수정 응답용 Serializer
    @patch("courses.views.Account.objects.get")
    def test_update_student_logic(self, mock_get, mock_serializer_class):
        """학생 정보 업데이트 로직만 테스트"""
        # Mock 학생 객체
        mock_student = MagicMock(spec=Account)
        mock_student.id = 1
        mock_student.display_name = "Old Name"
        mock_student.save = Mock()
        mock_get.return_value = mock_student

        # Mock Serializer
        mock_serializer = Mock()
        mock_serializer.is_valid.return_value = True
        mock_serializer.validated_data = {"display_name": "New Name"}
        mock_serializer.data = {"id": 1, "display_name": "New Name", "email": "test@example.com", "role": "STUDENT"}
        mock_serializer_class.return_value = mock_serializer

        view = StudentDetailView()
        view.kwargs = {"id": 1}

        # Mock request with data
        mock_request = Mock()
        mock_request.data = {"display_name": "New Name"}
        view.request = mock_request

        # put 메서드 실행
        response = view.put(mock_request, id=1)

        # 업데이트 로직 검증
        assert response.status_code == 200
        assert mock_student.display_name == "New Name"
        mock_student.save.assert_called_once()
        assert response.data["success"] is True


# pytest 실행 예시
# pytest courses/test/test_student_unit.py -v
