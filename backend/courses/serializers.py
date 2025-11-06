from catalog.serializers import SubjectSerializer
from courses.models import CourseClass, Enrollment
from django.contrib.auth import get_user_model
from rest_framework import serializers

Account = get_user_model()


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


class EnrollmentSerializer(serializers.ModelSerializer):
    """등록 정보를 위한 serializer"""

    student_name = serializers.CharField(source="student.display_name", read_only=True)
    class_name = serializers.CharField(source="course_class.name", read_only=True)

    class Meta:
        model = Enrollment
        fields = [
            "id",
            "student_name",
            "class_name",
            "status",
        ]


class StudentStatisticsSerializer(serializers.Serializer):
    """학생 진도 통계량을 위한 serializer"""

    total_assignments = serializers.IntegerField()  # 총 과제 수
    submitted_assignments = serializers.IntegerField()  # 완료된 과제 수
    in_progress_assignments = serializers.IntegerField()  # 진행 중인 과제 수
    not_started_assignments = serializers.IntegerField()  # 시작하지 않은 과제 수


class StudentClassStatisticsSerializer(serializers.Serializer):
    """특정 반의 학생 통계량을 위한 serializer"""

    average_score = serializers.FloatField()  # 평균 점수
    completion_rate = serializers.FloatField()  # 완료율 (0-100)


class ClassStudentsStatisticsSerializer(serializers.Serializer):
    """반의 모든 학생 통계량을 위한 serializer"""

    class StudentStatisticsItemSerializer(serializers.Serializer):
        student_id = serializers.IntegerField()
        average_score = serializers.FloatField()
        completion_rate = serializers.FloatField()
        total_assignments = serializers.IntegerField()  # 전체 과제 수
        completed_assignments = serializers.IntegerField()  # 완료한 과제 수

    overall_completion_rate = serializers.FloatField()  # 전체 평균 완료율
    students = StudentStatisticsItemSerializer(many=True)


class ClassCompletionRateSerializer(serializers.Serializer):
    """반의 완료율을 위한 serializer"""

    completion_rate = serializers.FloatField()  # 완료율 (0-100)
