from rest_framework import serializers


class AchievementStatisticsSerializer(serializers.Serializer):
    """성취기준별 통계 시리얼라이저"""

    total_questions = serializers.IntegerField(min_value=0, help_text="해당 성취기준 문제 수")
    correct_questions = serializers.IntegerField(min_value=0, help_text="맞춘 문제 수")
    accuracy = serializers.FloatField(min_value=0.0, max_value=100.0, help_text="정답률 (%)")
    content = serializers.CharField(help_text="성취기준 내용")


class CurriculumAnalysisSerializer(serializers.Serializer):
    """성취기준 분석 응답 시리얼라이저"""

    total_questions = serializers.IntegerField(min_value=0, help_text="전체 문제 수")
    total_correct = serializers.IntegerField(min_value=0, help_text="맞춘 문제 수")
    overall_accuracy = serializers.FloatField(min_value=0.0, max_value=100.0, help_text="전체 정답률 (%)")
    achievement_statistics = serializers.DictField(child=AchievementStatisticsSerializer(), help_text="성취기준별 통계")

    def to_representation(self, instance):
        """응답 데이터 포맷팅"""
        achievement_statistics = instance.get("achievement_statistics", {})

        # 각 성취기준별 통계에 content가 없는 경우 기본값 설정
        for code, stats in achievement_statistics.items():
            if "content" not in stats:
                stats["content"] = "성취기준 내용을 찾을 수 없습니다."

        return {
            "total_questions": instance.get("total_questions", 0),
            "total_correct": instance.get("total_correct", 0),
            "overall_accuracy": round(instance.get("overall_accuracy", 0.0), 1),
            "achievement_statistics": achievement_statistics,
        }
