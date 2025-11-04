import factory
from catalog.models import Subject
from courses.models import CourseClass, Enrollment
from django.contrib.auth import get_user_model

Account = get_user_model()


class SubjectFactory(factory.django.DjangoModelFactory):
    class Meta:
        model = Subject

    name = factory.Sequence(lambda n: f"Subject {n}")


class TeacherFactory(factory.django.DjangoModelFactory):
    class Meta:
        model = Account

    email = factory.Sequence(lambda n: f"teacher{n}@example.com")
    display_name = factory.Sequence(lambda n: f"Teacher {n}")
    is_student = False
    is_active = True


class StudentFactory(factory.django.DjangoModelFactory):
    class Meta:
        model = Account

    email = factory.Sequence(lambda n: f"student{n}@example.com")
    display_name = factory.Sequence(lambda n: f"Student {n}")
    is_student = True
    is_active = True


class CourseClassFactory(factory.django.DjangoModelFactory):
    class Meta:
        model = CourseClass

    name = factory.Sequence(lambda n: f"Class {n}")
    description = factory.Faker("text", max_nb_chars=200)
    teacher = factory.SubFactory(TeacherFactory)
    subject = factory.SubFactory(SubjectFactory)
    start_date = factory.Faker("date_time_this_year", before_now=False, after_now=True)
    end_date = factory.Faker("date_time_this_year", before_now=False, after_now=True)


class EnrollmentFactory(factory.django.DjangoModelFactory):
    class Meta:
        model = Enrollment

    student = factory.SubFactory(StudentFactory)
    course_class = factory.SubFactory(CourseClassFactory)
    status = Enrollment.Status.ENROLLED
