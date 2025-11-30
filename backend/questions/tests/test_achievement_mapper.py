from unittest.mock import MagicMock, mock_open, patch

from django.test import TestCase, override_settings


class TestLoadAchievementStandards(TestCase):
    """Tests for load_achievement_standards function"""

    def test_load_achievement_standards_success_cp949(self):
        """Test loading CSV with cp949 encoding"""
        csv_content = "code,content,subject,school,grade\n2과03-01,테스트 내용,과학,중학교,1학년"

        with patch("builtins.open", mock_open(read_data=csv_content)):
            from questions.utils.achievement_mapper import load_achievement_standards

            result = load_achievement_standards()
            self.assertEqual(len(result), 1)
            self.assertEqual(result[0]["code"], "2과03-01")

    def test_load_achievement_standards_unicode_decode_error_fallback_to_utf8(self):
        """Test fallback to utf-8 when cp949 fails"""
        csv_content = "code,content,subject,school,grade\n2과03-01,테스트 내용,과학,중학교,1학년"

        mock_file = mock_open(read_data=csv_content)
        call_count = [0]

        def side_effect(*args, **kwargs):
            call_count[0] += 1
            if call_count[0] == 1:  # First call with cp949
                raise UnicodeDecodeError("cp949", b"", 0, 1, "error")
            return mock_file(*args, **kwargs)

        with patch("builtins.open", side_effect=side_effect):
            from questions.utils.achievement_mapper import load_achievement_standards

            result = load_achievement_standards()
            # Should have tried both encodings
            self.assertEqual(call_count[0], 2)

    def test_load_achievement_standards_file_not_found(self):
        """Test handling of FileNotFoundError"""
        with patch("builtins.open", side_effect=FileNotFoundError()):
            from questions.utils.achievement_mapper import load_achievement_standards

            result = load_achievement_standards()
            self.assertEqual(result, [])


class TestGetSchoolLevelFromGrade(TestCase):
    """Tests for get_school_level_from_grade function"""

    def setUp(self):
        from questions.utils.achievement_mapper import get_school_level_from_grade

        self.get_school_level = get_school_level_from_grade

    def test_elementary_school(self):
        """Test elementary school detection"""
        self.assertEqual(self.get_school_level("초등학교 3학년"), "초등학교")
        self.assertEqual(self.get_school_level("초 5학년"), "초등학교")
        self.assertEqual(self.get_school_level("초등 6"), "초등학교")

    def test_middle_school(self):
        """Test middle school detection"""
        self.assertEqual(self.get_school_level("중학교 2학년"), "중학교")
        self.assertEqual(self.get_school_level("중 1학년"), "중학교")
        self.assertEqual(self.get_school_level("중등"), "중학교")

    def test_high_school(self):
        """Test high school detection"""
        self.assertEqual(self.get_school_level("고등학교 1학년"), "고등학교")
        self.assertEqual(self.get_school_level("고 2학년"), "고등학교")
        self.assertEqual(self.get_school_level("고등"), "고등학교")

    def test_none_input(self):
        """Test None input returns None"""
        self.assertIsNone(self.get_school_level(None))

    def test_empty_string(self):
        """Test empty string returns None"""
        self.assertIsNone(self.get_school_level(""))

    def test_invalid_grade(self):
        """Test invalid grade string returns None"""
        self.assertIsNone(self.get_school_level("대학교 1학년"))
        self.assertIsNone(self.get_school_level("유치원"))


class TestFilterRelevantStandards(TestCase):
    """Tests for filter_relevant_standards function"""

    def setUp(self):
        from questions.utils.achievement_mapper import filter_relevant_standards

        self.filter_relevant_standards = filter_relevant_standards

    def test_filter_by_subject_and_school(self):
        """Test filtering by subject and school level"""
        standards = [
            {"code": "1", "subject": "과학", "school": "중학교"},
            {"code": "2", "subject": "과학", "school": "고등학교"},
            {"code": "3", "subject": "수학", "school": "중학교"},
        ]
        result = self.filter_relevant_standards(standards, "과학", "중학교")
        self.assertEqual(len(result), 1)
        self.assertEqual(result[0]["code"], "1")

    def test_filter_no_school_level(self):
        """Test filtering with None school level returns empty list"""
        standards = [{"code": "1", "subject": "과학", "school": "중학교"}]
        result = self.filter_relevant_standards(standards, "과학", None)
        self.assertEqual(result, [])

    def test_filter_no_matching_standards(self):
        """Test filtering with no matching standards"""
        standards = [{"code": "1", "subject": "과학", "school": "중학교"}]
        result = self.filter_relevant_standards(standards, "수학", "중학교")
        self.assertEqual(len(result), 0)

    def test_filter_empty_standards(self):
        """Test filtering empty standards list"""
        result = self.filter_relevant_standards([], "과학", "중학교")
        self.assertEqual(result, [])


