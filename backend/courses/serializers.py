from catalog.models import Subject
from courses.models import CourseClass, Enrollment
from django.contrib.auth import get_user_model
from rest_framework import serializers

Account = get_user_model()


class SubjectSerializer(serializers.ModelSerializer):
    class Meta:
        model = Subject
        fields = ["id", "name"]


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
    enrollments = serializers.SerializerMethodField()

    class Meta:
        model = Account
        fields = ["id", "email", "display_name", "is_student", "role", "created_at", "enrollments"]

    def get_role(self, obj):
        return "STUDENT" if obj.is_student else "TEACHER"

    def get_enrollments(self, obj):
        enrollments = obj.enrollments.filter(status=Enrollment.Status.ENROLLED)
        return CourseClassSerializer([enrollment.course_class for enrollment in enrollments], many=True).data


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


class CourseClassSerializer(serializers.ModelSerializer):
    """클래스 정보를 위한 serializer"""

    subject = SubjectSerializer(read_only=True)
    teacher_name = serializers.CharField(source="teacher.display_name", read_only=True)
    student_count = serializers.SerializerMethodField()

    class Meta:
        model = CourseClass
        fields = [
            "id",
            "name",
            "description",
            "subject",
            "teacher_name",
            "start_date",
            "end_date",
            "student_count",
            "created_at",
        ]

    def get_student_count(self, obj):
        return obj.enrollments.filter(status=Enrollment.Status.ENROLLED).count()
