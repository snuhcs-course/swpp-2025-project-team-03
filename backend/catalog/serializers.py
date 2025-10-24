from rest_framework import serializers

from .models import Subject


class SubjectSerializer(serializers.ModelSerializer):
    """Subject 정보를 위한 serializer"""

    class Meta:
        model = Subject
        fields = ["id", "name"]
