from assignments.models import Assignment
from django.contrib.auth import get_user_model
from rest_framework import serializers

from .models import PersonalAssignment


class AssignmentInfoSerializer(serializers.ModelSerializer):
    """과제 정보 serializer"""

    class Meta:
        model = Assignment
        fields = ["id", "title", "description", "total_questions", "due_at", "grade"]


class StudentInfoSerializer(serializers.ModelSerializer):
    """학생 정보 serializer"""

    class Meta:
        model = get_user_model()
        fields = ["id", "email", "display_name"]


class PersonalAssignmentSerializer(serializers.ModelSerializer):
    """개인 과제 정보를 위한 serializer"""

    student = StudentInfoSerializer()
    assignment = AssignmentInfoSerializer()

    class Meta:
        model = PersonalAssignment
        fields = ["id", "student", "assignment", "status", "solved_num", "started_at", "submitted_at"]


class PersonalAssignmentStatisticsSerializer(serializers.Serializer):
    """개인 과제 통계 정보를 위한 serializer"""

    total_questions = serializers.IntegerField()  # 전체 질문 수 (꼬리 질문 포함)
    answered_questions = serializers.IntegerField()  # 답변한 질문 수 (꼬리 질문 포함)
    correct_answers = serializers.IntegerField()  # 정답인 답변 수 (꼬리 질문 포함)
    accuracy = serializers.FloatField()  # 정확도 (정답 비율)
    total_problem = serializers.IntegerField()  # 과제에 할당된 전체 문제 수 (꼬리 질문 미포함)
    solved_problem = serializers.IntegerField()  # 과제에 할당된 문제 중 푼 문제 수 (꼬리 질문 미포함)
    progress = serializers.FloatField()  # 진행률 (푼 문제 비율)
    average_score = serializers.FloatField()  # 평균 점수 (꼬리 질문 포함)


class AnswerCorrectnessSerializer(serializers.Serializer):
    """학생 답변 정보를 위한 serializer"""

    question_content = serializers.CharField(help_text="질문 내용")
    question_model_answer = serializers.CharField(help_text="모범 답안 텍스트")
    student_answer = serializers.CharField(help_text="학생의 답변 텍스트")
    is_correct = serializers.BooleanField(help_text="학생의 답변이 정답인지 여부")
    answered_at = serializers.DateTimeField(help_text="답변한 시각")
    question_number = serializers.CharField(help_text="질문 번호 (꼬리 질문인 경우 '-recalled_num' 포함)")
    explanation = serializers.CharField(help_text="질문 해설")


class PersonalAssignmentRecentSerializer(serializers.Serializer):
    """최근 답변한 개인 과제 정보를 위한 serializer"""

    personal_assignment_id = serializers.IntegerField(help_text="개인 과제 ID")
