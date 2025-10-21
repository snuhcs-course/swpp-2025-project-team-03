from rest_framework import serializers


class StudentEditRequestSerializer(serializers.Serializer):
    """학생 정보 수정 요청을 위한 serializer"""

    display_name = serializers.CharField(max_length=100, required=False)
    email = serializers.EmailField(required=False)


class StudentDeleteRequestSerializer(serializers.Serializer):
    """학생 삭제 요청을 위한 serializer"""

    reason = serializers.CharField(max_length=500, required=False)
