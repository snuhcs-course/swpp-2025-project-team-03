from rest_framework import serializers


class QuestionCreateRequestSerializer(serializers.Serializer):
    """요약용 PDF를 지정하기 위한 요청 serializer"""

    assignment_id = serializers.IntegerField(required=True, help_text="대상 Assignment의 ID")
    material_id = serializers.IntegerField(required=True, help_text="요약할 Material의 ID (PDF)")

    def validate(self, data):
        """필요 시 추가 유효성 검사"""
        if data["assignment_id"] <= 0 or data["material_id"] <= 0:
            raise serializers.ValidationError("assignment_id 및 material_id는 양수여야 합니다.")
        return data
