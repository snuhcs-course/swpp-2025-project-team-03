from django.contrib import admin
from django.contrib.auth.admin import UserAdmin
from .models import Account

@admin.register(Account)
class AccountAdmin(UserAdmin):
    # username 제거 → email을 사용자명 필드처럼 사용
    model = Account
    list_display = ("id", "email", "display_name", "is_student", "is_staff", "date_joined")
    list_filter = ("is_student", "is_staff", "is_superuser", "is_active")

    fieldsets = (
        (None, {"fields": ("email", "password")}),
        ("Personal info", {"fields": ("display_name", "is_student")}),
        ("Permissions", {"fields": ("is_active", "is_staff", "is_superuser", "groups", "user_permissions")}),
        ("Important dates", {"fields": ("last_login", "date_joined")}),
    )

    add_fieldsets = (
        (None, {
            "classes": ("wide",),
            "fields": ("email", "password1", "password2", "is_student", "is_staff", "is_superuser", "is_active"),
        }),
    )

    search_fields = ("email", "display_name")
    ordering = ("id",)
