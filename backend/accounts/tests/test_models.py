import pytest
from django.contrib.auth import get_user_model

Account = get_user_model()

# 모든 테스트에서 DB 접근 허용
pytestmark = pytest.mark.django_db


class TestAccountManager:
    """AccountManager 테스트"""

    def test_create_user_success(self):
        """정상적인 유저 생성 테스트"""
        user = Account.objects.create_user(
            email="test@example.com",
            password="testpass123",
            display_name="테스트유저",
        )
        assert user.email == "test@example.com"
        assert user.display_name == "테스트유저"
        assert user.check_password("testpass123")
        assert user.is_student is True  # default

    def test_create_user_without_email_raises_error(self):
        """이메일 없이 유저 생성 시 에러 발생"""
        with pytest.raises(ValueError, match="Users must have an email address"):
            Account.objects.create_user(email="", password="testpass123")

    def test_create_user_email_normalized(self):
        """이메일이 정규화되는지 테스트"""
        user = Account.objects.create_user(
            email="TEST@EXAMPLE.COM",
            password="testpass123",
        )
        # normalize_email은 domain만 lowercase로 변환
        assert user.email == "TEST@example.com"

    def test_create_superuser_success(self):
        """정상적인 슈퍼유저 생성 테스트"""
        superuser = Account.objects.create_superuser(
            email="admin@example.com",
            password="adminpass123",
        )
        assert superuser.is_staff is True
        assert superuser.is_superuser is True
        assert superuser.is_active is True

    def test_create_superuser_without_staff_raises_error(self):
        """is_staff=False로 슈퍼유저 생성 시 에러 발생"""
        with pytest.raises(ValueError, match="Superuser must have is_staff=True"):
            Account.objects.create_superuser(
                email="admin@example.com",
                password="adminpass123",
                is_staff=False,
            )

    def test_create_superuser_without_superuser_flag_raises_error(self):
        """is_superuser=False로 슈퍼유저 생성 시 에러 발생"""
        with pytest.raises(ValueError, match="Superuser must have is_superuser=True"):
            Account.objects.create_superuser(
                email="admin@example.com",
                password="adminpass123",
                is_superuser=False,
            )


class TestAccountModel:
    """Account 모델 테스트"""

    def test_account_str_with_display_name(self):
        """display_name이 있는 경우 __str__ 테스트"""
        user = Account.objects.create_user(
            email="test@example.com",
            password="testpass123",
            display_name="테스트유저",
            is_student=True,
        )
        assert str(user) == "테스트유저 (Student)"

    def test_account_str_without_display_name(self):
        """display_name이 없는 경우 __str__ 테스트"""
        user = Account.objects.create_user(
            email="test@example.com",
            password="testpass123",
            display_name="",
            is_student=True,
        )
        assert str(user) == "test@example.com (Student)"

    def test_account_str_teacher(self):
        """선생님 계정 __str__ 테스트"""
        user = Account.objects.create_user(
            email="teacher@example.com",
            password="testpass123",
            display_name="선생님",
            is_student=False,
        )
        assert str(user) == "선생님 (Teacher)"
