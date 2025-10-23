from rest_framework import serializers

from .models import Account


class SignupRequestSerializer(serializers.Serializer):
    email = serializers.EmailField()
    password = serializers.CharField(write_only=True)
    name = serializers.CharField()
    role = serializers.ChoiceField(choices=["STUDENT", "TEACHER"])

    def create(self, validated_data):
        user = Account.objects.create_user(
            email=validated_data["email"],
            password=validated_data["password"],
            display_name=validated_data["name"],
            is_student=validated_data["role"] == "STUDENT",
        )
        return user


class LoginRequestSerializer(serializers.Serializer):
    email = serializers.EmailField()
    password = serializers.CharField()
