import pytest
from accounts.serializers import LoginSerializer, SignupSerializer, UserResponseSerializer
from django.contrib.auth import get_user_model
from rest_framework.exceptions import ValidationError

Account = get_user_model()

# 모든 테스트에서 DB 접근 허용
pytestmark = pytest.mark.django_db


class TestSignupSerializer:
    """SignupSerializer 테스트"""

    def test_create_user_success(self):
        """정상적인 유저 생성 테스트"""
        data = {
            "email": "test@example.com",
            "password": "testpass123",
            "display_name": "테스트유저",
            "is_student": True,
        }
        serializer = SignupSerializer(data=data)
        assert serializer.is_valid()

        user = serializer.save()
        assert user.email == "test@example.com"
        assert user.display_name == "테스트유저"
        assert user.is_student is True
        assert user.check_password("testpass123")

    def test_create_user_with_default_values(self):
        """기본값으로 유저 생성 테스트"""
        data = {
            "email": "test@example.com",
            "password": "testpass123",
        }
        serializer = SignupSerializer(data=data)
        assert serializer.is_valid()

        user = serializer.save()
        assert user.display_name == ""
        assert user.is_student is True

    def test_password_is_write_only(self):
        """password 필드가 write_only인지 테스트"""
        serializer = SignupSerializer()
        password_field = serializer.fields["password"]
        assert password_field.write_only is True


class TestLoginSerializer:
    """LoginSerializer 테스트"""

    def test_validate_success(self):
        """정상적인 로그인 검증 테스트"""
        user = Account.objects.create_user(
            email="test@example.com",
            password="testpass123",
            is_active=True,
        )

        data = {
            "email": "test@example.com",
            "password": "testpass123",
        }
        serializer = LoginSerializer(data=data)
        assert serializer.is_valid()

        validated_data = serializer.validated_data
        assert validated_data["user"] == user

    def test_validate_wrong_password(self):
        """잘못된 비밀번호 검증 테스트"""
        Account.objects.create_user(
            email="test@example.com",
            password="rightpass",
            is_active=True,
        )

        data = {
            "email": "test@example.com",
            "password": "wrongpass",
        }
        serializer = LoginSerializer(data=data)

        with pytest.raises(ValidationError) as exc_info:
            serializer.is_valid(raise_exception=True)

        assert "잘못된 이메일 또는 비밀번호입니다" in str(exc_info.value.detail)

    def test_validate_inactive_user(self):
        """비활성화된 유저 검증 테스트"""
        user = Account.objects.create_user(
            email="test@example.com",
            password="testpass123",
            is_active=True,
        )
        user.is_active = False
        user.save()

        data = {
            "email": "test@example.com",
            "password": "testpass123",
        }
        serializer = LoginSerializer(data=data)

        with pytest.raises(ValidationError) as exc_info:
            serializer.is_valid(raise_exception=True)

        # authenticate가 None을 반환하므로 "잘못된 이메일 또는 비밀번호" 에러 발생
        assert "잘못된 이메일 또는 비밀번호입니다" in str(exc_info.value.detail)

    def test_validate_inactive_user_with_authenticate_mock(self):
        """authenticate가 비활성화된 사용자를 반환하는 경우 테스트 (line 36 커버)"""
        from unittest.mock import patch

        user = Account.objects.create_user(
            email="inactive@example.com",
            password="testpass123",
            is_active=False,
        )

        data = {
            "email": "inactive@example.com",
            "password": "testpass123",
        }
        serializer = LoginSerializer(data=data)

        with patch("accounts.serializers.authenticate") as mock_authenticate:
            mock_authenticate.return_value = user

            with pytest.raises(ValidationError) as exc_info:
                serializer.is_valid(raise_exception=True)

            assert "비활성화된 계정입니다" in str(exc_info.value.detail)

    def test_validate_nonexistent_user(self):
        """존재하지 않는 유저 검증 테스트"""
        data = {
            "email": "nonexistent@example.com",
            "password": "testpass123",
        }
        serializer = LoginSerializer(data=data)

        with pytest.raises(ValidationError) as exc_info:
            serializer.is_valid(raise_exception=True)

        assert "잘못된 이메일 또는 비밀번호입니다" in str(exc_info.value.detail)

    def test_password_is_write_only(self):
        """password 필드가 write_only인지 테스트"""
        serializer = LoginSerializer()
        password_field = serializer.fields["password"]
        assert password_field.write_only is True


class TestUserResponseSerializer:
    """UserResponseSerializer 테스트"""

    def test_get_role_student(self):
        """학생 role 반환 테스트"""
        user = Account.objects.create_user(
            email="student@example.com",
            password="testpass123",
            is_student=True,
        )
        serializer = UserResponseSerializer(user)
        assert serializer.data["role"] == "STUDENT"

    def test_get_role_teacher(self):
        """선생님 role 반환 테스트"""
        user = Account.objects.create_user(
            email="teacher@example.com",
            password="testpass123",
            is_student=False,
        )
        serializer = UserResponseSerializer(user)
        assert serializer.data["role"] == "TEACHER"
