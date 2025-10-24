from catalog.request_serializers import SubjectRequestSerializer
from rest_framework import serializers


class AssignmentCreateRequestSerializer(serializers.Serializer):
    title = serializers.CharField(max_length=255)
    class_id = serializers.IntegerField()
    grade = serializers.CharField(max_length=50, required=False, allow_blank=True)
    subject = serializers.CharField(max_length=50, required=False, allow_blank=True)
    description = serializers.CharField(required=False, allow_blank=True)
    due_at = serializers.CharField()


class AssignmentUpdateRequestSerializer(serializers.Serializer):
    """Assignment 수정용 serializer"""

    title = serializers.CharField(max_length=255, required=False)
    description = serializers.CharField(required=False, allow_blank=True)
    total_questions = serializers.IntegerField(required=False)
    visible_from = serializers.DateTimeField(required=False)
    due_at = serializers.DateTimeField(required=False)
    grade = serializers.CharField(max_length=16, required=False, allow_blank=True)
    subject = SubjectRequestSerializer(required=False)
