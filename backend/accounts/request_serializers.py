from rest_framework import serializers
from .models import Account

class SignupRequestSerializer(serializers.ModelSerializer):
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

class LoginRequestSerializer(serializers.Serializer):
    email = serializers.EmailField()
    password = serializers.CharField()
