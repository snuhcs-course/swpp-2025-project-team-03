from rest_framework import serializers


class SubjectRequestSerializer(serializers.Serializer):
    """Subject 정보를 위한 serializer"""

    name = serializers.CharField(max_length=255)