class TestFilterStandardsByRoberta(TestCase):
    """Tests for filter_standards_by_roberta function"""

    def test_roberta_filter_success(self):
        """Test successful RoBERTa filtering"""
        standards = [
            {"code": "1", "content": "운동과 에너지"},
            {"code": "2", "content": "전기와 자기"},
        ]

        # Patch where the function is imported from (inside filter_standards_by_roberta)
        with patch("reports.utils.achievement_inference.filter_standards_by_model") as mock_filter:
            mock_filter.return_value = [{"code": "1", "content": "운동과 에너지"}]

            from questions.utils.achievement_mapper import filter_standards_by_roberta

            result = filter_standards_by_roberta("운동에 대한 내용", standards, top_k=10)
            self.assertEqual(len(result), 1)
            self.assertEqual(result[0]["code"], "1")
            mock_filter.assert_called_once_with(
                question_content="운동에 대한 내용",
                achievement_standards=standards,
                top_k=10,
            )

    def test_roberta_filter_exception_fallback(self):
        """Test fallback when RoBERTa model fails"""
        standards = [
            {"code": "1", "content": "운동과 에너지"},
            {"code": "2", "content": "전기와 자기"},
        ]

        # Patch where the function is imported from
        with patch(
            "reports.utils.achievement_inference.filter_standards_by_model", side_effect=Exception("Model error")
        ):
            from questions.utils.achievement_mapper import filter_standards_by_roberta

            result = filter_standards_by_roberta("test", standards, top_k=10)
            # Should return all standards as fallback
            self.assertEqual(len(result), 2)
            self.assertEqual(result, standards)


