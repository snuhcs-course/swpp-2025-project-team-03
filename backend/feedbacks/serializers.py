from courses.models import CourseClass
from django.contrib.auth import get_user_model
from feedbacks.models import TeacherFeedback
from rest_framework import serializers

Account = get_user_model()


class TeacherInfoSerializer(serializers.ModelSerializer):
    """선생님 정보 serializer"""

    class Meta:
        model = Account
        fields = ["id", "display_name", "email"]


class StudentInfoSerializer(serializers.ModelSerializer):
    """학생 정보 serializer"""

    class Meta:
        model = Account
        fields = ["id", "display_name", "email"]


class CourseClassInfoSerializer(serializers.ModelSerializer):
    """클래스 정보 serializer"""

    class Meta:
        model = CourseClass
        fields = ["id", "name"]


class MessageSerializer(serializers.ModelSerializer):
    """메시지(피드백) 조회 serializer"""

    teacher = TeacherInfoSerializer(read_only=True)
    student = StudentInfoSerializer(read_only=True)
    course_class = CourseClassInfoSerializer(read_only=True)

    class Meta:
        model = TeacherFeedback
        fields = ["id", "course_class", "student", "teacher", "content", "created_at"]
