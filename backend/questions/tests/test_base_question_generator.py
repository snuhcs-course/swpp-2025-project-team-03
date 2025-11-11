import json
import sys
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

    @patch("questions.utils.base_question_generator.ChatOpenAI")
    def test_generate_quizzes_interpreter_shutdown(self, mock_llm_class):
        """Python 인터프리터 종료 중 예외 테스트 (line 128)"""
        # _exiting 플래그 설정
        original_exiting = getattr(sys, "_exiting", False)
        sys._exiting = True

        try:
            with pytest.raises(RuntimeError) as excinfo:
                generate_base_quizzes("Sample Material", n=1)
            assert "interpreter is shutting down" in str(excinfo.value).lower()
        finally:
            sys._exiting = original_exiting

    @patch("questions.utils.base_question_generator.ChatOpenAI")
    def test_generate_quizzes_unexpected_json_structure(self, mock_llm_class):
        """예상치 못한 JSON 구조 테스트 (line 172)"""
        mock_instance = mock_llm_class.return_value
        # 리스트도 딕셔너리도 아닌 다른 타입
        mock_instance.invoke.return_value = AIMessage(content=json.dumps("invalid"))

        with pytest.raises(ValueError) as excinfo:
            generate_base_quizzes("Sample Material", n=1)
        assert "Unexpected JSON structure" in str(excinfo.value)

    @patch("questions.utils.base_question_generator.ChatOpenAI")
    def test_generate_quizzes_json_decode_error(self, mock_llm_class):
        """JSON 파싱 오류 테스트 (line 183-190)"""
        mock_instance = mock_llm_class.return_value
        mock_instance.invoke.return_value = AIMessage(content="not valid json {")

        with pytest.raises(ValueError) as excinfo:
            generate_base_quizzes("Sample Material", n=1)
        assert "Invalid json output" in str(excinfo.value)

    @patch("questions.utils.base_question_generator.ChatOpenAI")
    def test_generate_quizzes_exception_during_parsing(self, mock_llm_class):
        """파싱 중 다른 예외 테스트 (line 183-190)"""
        mock_instance = mock_llm_class.return_value
        mock_instance.invoke.return_value = AIMessage(content='{"valid": "json"}')

        # json.loads를 패치하여 예외 발생
        with patch("questions.utils.base_question_generator.json.loads", side_effect=Exception("Parsing error")):
            with pytest.raises(Exception) as excinfo:
                generate_base_quizzes("Sample Material", n=1)
            assert "Parsing error" in str(excinfo.value)

    @patch("questions.utils.base_question_generator.ChatOpenAI")
    def test_generate_quizzes_runtime_error_retry(self, mock_llm_class):
        """RuntimeError 재시도 테스트 (line 193-201)"""
        mock_instance = mock_llm_class.return_value
        # 첫 번째 호출에서 RuntimeError 발생, 두 번째에서 성공
        mock_instance.invoke.side_effect = [
            RuntimeError("interpreter shutdown"),
            AIMessage(
                content=json.dumps(
                    [{"topic": "test", "question": "q", "model_answer": "a", "explanation": "e", "difficulty": "easy"}]
                )
            ),
        ]

        with patch("questions.utils.base_question_generator.time.sleep"):  # sleep 패치하여 테스트 속도 향상
            quizzes = generate_base_quizzes("Sample Material", n=1)
            assert len(quizzes) == 1
            assert mock_instance.invoke.call_count == 2

    @patch("questions.utils.base_question_generator.ChatOpenAI")
    def test_generate_quizzes_runtime_error_max_retries(self, mock_llm_class):
        """RuntimeError 최대 재시도 후 실패 테스트 (line 193-201)"""
        mock_instance = mock_llm_class.return_value
        mock_instance.invoke.side_effect = RuntimeError("interpreter shutdown")

        with patch("questions.utils.base_question_generator.time.sleep"):  # sleep 패치
            with pytest.raises(RuntimeError) as excinfo:
                generate_base_quizzes("Sample Material", n=1)
            assert "interrupted after" in str(excinfo.value) or "interpreter shutdown" in str(excinfo.value)
            # 3번 재시도
            assert mock_instance.invoke.call_count == 3

    @patch("questions.utils.base_question_generator.ChatOpenAI")
    def test_generate_quizzes_timeout_retry(self, mock_llm_class):
        """타임아웃 재시도 테스트 (line 208-210)"""
        mock_instance = mock_llm_class.return_value
        # 첫 번째 호출에서 타임아웃, 두 번째에서 성공
        mock_instance.invoke.side_effect = [
            Exception("timeout error"),
            AIMessage(
                content=json.dumps(
                    [{"topic": "test", "question": "q", "model_answer": "a", "explanation": "e", "difficulty": "easy"}]
                )
            ),
        ]

        with patch("questions.utils.base_question_generator.time.sleep"):
            quizzes = generate_base_quizzes("Sample Material", n=1)
            assert len(quizzes) == 1
            assert mock_instance.invoke.call_count == 2

    @patch("questions.utils.base_question_generator.ChatOpenAI")
    def test_generate_quizzes_all_retries_failed(self, mock_llm_class):
        """모든 재시도 실패 후 예외 발생 테스트"""
        mock_instance = mock_llm_class.return_value
        # 모든 재시도에서 타임아웃 발생 (timeout, connection, network 중 하나)
        mock_instance.invoke.side_effect = Exception("timeout error")

        with patch("questions.utils.base_question_generator.time.sleep"):
            # 재시도 로직에 의해 모든 재시도가 실패하면 원래 예외가 다시 발생 (line 212)
            # line 215는 실제로 도달하지 않지만, 재시도 로직이 제대로 작동하는지 확인
            with pytest.raises(Exception) as excinfo:
                generate_base_quizzes("Sample Material", n=1)
            # 재시도 후 실패하면 원래 예외가 재발생
            assert "timeout error" in str(excinfo.value)
            # 3번 재시도 (max_retries = 3)
            assert mock_instance.invoke.call_count == 3
