"""
parse_curriculum 함수 유닛 테스트
- DB 접근 없음
- 모든 의존성을 Mock으로 대체
- 함수 로직만 검증
"""

from unittest.mock import Mock, patch

from reports.utils.analyze_achievement import calculate_statistics, find_best_achievement_code, parse_curriculum

# pytest 실행 예시
# pytest reports/tests/test_parse_curriculum_unit.py -v


class TestParseCurriculumUnit:
    """parse_curriculum 함수 단위 테스트"""

    @patch("reports.utils.analyze_achievement.calculate_statistics")
    @patch("reports.utils.analyze_achievement.find_best_achievement_code")
    @patch("reports.utils.analyze_achievement.csv.DictReader")
    @patch("reports.utils.analyze_achievement.open")
    @patch("reports.utils.analyze_achievement.Question.objects")
    def test_parse_curriculum_success(
        self, mock_question_objects, mock_open, mock_csv_reader, mock_find_code, mock_calculate_stats
    ):
        """parse_curriculum 성공 케이스 테스트"""
        # Mock Question QuerySet
        mock_question = Mock()
        mock_question.id = 1
        mock_question.content = "테스트 질문"
        mock_question.personal_assignment.assignment.subject.name = "과학"
        mock_question.personal_assignment.assignment.grade = "중학교 2학년"

        mock_question_qs = Mock()
        mock_question_qs.count.return_value = 1
        mock_question_qs.__iter__ = Mock(return_value=iter([mock_question]))
        mock_question_qs.select_related.return_value = mock_question_qs
        mock_question_objects.filter.return_value = mock_question_qs

        # Mock CSV 파일
        mock_csv_data = [
            {"subject": "과학", "school": "중학교", "code": "4과01-01", "content": "테스트 성취기준", "grade": "2학년"}
        ]
        mock_csv_reader.return_value = mock_csv_data
        mock_file = Mock()
        mock_open.return_value.__enter__.return_value = mock_file

        # Mock find_best_achievement_code
        mock_find_code.return_value = "4과01-01"

        # Mock calculate_statistics
        mock_stats = {
            "total_questions": 1,
            "total_correct": 1,
            "overall_accuracy": 100.0,
            "achievement_statistics": {
                "4과01-01": {
                    "total_questions": 1,
                    "correct_questions": 1,
                    "accuracy": 100.0,
                    "content": "테스트 성취기준",
                }
            },
        }
        mock_calculate_stats.return_value = mock_stats

        # 함수 실행
        result = parse_curriculum(student_id=1, class_id=1)

        # 검증
        assert result == mock_stats
        mock_question_objects.filter.assert_called_once()
        mock_calculate_stats.assert_called_once_with(1, 1)

    @patch("reports.utils.analyze_achievement.calculate_statistics")
    @patch("reports.utils.analyze_achievement.Question.objects")
    def test_parse_curriculum_no_questions(self, mock_question_objects, mock_calculate_stats):
        """질문이 없는 경우 테스트"""
        # Mock Question QuerySet (빈 결과)
        mock_question_qs = Mock()
        mock_question_qs.count.return_value = 0
        mock_question_qs.select_related.return_value = mock_question_qs
        mock_question_qs.__iter__ = Mock(return_value=iter([]))  # 빈 이터레이터
        mock_question_objects.filter.return_value = mock_question_qs

        # Mock calculate_statistics
        mock_stats = {"total_questions": 0, "total_correct": 0, "overall_accuracy": 0.0, "achievement_statistics": {}}
        mock_calculate_stats.return_value = mock_stats

        # 함수 실행
        result = parse_curriculum(student_id=1, class_id=1)

        # 검증
        assert result == mock_stats
        mock_calculate_stats.assert_called_once_with(1, 1)

    @patch("reports.utils.analyze_achievement.calculate_statistics")
    @patch("reports.utils.analyze_achievement.find_best_achievement_code")
    @patch("reports.utils.analyze_achievement.csv.DictReader")
    @patch("reports.utils.analyze_achievement.open")
    @patch("reports.utils.analyze_achievement.Question.objects")
    def test_parse_curriculum_question_processing_error(
        self, mock_question_objects, mock_open, mock_csv_reader, mock_find_code, mock_calculate_stats
    ):
        """질문 처리 중 오류 발생 시 테스트"""
        # Mock Question QuerySet
        mock_question = Mock()
        mock_question.id = 1
        mock_question.content = "테스트 질문"

        # Mock 속성 체인 설정
        mock_personal_assignment = Mock()
        mock_assignment = Mock()
        mock_subject = Mock()
        mock_subject.name.side_effect = Exception("DB Error")  # 예외 발생하도록 설정
        mock_assignment.subject = mock_subject
        mock_assignment.grade = "중학교 2학년"
        mock_personal_assignment.assignment = mock_assignment
        mock_question.personal_assignment = mock_personal_assignment

        mock_question_qs = Mock()
        mock_question_qs.count.return_value = 1
        mock_question_qs.__iter__ = Mock(return_value=iter([mock_question]))
        mock_question_qs.select_related.return_value = mock_question_qs
        mock_question_objects.filter.return_value = mock_question_qs

        # Mock CSV 파일
        mock_csv_data = [
            {"subject": "과학", "school": "중학교", "code": "4과01-01", "content": "테스트 성취기준", "grade": "2학년"}
        ]
        mock_csv_reader.return_value = mock_csv_data
        mock_file = Mock()
        mock_open.return_value.__enter__.return_value = mock_file

        # Mock calculate_statistics
        mock_stats = {"total_questions": 0, "total_correct": 0, "overall_accuracy": 0.0, "achievement_statistics": {}}
        mock_calculate_stats.return_value = mock_stats

        # 함수 실행 (예외가 발생해도 계속 진행되어야 함)
        result = parse_curriculum(student_id=1, class_id=1)

        # 검증
        assert result == mock_stats
        mock_calculate_stats.assert_called_once_with(1, 1)


