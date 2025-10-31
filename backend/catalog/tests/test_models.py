import pytest
from catalog.models import Subject

pytestmark = pytest.mark.django_db


class TestSubjectModel:
    """Subject 모델 테스트"""

    def test_subject_str(self):
        """Subject __str__ 메서드 테스트"""
        subject = Subject.objects.create(name="Mathematics")
        assert str(subject) == "Mathematics"
