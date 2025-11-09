import json
from unittest.mock import patch

import pytest
from langchain_core.exceptions import OutputParserException
from langchain_core.messages import AIMessage
from questions.utils.base_question_generator import generate_base_quizzes


@pytest.mark.django_db
class TestBaseQuestionGenerator:
    @patch("questions.utils.base_question_generator.ChatOpenAI")
    def test_generate_quizzes_normal(self, mock_llm_class):
        """정상 생성 케이스"""
        mock_instance = mock_llm_class.return_value
        mock_instance.invoke.return_value = AIMessage(
            content=json.dumps(
                {
                    "0": {
                        "topic": "중력",
                        "question": "사과가 떨어지는 이유?",
                        "model_answer": "중력 때문",
                        "explanation": "질량이 있는 모든 물체는 서로 끌어당긴다.",
                        "difficulty": "easy",
                    }
                }
            )
        )

        quizzes = generate_base_quizzes("Sample Material", n=1)

        assert isinstance(quizzes, list)
        assert len(quizzes) == 1

        q = quizzes[0]
        for key in ["question", "model_answer", "explanation", "difficulty"]:
            assert hasattr(q, key) or key in q

        mock_instance.invoke.assert_called_once()

    @patch("questions.utils.base_question_generator.ChatOpenAI")
    def test_generate_quizzes_invalid_json(self, mock_llm_class):
        """잘못된 JSON 응답 → 예외 발생 테스트"""
        mock_instance = mock_llm_class.return_value
        mock_instance.invoke.return_value = AIMessage(content="INVALID_JSON")

        with pytest.raises((OutputParserException, ValueError)) as excinfo:
            generate_base_quizzes("에러 테스트", n=1)

        assert "Invalid json" in str(excinfo.value) or "INVALID_JSON" in str(excinfo.value)
        mock_instance.invoke.assert_called_once()
