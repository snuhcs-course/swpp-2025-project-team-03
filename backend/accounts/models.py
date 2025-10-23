from django.contrib.auth.models import AbstractUser, BaseUserManager
from django.db import models


class AccountManager(BaseUserManager):
    def create_user(self, email, password=None, **extra_fields):
        if not email:
            raise ValueError("Users must have an email address")

        email = self.normalize_email(email)
        user = self.model(email=email, **extra_fields)
        user.set_password(password)
        user.save(using=self._db)
        return user

    def create_superuser(self, email, password=None, **extra_fields):
        extra_fields.setdefault("is_staff", True)
        extra_fields.setdefault("is_superuser", True)
        extra_fields.setdefault("is_active", True)

        if extra_fields.get("is_staff") is not True:
            raise ValueError("Superuser must have is_staff=True.")
        if extra_fields.get("is_superuser") is not True:
            raise ValueError("Superuser must have is_superuser=True.")

        return self.create_user(email, password, **extra_fields)


class Account(AbstractUser):
    """
    이메일 기반 로그인용 커스텀 유저
    ERD: id, email, password, display_name, is_student, created_at
    """

    username = None  # 기본 username 제거
    email = models.EmailField(unique=True)
    display_name = models.CharField(max_length=100, blank=True)
    is_student = models.BooleanField(default=True)
    created_at = models.DateTimeField(auto_now_add=True)

    USERNAME_FIELD = "email"  # 로그인 필드로 email 사용
    REQUIRED_FIELDS = []  # createsuperuser 시 추가로 요구할 필드 없음

    objects = AccountManager()  # 커스텀 매니저 지정

    def __str__(self):
        role = "Student" if self.is_student else "Teacher"
        return f"{self.display_name or self.email} ({role})"
