from catalog.serializers import TopicSerializer
from courses.serializers import CourseClassSerializer
from rest_framework import serializers

from .models import Assignment, Material


class MaterialSerializer(serializers.ModelSerializer):
    """Material 정보를 위한 serializer"""

    class Meta:
        model = Material
        fields = ["id", "kind", "s3_key", "bytes", "created_at"]


class AssignmentSerializer(serializers.ModelSerializer):
    """Assignment 기본 정보를 위한 serializer"""

    course_class = CourseClassSerializer(read_only=True)
    topics = TopicSerializer(many=True, read_only=True)
    materials = MaterialSerializer(many=True, read_only=True)

    class Meta:
        model = Assignment
        fields = [
            "id",
            "title",
            "description",
            "total_questions",
            "created_at",
            "visible_from",
            "due_at",
            "course_class",
            "topics",
            "materials",
            "grade",
        ]


class AssignmentDetailSerializer(serializers.ModelSerializer):
    """Assignment 상세 정보를 위한 serializer"""

    course_class = CourseClassSerializer(read_only=True)
    topics = TopicSerializer(many=True, read_only=True)
    materials = MaterialSerializer(many=True, read_only=True)

    class Meta:
        model = Assignment
        fields = [
            "id",
            "title",
            "description",
            "total_questions",
            "created_at",
            "visible_from",
            "due_at",
            "course_class",
            "topics",
            "materials",
            "grade",
        ]


class AssignmentUpdateSerializer(serializers.ModelSerializer):
    """Assignment 수정용 serializer"""

    class Meta:
        model = Assignment
        fields = ["title", "description", "total_questions", "visible_from", "due_at", "grade"]


class AssignmentCreateSerializer(serializers.Serializer):
    assignment_id = serializers.IntegerField(help_text="생성된 과제의 고유 ID")
    material_id = serializers.IntegerField(help_text="생성된 자료(Material)의 고유 ID")
    s3_key = serializers.CharField(help_text="S3에 저장될 PDF 파일의 key 경로")
    upload_url = serializers.URLField(help_text="PDF 업로드용 presigned URL")
