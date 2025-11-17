import pytest
from catalog.models import Subject
from courses.models import CourseClass, Enrollment
from django.contrib.auth import get_user_model

Account = get_user_model()

pytestmark = pytest.mark.django_db


class TestEnrollmentModel:
    """Enrollment 모델 테스트"""

    def test_enrollment_str(self):
        """Enrollment __str__ 메서드 테스트"""
        teacher = Account.objects.create_user(
            email="teacher@test.com",
            password="testpass123",
            display_name="Teacher",
            is_student=False,
        )
        student = Account.objects.create_user(
            email="student@test.com",
            password="testpass123",
            display_name="Student Name",
            is_student=True,
        )
        subject = Subject.objects.create(name="Math")
        course_class = CourseClass.objects.create(
            teacher=teacher,
            subject=subject,
            name="Math Class",
        )
        enrollment = Enrollment.objects.create(
            student=student,
            course_class=course_class,
            status=Enrollment.Status.ENROLLED,
        )

        assert str(enrollment) == "Student Name - Math Class"


class TestCourseClassModel:
    """CourseClass 모델 테스트"""

    def test_course_class_str(self):
        """CourseClass __str__ 메서드 테스트"""
        teacher = Account.objects.create_user(
            email="teacher@test.com",
            password="testpass123",
            display_name="Teacher",
            is_student=False,
        )
        subject = Subject.objects.create(name="Mathematics")
        course_class = CourseClass.objects.create(
            teacher=teacher,
            subject=subject,
            name="Math Class",
        )

        assert str(course_class) == "Math Class (Mathematics)"
