import types

import pytest
from questions.serializers import TailQuestionSerializer


class Obj:
    pass


@pytest.mark.parametrize(
    "number,recalled_num,expected",
    [
        (3, 0, "3"),
        (5, 2, "5-2"),
    ],
)
def test_tail_question_serializer_number_str(number, recalled_num, expected):
    # Prepare a minimal object that exposes attributes accessed by the serializer
    o = types.SimpleNamespace()
    o.is_correct = True
    o.number = number
    o.recalled_num = recalled_num
    o.tail_question = {
        "id": 123,
        "number": number,
        "question": "What is 2+2?",
        "answer": "4",
        "explanation": "Basic addition.",
        "difficulty": "medium",
    }

    ser = TailQuestionSerializer(o)
    data = ser.data

    assert data["is_correct"] is True
    assert data["number_str"] == expected
    assert data["tail_question"]["id"] == 123
    assert data["tail_question"]["number"] == number
    assert data["tail_question"]["question"] == "What is 2+2?"
    assert data["tail_question"]["answer"] == "4"
    assert data["tail_question"]["explanation"] == "Basic addition."
    assert data["tail_question"]["difficulty"] == "medium"


def test_tail_question_serializer_allows_null_tail_question():
    o = types.SimpleNamespace()
    o.is_correct = False
    o.number = 7
    o.recalled_num = 1
    o.tail_question = None

    ser = TailQuestionSerializer(o)
    data = ser.data

    assert data["is_correct"] is False
    # recalled_num != 0 -> include suffix
    assert data["number_str"] == "7-1"
    assert data["tail_question"] is None
