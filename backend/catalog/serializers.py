from rest_framework import serializers

from .models import Topic


class TopicSerializer(serializers.ModelSerializer):
    """Topic 정보를 위한 serializer"""

    class Meta:
        model = Topic
        fields = ["id", "name"]
