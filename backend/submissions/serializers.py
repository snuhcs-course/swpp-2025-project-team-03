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
