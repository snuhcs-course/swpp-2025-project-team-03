from datetime import timedelta

import pytest
from assignments.models import Assignment
from catalog.models import Subject
from courses.models import CourseClass
from django.contrib.auth import get_user_model
from django.utils import timezone
from questions.models import Question
from submissions.models import PersonalAssignment

Account = get_user_model()

pytestmark = pytest.mark.django_db


class TestQuestionModel:
    """Question 모델 테스트"""

    def test_question_str(self):
        """Question __str__ 메서드 테스트"""
        teacher = Account.objects.create_user(
            email="teacher@test.com",
            password="testpass123",
            is_student=False,
        )
        student = Account.objects.create_user(
            email="student@test.com",
            password="testpass123",
            is_student=True,
        )
        subject = Subject.objects.create(name="Math")
        course_class = CourseClass.objects.create(
            teacher=teacher,
            subject=subject,
            name="Math Class",
        )
        assignment = Assignment.objects.create(
            course_class=course_class,
            subject=subject,
            title="Test Assignment",
            due_at=timezone.now() + timedelta(days=7),
        )
        personal_assignment = PersonalAssignment.objects.create(
            student=student,
            assignment=assignment,
            status=PersonalAssignment.Status.NOT_STARTED,
        )
        question = Question.objects.create(
            personal_assignment=personal_assignment,
            number=1,
            content="What is 2+2?",
            model_answer="4",
            difficulty="easy",
            recalled_num=0,
        )

        assert str(question) == f"Q1 of {personal_assignment}"
