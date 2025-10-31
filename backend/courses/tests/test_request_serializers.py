from datetime import timedelta

import pytest
from catalog.models import Subject
from courses.request_serializers import ClassCreateRequestSerializer
from django.contrib.auth import get_user_model
from django.utils import timezone

Account = get_user_model()

pytestmark = pytest.mark.django_db


class TestClassCreateRequestSerializer:
    """ClassCreateRequestSerializer 테스트"""

    def test_create_with_new_subject(self):
        """새로운 subject와 함께 CourseClass 생성 테스트"""
        teacher = Account.objects.create_user(
            email="teacher@test.com",
            password="testpass123",
            is_student=False,
        )

        data = {
            "name": "New Class",
            "description": "Test Description",
            "subject_name": "New Subject",
            "teacher_id": teacher.id,
            "start_date": timezone.now(),
            "end_date": timezone.now() + timedelta(days=30),
        }

        serializer = ClassCreateRequestSerializer(data=data)
        assert serializer.is_valid(raise_exception=True)
        course_class = serializer.save()

        assert course_class.name == "New Class"
        assert course_class.description == "Test Description"
        assert course_class.subject.name == "New Subject"
        assert course_class.teacher == teacher
        assert Subject.objects.filter(name="New Subject").exists()

    def test_create_with_existing_subject(self):
        """기존 subject와 함께 CourseClass 생성 테스트"""
        teacher = Account.objects.create_user(
            email="teacher@test.com",
            password="testpass123",
            is_student=False,
        )
        existing_subject = Subject.objects.create(name="Existing Subject")

        data = {
            "name": "Class with Existing Subject",
            "subject_name": "Existing Subject",
            "teacher_id": teacher.id,
            "start_date": timezone.now(),
            "end_date": timezone.now() + timedelta(days=30),
        }

        serializer = ClassCreateRequestSerializer(data=data)
        assert serializer.is_valid(raise_exception=True)
        course_class = serializer.save()

        assert course_class.subject == existing_subject
        # subject가 중복 생성되지 않았는지 확인
        assert Subject.objects.filter(name="Existing Subject").count() == 1