class TestCalculateStatisticsUnit:
    """calculate_statistics 함수 단위 테스트"""

    @patch("reports.utils.analyze_achievement.Answer.objects")
    @patch("reports.utils.analyze_achievement.Question.objects")
    @patch("reports.utils.analyze_achievement.csv.DictReader")
    @patch("reports.utils.analyze_achievement.open")
    def test_calculate_statistics_success(self, mock_open, mock_csv_reader, mock_question_objects, mock_answer_objects):
        """calculate_statistics 성공 케이스 테스트"""
        # Mock Question QuerySet
        mock_question = Mock()
        mock_question.achievement_code = "4과01-01"

        mock_question_qs = Mock()
        mock_question_qs.count.return_value = 1
        mock_question_qs.select_related.return_value = mock_question_qs
        mock_question_objects.filter.return_value = mock_question_qs

        # Mock Answer QuerySet
        mock_answer = Mock()
        mock_answer.question.achievement_code = "4과01-01"
        mock_answer.state = "correct"

        mock_answer_qs = Mock()
        mock_answer_qs.count.return_value = 1
        mock_answer_qs.filter.return_value.count.return_value = 1
        mock_answer_qs.select_related.return_value = mock_answer_qs
        mock_answer_qs.__iter__ = Mock(return_value=iter([mock_answer]))
        mock_answer_objects.filter.return_value = mock_answer_qs

        # Mock CSV 파일
        mock_csv_data = [{"code": "4과01-01", "content": "테스트 성취기준 내용"}]
        mock_csv_reader.return_value = mock_csv_data
        mock_file = Mock()
        mock_open.return_value.__enter__.return_value = mock_file

        # 함수 실행
        result = calculate_statistics(student_id=1, class_id=1)

        # 검증
        assert result["total_questions"] == 1
        assert result["total_correct"] == 1
        assert result["overall_accuracy"] == 100.0
        assert "4과01-01" in result["achievement_statistics"]
        assert result["achievement_statistics"]["4과01-01"]["total_questions"] == 1
        assert result["achievement_statistics"]["4과01-01"]["correct_questions"] == 1
        assert result["achievement_statistics"]["4과01-01"]["accuracy"] == 100.0
        assert result["achievement_statistics"]["4과01-01"]["content"] == "테스트 성취기준 내용"

    @patch("reports.utils.analyze_achievement.Answer.objects")
    @patch("reports.utils.analyze_achievement.Question.objects")
    def test_calculate_statistics_no_answers(self, mock_question_objects, mock_answer_objects):
        """답안이 없는 경우 테스트"""
        # Mock Question QuerySet (빈 결과)
        mock_question_qs = Mock()
        mock_question_qs.count.return_value = 0
        mock_question_qs.select_related.return_value = mock_question_qs
        mock_question_objects.filter.return_value = mock_question_qs

        # Mock Answer QuerySet (빈 결과)
        mock_answer_qs = Mock()
        mock_answer_qs.count.return_value = 0
        mock_answer_qs.filter.return_value.count.return_value = 0
        mock_answer_qs.select_related.return_value = mock_answer_qs
        mock_answer_qs.__iter__ = Mock(return_value=iter([]))
        mock_answer_objects.filter.return_value = mock_answer_qs

        # 함수 실행
        result = calculate_statistics(student_id=1, class_id=1)

        # 검증
        assert result["total_questions"] == 0
        assert result["total_correct"] == 0
        assert result["overall_accuracy"] == 0.0
        assert result["achievement_statistics"] == {}

    @patch("reports.utils.analyze_achievement.Answer.objects")
    @patch("reports.utils.analyze_achievement.Question.objects")
    @patch("reports.utils.analyze_achievement.csv.DictReader")
    @patch("reports.utils.analyze_achievement.open")
    def test_calculate_statistics_multiple_achievements(
        self, mock_open, mock_csv_reader, mock_question_objects, mock_answer_objects
    ):
        """여러 성취기준이 있는 경우 테스트"""
        # Mock Question QuerySet
        mock_question_qs = Mock()
        mock_question_qs.count.return_value = 2
        mock_question_qs.select_related.return_value = mock_question_qs
        mock_question_objects.filter.return_value = mock_question_qs

        # Mock Answer QuerySet
        mock_answer1 = Mock()
        mock_answer1.question.achievement_code = "4과01-01"
        mock_answer1.state = "correct"

        mock_answer2 = Mock()
        mock_answer2.question.achievement_code = "4과01-02"
        mock_answer2.state = "incorrect"

        mock_answer_qs = Mock()
        mock_answer_qs.count.return_value = 2
        mock_answer_qs.filter.return_value.count.return_value = 1  # correct answers
        mock_answer_qs.select_related.return_value = mock_answer_qs
        mock_answer_qs.__iter__ = Mock(return_value=iter([mock_answer1, mock_answer2]))
        mock_answer_objects.filter.return_value = mock_answer_qs

        # Mock CSV 파일
        mock_csv_data = [
            {"code": "4과01-01", "content": "첫 번째 성취기준"},
            {"code": "4과01-02", "content": "두 번째 성취기준"},
        ]
        mock_csv_reader.return_value = mock_csv_data
        mock_file = Mock()
        mock_open.return_value.__enter__.return_value = mock_file

        # 함수 실행
        result = calculate_statistics(student_id=1, class_id=1)

        # 검증
        assert result["total_questions"] == 2
        assert result["total_correct"] == 1
        assert result["overall_accuracy"] == 50.0
        assert len(result["achievement_statistics"]) == 2
        assert "4과01-01" in result["achievement_statistics"]
        assert "4과01-02" in result["achievement_statistics"]
        assert result["achievement_statistics"]["4과01-01"]["accuracy"] == 100.0
        assert result["achievement_statistics"]["4과01-02"]["accuracy"] == 0.0


