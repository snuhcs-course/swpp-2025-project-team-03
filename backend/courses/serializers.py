from catalog.models import Subject
from django.contrib.auth import get_user_model
from rest_framework import serializers

Account = get_user_model()


class SubjectSerializer(serializers.ModelSerializer):
    class Meta:
        model = Subject
        fields = ["id", "name", "code"]


class StudentSerializer(serializers.ModelSerializer):
    """학생 정보를 위한 serializer"""

    role = serializers.SerializerMethodField()

    class Meta:
        model = Account
        fields = ["id", "email", "display_name", "is_student", "role", "created_at"]

    def get_role(self, obj):
        return "STUDENT" if obj.is_student else "TEACHER"


class StudentDetailSerializer(serializers.ModelSerializer):
    """학생 상세 정보를 위한 serializer"""

    role = serializers.SerializerMethodField()

    class Meta:
        model = Account
        fields = ["id", "email", "display_name", "is_student", "role", "created_at"]

    def get_role(self, obj):
        return "STUDENT" if obj.is_student else "TEACHER"


class StudentEditResponseSerializer(serializers.ModelSerializer):
    """학생 정보 수정 응답을 위한 serializer"""

    role = serializers.SerializerMethodField()

    class Meta:
        model = Account
        fields = ["id", "email", "display_name", "is_student", "role"]

    def get_role(self, obj):
        return "STUDENT" if obj.is_student else "TEACHER"


class StudentDeleteResponseSerializer(serializers.Serializer):
    """학생 삭제 응답을 위한 serializer"""

    success = serializers.BooleanField()
    message = serializers.CharField()
