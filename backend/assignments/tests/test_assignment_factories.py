from datetime import timedelta

import factory
from assignments.models import Assignment, Material
from django.contrib.auth import get_user_model
from django.utils import timezone
from factory.django import DjangoModelFactory

Account = get_user_model()


class AssignmentFactory(DjangoModelFactory):
    class Meta:
        model = Assignment

    course_class = factory.SubFactory("courses.tests.test_factories.CourseClassFactory")
    title = factory.Sequence(lambda n: f"Assignment {n}")
    description = factory.Faker("text", max_nb_chars=200)
    due_at = factory.LazyFunction(lambda: timezone.now() + timedelta(days=7))


class MaterialFactory(DjangoModelFactory):
    class Meta:
        model = Material

    assignment = factory.SubFactory(AssignmentFactory)
    kind = Material.Kind.PDF
    s3_key = factory.Sequence(lambda n: f"pdf/test/{n}/material.pdf")
    bytes = 1024
