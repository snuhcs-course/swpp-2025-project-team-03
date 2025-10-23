from datetime import timedelta

import factory
from accounts.models import Account
from catalog.models import Subject
from courses.models import CourseClass
from django.utils import timezone
from feedbacks.models import TeacherFeedback


class SubjectFactory(factory.django.DjangoModelFactory):
    """Subject 테스트 데이터 생성"""

    class Meta:
        model = Subject

    name = factory.Sequence(lambda n: f"Subject {n}")
    code = factory.Sequence(lambda n: f"SUB{n:03d}")


class TeacherFactory(factory.django.DjangoModelFactory):
    """선생님 테스트 데이터 생성"""

    class Meta:
        model = Account

    email = factory.Sequence(lambda n: f"teacher{n}@example.com")
    display_name = factory.Sequence(lambda n: f"Teacher {n}")
    is_student = False
    is_active = True


class StudentFactory(factory.django.DjangoModelFactory):
    """학생 테스트 데이터 생성"""

    class Meta:
        model = Account

    email = factory.Sequence(lambda n: f"student{n}@example.com")
    display_name = factory.Sequence(lambda n: f"Student {n}")
    is_student = True
    is_active = True


class CourseClassFactory(factory.django.DjangoModelFactory):
    """CourseClass 테스트 데이터 생성"""

    class Meta:
        model = CourseClass

    name = factory.Sequence(lambda n: f"Class {n}")
    description = factory.Sequence(lambda n: f"Description for class {n}")
    teacher = factory.SubFactory(TeacherFactory)
    subject = factory.SubFactory(SubjectFactory)
    start_date = factory.LazyFunction(lambda: timezone.now())
    end_date = factory.LazyFunction(lambda: timezone.now() + timedelta(days=90))


class TeacherFeedbackFactory(factory.django.DjangoModelFactory):
    """TeacherFeedback 테스트 데이터 생성"""

    class Meta:
        model = TeacherFeedback

    course_class = factory.SubFactory(CourseClassFactory)
    student = factory.SubFactory(StudentFactory)
    teacher = factory.SubFactory(TeacherFactory)
    content = factory.Sequence(lambda n: f"Feedback content {n}")
    created_at = factory.LazyFunction(timezone.now)
