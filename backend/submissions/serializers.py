from assignments.models import Assignment
from feedbacks.serializers import StudentInfoSerializer
from rest_framework import serializers

from .models import PersonalAssignment


class AssignmentInfoSerializer(serializers.ModelSerializer):
    """과제 정보 serializer"""

    class Meta:
        model = Assignment
        fields = ["id", "title", "description", "total_questions", "visible_from", "due_at", "grade"]


class PersonalAssignmentSerializer(serializers.ModelSerializer):
    """개인 과제 정보를 위한 serializer"""

    student = StudentInfoSerializer()
    assignment = AssignmentInfoSerializer()

    class Meta:
        model = PersonalAssignment
        fields = ["student", "assignment", "status", "solved_num", "started_at", "submitted_at"]


class PersonalAssignmentStatisticsSerializer(serializers.Serializer):
    """개인 과제 통계 정보를 위한 serializer"""

    total_questions = serializers.IntegerField()  # 전체 질문 수 (꼬리 질문 포함)
    answered_questions = serializers.IntegerField()  # 답변한 질문 수 (꼬리 질문 포함)
    correct_answers = serializers.IntegerField()  # 정답인 답변 수 (꼬리 질문 포함)
    accuracy = serializers.FloatField()  # 정확도 (정답 비율)
    total_problem = serializers.IntegerField()  # 과제에 할당된 전체 문제 수 (꼬리 질문 미포함)
    solved_problem = serializers.IntegerField()  # 과제에 할당된 문제 중 푼 문제 수 (꼬리 질문 미포함)
    progress = serializers.FloatField()  # 진행률 (푼 문제 비율)
