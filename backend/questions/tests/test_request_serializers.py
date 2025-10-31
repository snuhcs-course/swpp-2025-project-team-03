import pytest
from questions.request_serializers import QuestionCreateRequestSerializer
from rest_framework.exceptions import ValidationError

pytestmark = pytest.mark.django_db


class TestQuestionCreateRequestSerializer:
    """QuestionCreateRequestSerializer 테스트"""

    def test_validate_success(self):
        """정상적인 데이터 검증 테스트"""
        data = {
            "assignment_id": 1,
            "material_id": 1,
            "total_number": 5,
        }
        serializer = QuestionCreateRequestSerializer(data=data)
        assert serializer.is_valid(raise_exception=True)

    def test_validate_negative_assignment_id(self):
        """음수 assignment_id 검증 테스트"""
        data = {
            "assignment_id": -1,
            "material_id": 1,
            "total_number": 5,
        }
        serializer = QuestionCreateRequestSerializer(data=data)
        with pytest.raises(ValidationError) as exc_info:
            serializer.is_valid(raise_exception=True)
        assert "assignment_id 및 material_id는 양수여야 합니다" in str(exc_info.value.detail)

    def test_validate_zero_material_id(self):
        """0인 material_id 검증 테스트"""
        data = {
            "assignment_id": 1,
            "material_id": 0,
            "total_number": 5,
        }
        serializer = QuestionCreateRequestSerializer(data=data)
        with pytest.raises(ValidationError) as exc_info:
            serializer.is_valid(raise_exception=True)
        assert "assignment_id 및 material_id는 양수여야 합니다" in str(exc_info.value.detail)

    def test_validate_negative_total_number(self):
        """음수 total_number 검증 테스트"""
        data = {
            "assignment_id": 1,
            "material_id": 1,
            "total_number": -5,
        }
        serializer = QuestionCreateRequestSerializer(data=data)
        with pytest.raises(ValidationError) as exc_info:
            serializer.is_valid(raise_exception=True)
        assert "total_number는 양수여야 합니다" in str(exc_info.value.detail)
