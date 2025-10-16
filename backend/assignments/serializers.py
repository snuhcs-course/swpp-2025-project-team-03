from rest_framework import serializers


class AssignmentCreateSerializer(serializers.Serializer):
    title = serializers.CharField(max_length=255)
    class_id = serializers.IntegerField()
    grade = serializers.CharField(max_length=50, required=False, allow_blank=True)
    subject = serializers.CharField(max_length=50, required=False, allow_blank=True)
    description = serializers.CharField(required=False, allow_blank=True)
    due_at = serializers.CharField()
