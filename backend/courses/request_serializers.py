from catalog.models import Subject
from django.contrib.auth import get_user_model
from rest_framework import serializers

Account = get_user_model()


class StudentEditRequestSerializer(serializers.Serializer):
    """학생 정보 수정 요청을 위한 serializer"""

    display_name = serializers.CharField(max_length=100, required=False)
    email = serializers.EmailField(required=False)


class StudentDeleteRequestSerializer(serializers.Serializer):
    """학생 삭제 요청을 위한 serializer"""

    reason = serializers.CharField(max_length=500, required=False)


class ClassCreateRequestSerializer(serializers.Serializer):
    """클래스 생성 요청을 위한 serializer"""

    name = serializers.CharField(max_length=255)
    description = serializers.CharField(required=False, allow_blank=True)
    subject_name = serializers.CharField(max_length=100)
    teacher_id = serializers.IntegerField()
    start_date = serializers.DateTimeField()
    end_date = serializers.DateTimeField()

    def create(self, validated_data):
        from .models import CourseClass

        # Subject 가져오기 또는 생성
        subject, _ = Subject.objects.get_or_create(name=validated_data["subject_name"])

        # Teacher 가져오기
        teacher = Account.objects.get(id=validated_data["teacher_id"])

        # CourseClass 생성
        course_class = CourseClass.objects.create(
            name=validated_data["name"],
            description=validated_data.get("description", ""),
            subject=subject,
            teacher=teacher,
            start_date=validated_data["start_date"],
            end_date=validated_data["end_date"],
        )

        return course_class
