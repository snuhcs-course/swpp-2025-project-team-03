from rest_framework import serializers


class QuestionSerializer(serializers.Serializer):
    """생성된 개별 문항 정보"""

    id = serializers.IntegerField(help_text="DB에 저장된 Question의 고유 ID")
    number = serializers.IntegerField(help_text="문항 번호 (1부터 시작)")
    question = serializers.CharField(help_text="질문 내용")
    answer = serializers.CharField(help_text="정답 텍스트")
    explanation = serializers.CharField(help_text="정답 해설")
    difficulty = serializers.ChoiceField(choices=["easy", "medium", "hard"], help_text="난이도")


class QuestionCreateSerializer(serializers.Serializer):
    """PDF 요약 + 문제 생성 결과"""

    assignment_id = serializers.IntegerField(help_text="요약/질문이 생성된 과제 ID")
    material_summary_id = serializers.IntegerField(help_text="요약 텍스트 Material의 ID")
    summary_preview = serializers.CharField(help_text="요약문 일부 (앞 100자)")
    questions = QuestionSerializer(many=True, help_text="생성된 질문 리스트")


class TailQuestionSerializer(serializers.Serializer):
    """생성된 개별 문항 정보 (Tail Question)"""

    is_correct = serializers.BooleanField(help_text="학생의 답변이 정답인지 여부")
    number_str = serializers.SerializerMethodField(help_text="문항 번호 문자열 (예: '3' 또는 '3-1')")

    tail_question = QuestionSerializer(
        help_text="Tail Question 정보. 꼬리 질문이 없으면 null일 수 있습니다.",
        required=False,  # 필수가 아님
        allow_null=True,  # null 값 허용
    )

    def get_number_str(self, obj):
        # obj가 dict인 경우와 object인 경우 모두 처리
        recalled_num = obj.get("recalled_num") if isinstance(obj, dict) else obj.recalled_num
        number = obj.get("number") if isinstance(obj, dict) else obj.number

        if recalled_num == 0:
            return f"{number}"
        else:
            return f"{number}-{recalled_num}"
