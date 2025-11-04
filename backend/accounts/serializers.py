from django.contrib.auth import authenticate
from rest_framework import serializers

from .models import Account


class SignupSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only=True)

    class Meta:
        model = Account
        fields = ["email", "password", "display_name", "is_student"]

    def create(self, validated_data):
        user = Account.objects.create_user(
            email=validated_data["email"],
            password=validated_data["password"],
            display_name=validated_data.get("display_name", ""),
            is_student=validated_data.get("is_student", True),
        )
        return user


class LoginSerializer(serializers.Serializer):
    email = serializers.EmailField()
    password = serializers.CharField(write_only=True)

    def validate(self, attrs):
        email = attrs.get("email")
        password = attrs.get("password")
        user = authenticate(email=email, password=password)

        if not user:
            raise serializers.ValidationError("잘못된 이메일 또는 비밀번호입니다.")
        if not user.is_active:
            raise serializers.ValidationError("비활성화된 계정입니다.")

        attrs["user"] = user
        return attrs


class UserResponseSerializer(serializers.ModelSerializer):
    role = serializers.SerializerMethodField()

    class Meta:
        model = Account
        fields = ["id", "email", "display_name", "is_student", "role"]

    def get_role(self, obj):
        return "STUDENT" if obj.is_student else "TEACHER"
