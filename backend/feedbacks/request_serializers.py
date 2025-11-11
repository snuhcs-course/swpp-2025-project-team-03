from accounts.models import Account
from courses.models import CourseClass
from rest_framework import serializers


class MessageSendRequestSerializer(serializers.Serializer):  # pragma: no cover
    """메시지(피드백) 전송 요청 serializer"""

    teacher_id = serializers.IntegerField(required=True)
    class_id = serializers.IntegerField(required=True)
    student_id = serializers.IntegerField(required=True)
    content = serializers.CharField(required=True, allow_blank=False, max_length=5000)

    def validate_class_id(self, value):
        """클래스 존재 여부 확인"""
        if not CourseClass.objects.filter(id=value).exists():
            raise serializers.ValidationError("Invalid class_id. Class not found")
        return value

    def validate_student_id(self, value):
        """학생 존재 여부 및 역할 확인"""
        try:
            student = Account.objects.get(id=value)
            if not student.is_student:
                raise serializers.ValidationError("User is not a student")
        except Account.DoesNotExist:
            raise serializers.ValidationError("Invalid student_id. Student not found")
        return value

    def validate_teacher_id(self, value):
        """선생님 존재 여부 및 역할 확인"""
        try:
            teacher = Account.objects.get(id=value)
            if teacher.is_student:
                raise serializers.ValidationError("User is not a teacher")
        except Account.DoesNotExist:
            raise serializers.ValidationError("Invalid teacher_id. Teacher not found")
        return value
