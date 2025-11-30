from abc import ABC, abstractmethod

from django.db import transaction

from .models import Question


class QuestionFactory(ABC):
    """Abstract Factory for creating Question objects."""

    @abstractmethod
    def create_question(self, **kwargs) -> Question:
        """Create and return a Question instance."""
        pass


class BaseQuestionFactory(QuestionFactory):
    """Factory for creating base questions (recalled_num=0)."""

    def create_question(self, **kwargs) -> Question:
        """
        Create a base question.
        Required kwargs: personal_assignment, number, content
        Optional kwargs: topic, achievement_code, explanation, model_answer, difficulty
        """
        kwargs["recalled_num"] = 0
        kwargs["base_question"] = None
        return Question.objects.create(**kwargs)


class TailQuestionFactory(QuestionFactory):
    """Factory for creating tail questions (derived from a base question)."""

    def create_question(self, **kwargs) -> Question:
        """
        Create a tail question.
        Required kwargs: base_question, content
        Optional kwargs: explanation, model_answer, difficulty

        The personal_assignment, number, topic, and achievement_code are inherited from the base_question.
        recalled_num is automatically incremented based on existing tail questions.
        """
        base_question = kwargs.pop("base_question", None)
        if not base_question:
            raise ValueError("base_question is required for TailQuestionFactory")

        # Inherit fields from base_question
        kwargs["personal_assignment"] = base_question.personal_assignment
        kwargs["number"] = base_question.number
        kwargs["topic"] = base_question.topic
        kwargs["achievement_code"] = base_question.achievement_code
        kwargs["base_question"] = base_question

        # Use provided recalled_num or calculate next
        if "recalled_num" not in kwargs:
            with transaction.atomic():
                # Calculate next recalled_num
                last_tail = (
                    Question.objects.filter(
                        personal_assignment=base_question.personal_assignment, number=base_question.number
                    )
                    .order_by("-recalled_num")
                    .first()
                )

                next_recalled_num = (last_tail.recalled_num + 1) if last_tail else 1
                kwargs["recalled_num"] = next_recalled_num

        return Question.objects.create(**kwargs)