class TestFindBestAchievementCodeUnit:
    """find_best_achievement_code 함수 단위 테스트"""

    @patch("reports.utils.analyze_achievement.settings")
    @patch("reports.utils.analyze_achievement.requests.post")
    def test_find_best_achievement_code_success(self, mock_requests_post, mock_settings):
        """find_best_achievement_code 성공 케이스 테스트"""
        # Mock settings
        mock_settings.OPENAI_API_KEY = "test-api-key"

        # Mock API 응답
        mock_response = Mock()
        mock_response.status_code = 200
        mock_response.json.return_value = {"choices": [{"message": {"content": "4과01-01"}}]}
        mock_requests_post.return_value = mock_response

        # 테스트 데이터
        question_content = "호흡 운동에 대해 설명하세요"
        achievement_standards = [
            {"code": "4과01-01", "content": "호흡 운동의 원리", "grade": "2학년"},
            {"code": "4과01-02", "content": "배설의 과정", "grade": "2학년"},
        ]

        # 함수 실행
        result = find_best_achievement_code(question_content, achievement_standards)

        # 검증
        assert result == "4과01-01"
        mock_requests_post.assert_called_once()

    @patch("reports.utils.analyze_achievement.settings")
    def test_find_best_achievement_code_no_api_key(self, mock_settings):
        """API 키가 없는 경우 테스트"""
        # Mock settings (API 키 없음)
        mock_settings.OPENAI_API_KEY = None

        # 테스트 데이터
        question_content = "테스트 질문"
        achievement_standards = [{"code": "4과01-01", "content": "테스트 성취기준", "grade": "2학년"}]

        # 함수 실행
        result = find_best_achievement_code(question_content, achievement_standards)

        # 검증
        assert result is None

    @patch("reports.utils.analyze_achievement.settings")
    @patch("reports.utils.analyze_achievement.requests.post")
    def test_find_best_achievement_code_api_error(self, mock_requests_post, mock_settings):
        """API 오류 발생 시 테스트"""
        # Mock settings
        mock_settings.OPENAI_API_KEY = "test-api-key"

        # Mock API 오류 응답
        mock_response = Mock()
        mock_response.status_code = 500
        mock_response.text = "Internal Server Error"
        mock_requests_post.return_value = mock_response

        # 테스트 데이터
        question_content = "테스트 질문"
        achievement_standards = [{"code": "4과01-01", "content": "테스트 성취기준", "grade": "2학년"}]

        # 함수 실행
        result = find_best_achievement_code(question_content, achievement_standards)

        # 검증
        assert result is None

    @patch("reports.utils.analyze_achievement.settings")
    @patch("reports.utils.analyze_achievement.requests.post")
    def test_find_best_achievement_code_invalid_response(self, mock_requests_post, mock_settings):
        """잘못된 API 응답 테스트"""
        # Mock settings
        mock_settings.OPENAI_API_KEY = "test-api-key"

        # Mock API 응답 (잘못된 코드)
        mock_response = Mock()
        mock_response.status_code = 200
        mock_response.json.return_value = {"choices": [{"message": {"content": "INVALID_CODE"}}]}
        mock_requests_post.return_value = mock_response

        # 테스트 데이터
        question_content = "테스트 질문"
        achievement_standards = [{"code": "4과01-01", "content": "테스트 성취기준", "grade": "2학년"}]

        # 함수 실행
        result = find_best_achievement_code(question_content, achievement_standards)

        # 검증
        assert result is None

    @patch("reports.utils.analyze_achievement.settings")
    @patch("reports.utils.analyze_achievement.requests.post")
    def test_find_best_achievement_code_request_exception(self, mock_requests_post, mock_settings):
        """요청 예외 발생 시 테스트"""
        # Mock settings
        mock_settings.OPENAI_API_KEY = "test-api-key"

        # Mock 요청 예외
        mock_requests_post.side_effect = Exception("Network error")

        # 테스트 데이터
        question_content = "테스트 질문"
        achievement_standards = [{"code": "4과01-01", "content": "테스트 성취기준", "grade": "2학년"}]

        # 함수 실행
        result = find_best_achievement_code(question_content, achievement_standards)

        # 검증
        assert result is None