class TestInferRelevantAchievementCodesFromSummary(TestCase):
    """Tests for infer_relevant_achievement_codes_from_summary function"""

    @override_settings(OPENAI_API_KEY=None)
    def test_no_api_key(self):
        """Test handling when OPENAI_API_KEY is not set"""
        from questions.utils.achievement_mapper import infer_relevant_achievement_codes_from_summary

        result = infer_relevant_achievement_codes_from_summary("summary", "과학", "중학교 1학년")
        self.assertEqual(result, {"codes": [], "details": {}})

    @override_settings(OPENAI_API_KEY="test-key")
    def test_empty_achievement_standards(self):
        """Test handling when achievement standards file is empty"""
        with patch("questions.utils.achievement_mapper.load_achievement_standards", return_value=[]):
            from questions.utils.achievement_mapper import infer_relevant_achievement_codes_from_summary

            result = infer_relevant_achievement_codes_from_summary("summary", "과학", "중학교 1학년")
            self.assertEqual(result, {"codes": [], "details": {}})

    @override_settings(OPENAI_API_KEY="test-key")
    def test_invalid_grade(self):
        """Test handling when grade cannot determine school level"""
        with patch(
            "questions.utils.achievement_mapper.load_achievement_standards",
            return_value=[{"code": "1", "subject": "과학", "school": "중학교"}],
        ):
            from questions.utils.achievement_mapper import infer_relevant_achievement_codes_from_summary

            result = infer_relevant_achievement_codes_from_summary("summary", "과학", "대학교 1학년")
            self.assertEqual(result, {"codes": [], "details": {}})

    @override_settings(OPENAI_API_KEY="test-key")
    def test_no_matching_standards(self):
        """Test handling when no standards match subject/school"""
        with patch(
            "questions.utils.achievement_mapper.load_achievement_standards",
            return_value=[{"code": "1", "subject": "수학", "school": "고등학교"}],
        ):
            from questions.utils.achievement_mapper import infer_relevant_achievement_codes_from_summary

            result = infer_relevant_achievement_codes_from_summary("summary", "과학", "중학교 1학년")
            self.assertEqual(result, {"codes": [], "details": {}})

    @override_settings(OPENAI_API_KEY="test-key")
    def test_successful_inference(self):
        """Test successful achievement code inference"""
        mock_standards = [
            {"code": "2과03-01", "content": "운동과 에너지", "subject": "과학", "school": "중학교", "grade": "2학년"},
            {"code": "2과03-02", "content": "전기와 자기", "subject": "과학", "school": "중학교", "grade": "2학년"},
        ]

        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.return_value = {"choices": [{"message": {"content": '["2과03-01"]'}}]}

        with patch("questions.utils.achievement_mapper.load_achievement_standards", return_value=mock_standards):
            with patch("questions.utils.achievement_mapper.filter_standards_by_roberta", return_value=mock_standards):
                with patch("requests.post", return_value=mock_response) as mock_post:
                    from questions.utils.achievement_mapper import infer_relevant_achievement_codes_from_summary

                    result = infer_relevant_achievement_codes_from_summary(
                        "운동에 대한 학습 내용", "과학", "중학교 2학년"
                    )

                    # Verify result structure and content
                    self.assertIsInstance(result["codes"], list)
                    self.assertEqual(result["codes"], ["2과03-01"])
                    self.assertIn("2과03-01", result["details"])
                    self.assertEqual(result["details"]["2과03-01"], "운동과 에너지")
                    self.assertEqual(len(result["codes"]), 1)

                    # Verify GPT API was called with correct parameters
                    mock_post.assert_called_once()
                    call_args = mock_post.call_args
                    self.assertEqual(call_args[0][0], "https://api.openai.com/v1/chat/completions")

    @override_settings(OPENAI_API_KEY="test-key")
    def test_gpt_returns_json_with_markdown(self):
        """Test handling when GPT returns JSON wrapped in markdown code block"""
        mock_standards = [
            {"code": "2과03-01", "content": "운동과 에너지", "subject": "과학", "school": "중학교", "grade": "2학년"},
        ]

        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.return_value = {"choices": [{"message": {"content": '```json\n["2과03-01"]\n```'}}]}

        with patch("questions.utils.achievement_mapper.load_achievement_standards", return_value=mock_standards):
            with patch("questions.utils.achievement_mapper.filter_standards_by_roberta", return_value=mock_standards):
                with patch("requests.post", return_value=mock_response):
                    from questions.utils.achievement_mapper import infer_relevant_achievement_codes_from_summary

                    result = infer_relevant_achievement_codes_from_summary("summary", "과학", "중학교 2학년")

                    # Verify result structure
                    self.assertIsInstance(result, dict)
                    self.assertIn("codes", result)
                    self.assertIn("details", result)

                    # Verify returned codes
                    self.assertEqual(result["codes"], ["2과03-01"])
                    self.assertEqual(len(result["details"]), 1)
                    self.assertEqual(result["details"]["2과03-01"], "운동과 에너지")

    @override_settings(OPENAI_API_KEY="test-key")
    def test_gpt_returns_non_list(self):
        """Test handling when GPT returns a single code instead of list"""
        mock_standards = [
            {"code": "2과03-01", "content": "운동과 에너지", "subject": "과학", "school": "중학교", "grade": "2학년"},
        ]

        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.return_value = {"choices": [{"message": {"content": '"2과03-01"'}}]}

        with patch("questions.utils.achievement_mapper.load_achievement_standards", return_value=mock_standards):
            with patch("questions.utils.achievement_mapper.filter_standards_by_roberta", return_value=mock_standards):
                with patch("requests.post", return_value=mock_response):
                    from questions.utils.achievement_mapper import infer_relevant_achievement_codes_from_summary

                    result = infer_relevant_achievement_codes_from_summary("summary", "과학", "중학교 2학년")
                    # Should handle non-list by converting to list
                    self.assertIn("2과03-01", result["codes"])

    @override_settings(OPENAI_API_KEY="test-key")
    def test_gpt_returns_invalid_codes(self):
        """Test handling when GPT returns codes not in filtered standards"""
        mock_standards = [
            {"code": "2과03-01", "content": "운동과 에너지", "subject": "과학", "school": "중학교", "grade": "2학년"},
        ]

        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.return_value = {"choices": [{"message": {"content": '["INVALID-CODE"]'}}]}

        with patch("questions.utils.achievement_mapper.load_achievement_standards", return_value=mock_standards):
            with patch("questions.utils.achievement_mapper.filter_standards_by_roberta", return_value=mock_standards):
                with patch("requests.post", return_value=mock_response):
                    from questions.utils.achievement_mapper import infer_relevant_achievement_codes_from_summary

                    result = infer_relevant_achievement_codes_from_summary("summary", "과학", "중학교 2학년")
                    self.assertEqual(result, {"codes": [], "details": {}})

    @override_settings(OPENAI_API_KEY="test-key")
    def test_gpt_returns_invalid_json(self):
        """Test handling when GPT returns invalid JSON"""
        mock_standards = [
            {"code": "2과03-01", "content": "운동과 에너지", "subject": "과학", "school": "중학교", "grade": "2학년"},
        ]

        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.return_value = {"choices": [{"message": {"content": "not valid json at all"}}]}

        with patch("questions.utils.achievement_mapper.load_achievement_standards", return_value=mock_standards):
            with patch("questions.utils.achievement_mapper.filter_standards_by_roberta", return_value=mock_standards):
                with patch("requests.post", return_value=mock_response):
                    from questions.utils.achievement_mapper import infer_relevant_achievement_codes_from_summary

                    result = infer_relevant_achievement_codes_from_summary("summary", "과학", "중학교 2학년")
                    self.assertEqual(result, {"codes": [], "details": {}})

    @override_settings(OPENAI_API_KEY="test-key")
    def test_gpt_api_error(self):
        """Test handling when GPT API returns error status code"""
        mock_standards = [
            {"code": "2과03-01", "content": "test", "subject": "과학", "school": "중학교", "grade": "2학년"},
        ]

        mock_response = MagicMock()
        mock_response.status_code = 500
        mock_response.text = "Internal Server Error"

        with patch("questions.utils.achievement_mapper.load_achievement_standards", return_value=mock_standards):
            with patch("questions.utils.achievement_mapper.filter_standards_by_roberta", return_value=mock_standards):
                with patch("requests.post", return_value=mock_response):
                    from questions.utils.achievement_mapper import infer_relevant_achievement_codes_from_summary

                    result = infer_relevant_achievement_codes_from_summary("summary", "과학", "중학교 2학년")
                    self.assertEqual(result, {"codes": [], "details": {}})

    @override_settings(OPENAI_API_KEY="test-key")
    def test_gpt_request_exception(self):
        """Test handling when requests.post raises exception"""
        mock_standards = [
            {"code": "2과03-01", "content": "test", "subject": "과학", "school": "중학교", "grade": "2학년"},
        ]

        import requests

        with patch("questions.utils.achievement_mapper.load_achievement_standards", return_value=mock_standards):
            with patch("questions.utils.achievement_mapper.filter_standards_by_roberta", return_value=mock_standards):
                with patch("requests.post", side_effect=requests.exceptions.Timeout("Timeout")):
                    from questions.utils.achievement_mapper import infer_relevant_achievement_codes_from_summary

                    result = infer_relevant_achievement_codes_from_summary("summary", "과학", "중학교 2학년")
                    self.assertEqual(result, {"codes": [], "details": {}})

    @override_settings(OPENAI_API_KEY="test-key")
    def test_max_codes_limit(self):
        """Test that results are limited to max_codes"""
        mock_standards = [
            {"code": f"2과03-0{i}", "content": f"내용 {i}", "subject": "과학", "school": "중학교", "grade": "2학년"}
            for i in range(1, 10)
        ]

        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.return_value = {
            "choices": [
                {"message": {"content": '["2과03-01", "2과03-02", "2과03-03", "2과03-04", "2과03-05", "2과03-06"]'}}
            ]
        }

        with patch("questions.utils.achievement_mapper.load_achievement_standards", return_value=mock_standards):
            with patch("questions.utils.achievement_mapper.filter_standards_by_roberta", return_value=mock_standards):
                with patch("requests.post", return_value=mock_response):
                    from questions.utils.achievement_mapper import infer_relevant_achievement_codes_from_summary

                    result = infer_relevant_achievement_codes_from_summary(
                        "summary", "과학", "중학교 2학년", max_codes=3
                    )
                    self.assertEqual(len(result["codes"]), 3)
                    self.assertEqual(len(result["details"]), 3)

    @override_settings(OPENAI_API_KEY="test-key")
    def test_single_code_json_decode_error_fallback(self):
        """Test fallback to single code parsing when JSON decode fails"""
        mock_standards = [
            {"code": "2과03-01", "content": "운동과 에너지", "subject": "과학", "school": "중학교", "grade": "2학년"},
        ]

        mock_response = MagicMock()
        mock_response.status_code = 200
        # Return a valid code but not as JSON
        mock_response.json.return_value = {"choices": [{"message": {"content": "2과03-01"}}]}

        with patch("questions.utils.achievement_mapper.load_achievement_standards", return_value=mock_standards):
            with patch("questions.utils.achievement_mapper.filter_standards_by_roberta", return_value=mock_standards):
                with patch("requests.post", return_value=mock_response):
                    from questions.utils.achievement_mapper import infer_relevant_achievement_codes_from_summary

                    result = infer_relevant_achievement_codes_from_summary("summary", "과학", "중학교 2학년")
                    self.assertEqual(result["codes"], ["2과03-01"])

    @override_settings(OPENAI_API_KEY="test-key")
    def test_general_exception_handling(self):
        """Test handling of general exceptions during processing"""
        mock_standards = [
            {"code": "2과03-01", "content": "test", "subject": "과학", "school": "중학교", "grade": "2학년"},
        ]

        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.side_effect = Exception("Unexpected error")

        with patch("questions.utils.achievement_mapper.load_achievement_standards", return_value=mock_standards):
            with patch("questions.utils.achievement_mapper.filter_standards_by_roberta", return_value=mock_standards):
                with patch("requests.post", return_value=mock_response):
                    from questions.utils.achievement_mapper import infer_relevant_achievement_codes_from_summary

                    result = infer_relevant_achievement_codes_from_summary("summary", "과학", "중학교 2학년")
                    self.assertEqual(result, {"codes": [], "details": {}})
