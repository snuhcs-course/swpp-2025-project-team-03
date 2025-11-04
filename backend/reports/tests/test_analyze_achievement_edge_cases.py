from unittest.mock import Mock, patch

import pytest
from reports.utils.analyze_achievement import parse_curriculum

pytestmark = pytest.mark.django_db


class TestAnalyzeAchievementEdgeCases:
    """analyze_achievement.py의 edge case 테스트"""

    @patch("reports.utils.analyze_achievement.calculate_statistics")
    @patch("reports.utils.analyze_achievement.Question.objects")
    def test_parse_curriculum_no_school_level(self, mock_question_objects, mock_calc_stats):
        """학교 단계를 결정할 수 없는 경우 테스트"""
        # grade에 "초", "중", "고"가 없는 경우
        mock_question = Mock()
        mock_question.id = 1
        mock_question.achievement_code = None
        mock_question.personal_assignment.assignment.subject.name = "Math"
        mock_question.personal_assignment.assignment.grade = "Unknown"
        mock_question.content = "Test question"

        mock_questions = [mock_question]
        mock_question_qs = Mock()
        mock_question_qs.__iter__ = Mock(return_value=iter(mock_questions))
        mock_question_qs.select_related.return_value = mock_question_qs
        mock_question_objects.filter.return_value = mock_question_qs

        mock_calc_stats.return_value = {"total_questions": 0}

        # parse_curriculum 호출 시 warning이 출력되지만 에러는 발생하지 않아야 함
        with patch("builtins.print") as mock_print:
            result = parse_curriculum(student_id=1, class_id=1)
            # Warning 메시지가 출력되었는지 확인
            assert any("Could not determine school level" in str(call) for call in mock_print.call_args_list)

    @patch("reports.utils.analyze_achievement.calculate_statistics")
    @patch("reports.utils.analyze_achievement.find_best_achievement_code")
    @patch("reports.utils.analyze_achievement.csv.DictReader")
    @patch("reports.utils.analyze_achievement.open")
    @patch("reports.utils.analyze_achievement.Question.objects")
    def test_parse_curriculum_high_school_grade(
        self, mock_question_objects, mock_open, mock_csv_reader, mock_find_code, mock_calc_stats
    ):
        """고등학교 학년 테스트 (line 79 커버)"""
        mock_question = Mock()
        mock_question.id = 1
        mock_question.achievement_code = None
        mock_question.personal_assignment.assignment.subject.name = "수학"
        mock_question.personal_assignment.assignment.grade = "고등학교 1학년"
        mock_question.content = "테스트 질문"
        mock_question.save = Mock()

        mock_questions = [mock_question]
        mock_question_qs = Mock()
        mock_question_qs.__iter__ = Mock(return_value=iter(mock_questions))
        mock_question_qs.select_related.return_value = mock_question_qs
        mock_question_objects.filter.return_value = mock_question_qs

        mock_csv_data = [
            {
                "subject": "수학",
                "school": "고등학교",
                "code": "수1-01",
                "content": "고등학교 수학 성취기준",
                "grade": "1학년",
            }
        ]
        mock_csv_reader.return_value = mock_csv_data
        mock_file = Mock()
        mock_open.return_value.__enter__.return_value = mock_file

        mock_find_code.return_value = "수1-01"
        mock_calc_stats.return_value = {
            "total_questions": 0,
            "total_correct": 0,
            "overall_accuracy": 0.0,
            "achievement_statistics": {},
        }

        result = parse_curriculum(student_id=1, class_id=1)

        assert result == mock_calc_stats.return_value
        mock_find_code.assert_called_once()
        mock_question.save.assert_called_once()

    @patch("reports.utils.analyze_achievement.calculate_statistics")
    @patch("reports.utils.analyze_achievement.csv.DictReader")
    @patch("reports.utils.analyze_achievement.open")
    @patch("reports.utils.analyze_achievement.Question.objects")
    def test_parse_curriculum_no_relevant_standards(
        self, mock_question_objects, mock_open, mock_csv_reader, mock_calc_stats
    ):
        """관련 성취기준을 찾을 수 없는 경우 테스트"""
        mock_question = Mock()
        mock_question.id = 1
        mock_question.achievement_code = None
        mock_question.personal_assignment.assignment.subject.name = "UnknownSubject"
        mock_question.personal_assignment.assignment.grade = "초등학교 1학년"
        mock_question.content = "Test question"

        mock_questions = [mock_question]
        mock_question_qs = Mock()
        mock_question_qs.__iter__ = Mock(return_value=iter(mock_questions))
        mock_question_qs.select_related.return_value = mock_question_qs
        mock_question_objects.filter.return_value = mock_question_qs

        # CSV에 없는 과목인 경우
        mock_csv_reader.return_value = []
        mock_file = Mock()
        mock_open.return_value.__enter__.return_value = mock_file

        mock_calc_stats.return_value = {"total_questions": 0}

        with patch("builtins.print") as mock_print:
            result = parse_curriculum(student_id=1, class_id=1)
            # Warning 메시지가 출력되었는지 확인
            assert any("No achievement standards found" in str(call) for call in mock_print.call_args_list)

    @patch("reports.utils.analyze_achievement.calculate_statistics")
    @patch("reports.utils.analyze_achievement.find_best_achievement_code")
    @patch("reports.utils.analyze_achievement.csv.DictReader")
    @patch("reports.utils.analyze_achievement.open")
    @patch("reports.utils.analyze_achievement.Question.objects")
    def test_parse_curriculum_no_best_code(
        self, mock_question_objects, mock_open, mock_csv_reader, mock_find_code, mock_calc_stats
    ):
        """가장 적합한 성취기준을 찾을 수 없는 경우 테스트"""
        mock_question = Mock()
        mock_question.id = 1
        mock_question.achievement_code = None
        mock_question.personal_assignment.assignment.subject.name = "Math"
        mock_question.personal_assignment.assignment.grade = "초등학교 1학년"
        mock_question.content = "Test question"

        mock_questions = [mock_question]
        mock_question_qs = Mock()
        mock_question_qs.__iter__ = Mock(return_value=iter(mock_questions))
        mock_question_qs.select_related.return_value = mock_question_qs
        mock_question_objects.filter.return_value = mock_question_qs

        # find_best_achievement_code가 None을 반환하는 경우
        mock_find_code.return_value = None

        mock_csv_data = [{"subject": "Math", "school": "초등학교", "code": "M-1-1", "content": "Test", "grade": "1"}]
        mock_csv_reader.return_value = mock_csv_data
        mock_file = Mock()
        mock_open.return_value.__enter__.return_value = mock_file

        mock_calc_stats.return_value = {"total_questions": 0}

        with patch("builtins.print") as mock_print:
            result = parse_curriculum(student_id=1, class_id=1)
            # Warning 메시지가 출력되었는지 확인
            assert any("Could not determine best achievement code" in str(call) for call in mock_print.call_args_list)

    @patch("reports.utils.analyze_achievement.requests.post")
    def test_find_best_achievement_code_request_exception(self, mock_post):
        """GPT API 요청 예외 처리 테스트"""
        import requests
        from reports.utils.analyze_achievement import find_best_achievement_code

        # RequestException 발생
        mock_post.side_effect = requests.exceptions.RequestException("Connection error")

        result = find_best_achievement_code("Test question", [{"code": "M-1-1", "content": "Test", "grade": "1"}])

        assert result is None

    @patch("reports.utils.analyze_achievement.requests.post")
    def test_find_best_achievement_code_general_exception(self, mock_post):
        """GPT API 처리 중 일반 예외 처리 테스트"""
        from reports.utils.analyze_achievement import find_best_achievement_code

        # 일반 예외 발생
        mock_post.side_effect = Exception("Unexpected error")

        result = find_best_achievement_code("Test question", [{"code": "M-1-1", "content": "Test", "grade": "1"}])

        assert result is None

    @patch("reports.utils.analyze_achievement.calculate_statistics")
    @patch("reports.utils.analyze_achievement.Question.objects")
    def test_parse_curriculum_exception_in_question_processing(self, mock_question_objects, mock_calc_stats):
        """질문 처리 중 예외 발생 테스트"""
        mock_question = Mock()
        mock_question.id = 1
        mock_question.achievement_code = None
        # 속성 접근 시 예외 발생
        type(mock_question.personal_assignment.assignment.subject).name = Mock(side_effect=Exception("Database error"))

        mock_questions = [mock_question]
        mock_question_qs = Mock()
        mock_question_qs.__iter__ = Mock(return_value=iter(mock_questions))
        mock_question_qs.select_related.return_value = mock_question_qs
        mock_question_objects.filter.return_value = mock_question_qs

        mock_calc_stats.return_value = {"total_questions": 0}

        # 예외가 발생해도 처리되어야 함
        with patch("builtins.print") as mock_print:
            result = parse_curriculum(student_id=1, class_id=1)
            # 에러 메시지가 출력되었는지 확인
            assert any("Error processing question" in str(call) for call in mock_print.call_args_list)
