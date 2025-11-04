"""
Reports 순수 유닛 테스트
- DB 접근 없음
- 모든 의존성을 Mock으로 대체
- 단일 컴포넌트(View 로직)만 검증
"""

from unittest.mock import Mock, patch

from reports.views import CurriculumAnalysisView
from rest_framework import status

# pytest 실행 예시
# pytest reports/tests/test_reports_unit.py -v


class TestCurriculumAnalysisViewUnit:
    """CurriculumAnalysisView 단위 테스트"""

    @patch("reports.views.parse_curriculum")
    @patch("reports.views.CurriculumAnalysisSerializer")
    @patch("reports.views.logger")
    def test_get_success(self, mock_logger, mock_serializer_class, mock_parse_curriculum):
        """성공적인 GET 요청 처리 로직 검증"""
        # Mock parse_curriculum
        mock_statistics = {
            "total_questions": 1,
            "total_correct": 1,
            "overall_accuracy": 100.0,
            "achievement_statistics": {
                "4과01-01": {
                    "total_questions": 1,
                    "correct_questions": 1,
                    "accuracy": 100.0,
                    "content": "테스트 성취기준 내용",
                }
            },
        }
        mock_parse_curriculum.return_value = mock_statistics

        # Mock Response Serializer
        mock_serializer = Mock()
        mock_serializer.is_valid.return_value = True
        mock_serializer.validated_data = mock_statistics
        mock_serializer_class.return_value = mock_serializer

        # View 실행
        view = CurriculumAnalysisView()
        mock_request = Mock()
        response = view.get(mock_request, class_id=1, student_id=2)

        # 검증
        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert response.data["data"] == mock_statistics
        assert response.data["message"] == "성취기준 분석이 완료되었습니다."
        assert response.data["error"] is None

        # 함수 호출 확인
        mock_parse_curriculum.assert_called_once_with(2, 1)
        mock_serializer_class.assert_called_once_with(data=mock_statistics)
        mock_logger.info.assert_called()

    def test_get_invalid_class_id_zero(self):
        """클래스 ID가 0인 경우 처리 로직 검증"""
        view = CurriculumAnalysisView()
        mock_request = Mock()
        response = view.get(mock_request, class_id=0, student_id=1)

        assert response.status_code == status.HTTP_400_BAD_REQUEST
        assert response.data["success"] is False
        assert "클래스 ID는 양수여야 합니다" in response.data["message"]
        assert response.data["error"] == "Invalid class_id"

    def test_get_invalid_class_id_negative(self):
        """클래스 ID가 음수인 경우 처리 로직 검증"""
        view = CurriculumAnalysisView()
        mock_request = Mock()
        response = view.get(mock_request, class_id=-1, student_id=1)

        assert response.status_code == status.HTTP_400_BAD_REQUEST
        assert response.data["success"] is False
        assert "클래스 ID는 양수여야 합니다" in response.data["message"]

    def test_get_invalid_student_id_zero(self):
        """학생 ID가 0인 경우 처리 로직 검증"""
        view = CurriculumAnalysisView()
        mock_request = Mock()
        response = view.get(mock_request, class_id=1, student_id=0)

        assert response.status_code == status.HTTP_400_BAD_REQUEST
        assert response.data["success"] is False
        assert "학생 ID는 양수여야 합니다" in response.data["message"]
        assert response.data["error"] == "Invalid student_id"

    def test_get_invalid_student_id_negative(self):
        """학생 ID가 음수인 경우 처리 로직 검증"""
        view = CurriculumAnalysisView()
        mock_request = Mock()
        response = view.get(mock_request, class_id=1, student_id=-1)

        assert response.status_code == status.HTTP_400_BAD_REQUEST
        assert response.data["success"] is False
        assert "학생 ID는 양수여야 합니다" in response.data["message"]

    @patch("reports.views.parse_curriculum")
    @patch("reports.views.CurriculumAnalysisSerializer")
    @patch("reports.views.logger")
    def test_get_parse_curriculum_exception(self, mock_logger, mock_serializer_class, mock_parse_curriculum):
        """parse_curriculum에서 예외 발생 시 처리 로직 검증"""
        # Mock parse_curriculum에서 예외 발생
        mock_parse_curriculum.side_effect = Exception("Database connection failed")

        # View 실행
        view = CurriculumAnalysisView()
        mock_request = Mock()
        response = view.get(mock_request, class_id=1, student_id=2)

        # 검증
        assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
        assert response.data["success"] is False
        assert "성취기준 분석 중 오류가 발생했습니다" in response.data["message"]
        assert "Database connection failed" in response.data["error"]

        # 로깅 확인
        mock_logger.error.assert_called()

    @patch("reports.views.parse_curriculum")
    @patch("reports.views.CurriculumAnalysisSerializer")
    @patch("reports.views.logger")
    def test_get_serializer_validation_failure(self, mock_logger, mock_serializer_class, mock_parse_curriculum):
        """응답 시리얼라이저 검증 실패 시 처리 로직 검증"""
        # Mock parse_curriculum
        mock_statistics = {"invalid": "data"}
        mock_parse_curriculum.return_value = mock_statistics

        # Mock Response Serializer 검증 실패
        mock_serializer = Mock()
        mock_serializer.is_valid.return_value = False
        mock_serializer.errors = {"field": ["This field is required."]}
        mock_serializer_class.return_value = mock_serializer

        # View 실행
        view = CurriculumAnalysisView()
        mock_request = Mock()
        response = view.get(mock_request, class_id=1, student_id=2)

        # 검증
        assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
        assert response.data["success"] is False
        assert "응답 데이터 생성 중 오류가 발생했습니다" in response.data["message"]
        assert response.data["error"] == "Internal server error"

        # 로깅 확인
        mock_logger.error.assert_called()

    @patch("reports.views.parse_curriculum")
    @patch("reports.views.CurriculumAnalysisSerializer")
    @patch("reports.views.logger")
    def test_get_logging_behavior(self, mock_logger, mock_serializer_class, mock_parse_curriculum):
        """로깅 동작 검증"""
        # Mock 설정
        mock_statistics = {
            "total_questions": 1,
            "total_correct": 1,
            "overall_accuracy": 100.0,
            "achievement_statistics": {},
        }
        mock_parse_curriculum.return_value = mock_statistics

        mock_serializer = Mock()
        mock_serializer.is_valid.return_value = True
        mock_serializer.validated_data = mock_statistics
        mock_serializer_class.return_value = mock_serializer

        # View 실행
        view = CurriculumAnalysisView()
        mock_request = Mock()
        response = view.get(mock_request, class_id=1, student_id=2)

        # 로깅 호출 확인
        assert mock_logger.info.call_count == 2  # 요청 시작과 완료
        mock_logger.info.assert_any_call("성취기준 분석 요청: student_id=2, class_id=1")
        mock_logger.info.assert_any_call("성취기준 분석 완료: student_id=2, class_id=1")

    @patch("reports.views.parse_curriculum")
    @patch("reports.views.CurriculumAnalysisSerializer")
    def test_get_response_data_structure(self, mock_serializer_class, mock_parse_curriculum):
        """응답 데이터 구조 검증"""
        # Mock 설정
        mock_statistics = {
            "total_questions": 3,
            "total_correct": 2,
            "overall_accuracy": 66.7,
            "achievement_statistics": {
                "4과01-01": {
                    "total_questions": 2,
                    "correct_questions": 1,
                    "accuracy": 50.0,
                    "content": "테스트 내용",
                }
            },
        }
        mock_parse_curriculum.return_value = mock_statistics

        mock_serializer = Mock()
        mock_serializer.is_valid.return_value = True
        mock_serializer.validated_data = mock_statistics
        mock_serializer_class.return_value = mock_serializer

        # View 실행
        view = CurriculumAnalysisView()
        mock_request = Mock()
        response = view.get(mock_request, class_id=1, student_id=2)

        # 응답 구조 검증
        assert "success" in response.data
        assert "data" in response.data
        assert "message" in response.data
        assert "error" in response.data

        # data 필드 검증
        data = response.data["data"]
        assert data["total_questions"] == 3
        assert data["total_correct"] == 2
        assert data["overall_accuracy"] == 66.7
        assert "4과01-01" in data["achievement_statistics"]

    @patch("reports.views.parse_curriculum")
    def test_get_parameter_passing(self, mock_parse_curriculum):
        """파라미터 전달 검증"""
        mock_statistics = {
            "total_questions": 0,
            "total_correct": 0,
            "overall_accuracy": 0.0,
            "achievement_statistics": {},
        }
        mock_parse_curriculum.return_value = mock_statistics

        view = CurriculumAnalysisView()
        mock_request = Mock()

        # 다양한 파라미터로 테스트
        test_cases = [
            (1, 1),
            (10, 20),
            (999, 888),
        ]

        for class_id, student_id in test_cases:
            view.get(mock_request, class_id=class_id, student_id=student_id)
            mock_parse_curriculum.assert_called_with(student_id, class_id)
            mock_parse_curriculum.reset_mock()

    def test_get_edge_case_large_numbers(self):
        """큰 숫자 파라미터 처리 검증"""
        view = CurriculumAnalysisView()
        mock_request = Mock()

        # 큰 숫자도 양수이므로 통과해야 함
        response = view.get(mock_request, class_id=999999, student_id=888888)

        # URL 파라미터 검증은 통과하므로 200 또는 500 (parse_curriculum 결과에 따라)
        assert response.status_code in [status.HTTP_200_OK, status.HTTP_500_INTERNAL_SERVER_ERROR]
