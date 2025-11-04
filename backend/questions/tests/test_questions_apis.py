from unittest.mock import Mock, patch

from django.test import TestCase
from rest_framework import status
from rest_framework.test import APIClient


class TestQuestionCreateView(TestCase):
    """QuestionCreateView 단위 테스트"""

    def setUp(self):
        """테스트 설정"""
        self.client = APIClient()

    @patch("questions.views.transaction.atomic")
    @patch("questions.views.Question.objects.filter")
    @patch("questions.views.Assignment.objects.get")
    @patch("questions.views.Material.objects.get")
    @patch("questions.views.PersonalAssignment.objects.filter")
    @patch("questions.views.generate_base_quizzes")
    def test_post_success_with_existing_summary(
        self,
        mock_generate,
        mock_personal_assignment_filter,
        mock_material_get,
        mock_assignment_get,
        mock_question_filter,
        mock_transaction,
    ):
        """summary가 있는 경우 성공 테스트"""
        # Given
        mock_assignment = Mock()
        mock_assignment.id = 1
        mock_assignment_get.return_value = mock_assignment

        mock_material = Mock()
        mock_material.id = 1
        mock_material.summary = "테스트 요약 텍스트입니다."
        mock_material_get.return_value = mock_material

        # PersonalAssignment Mock 설정
        mock_personal_assignment1 = Mock()
        mock_personal_assignment1.id = 1
        mock_personal_assignment2 = Mock()
        mock_personal_assignment2.id = 2

        personal_assignments_list = [mock_personal_assignment1, mock_personal_assignment2]

        from unittest.mock import MagicMock

        mock_personal_assignment_qs_obj = MagicMock()
        mock_personal_assignment_qs_obj.exists.return_value = True
        mock_personal_assignment_qs_obj.count.return_value = 2
        mock_personal_assignment_qs_obj.first.return_value = personal_assignments_list[0]
        mock_personal_assignment_qs_obj.__iter__.side_effect = lambda: iter(personal_assignments_list)
        mock_personal_assignment_filter.return_value = mock_personal_assignment_qs_obj

        mock_question_filter_qs = Mock()
        mock_question_filter_qs.exists.return_value = False
        mock_question_filter.return_value = mock_question_filter_qs

        mock_generate.return_value = [
            Mock(question="질문1", explanation="설명1", model_answer="답1", difficulty="EASY"),
            Mock(question="질문2", explanation="설명2", model_answer="답2", difficulty="MEDIUM"),
        ]

        mock_transaction_context = Mock()
        mock_transaction_context.__enter__ = Mock(return_value=None)
        mock_transaction_context.__exit__ = Mock(return_value=False)
        mock_transaction.return_value = mock_transaction_context

        request_data = {
            "assignment_id": 1,
            "material_id": 1,
            "total_number": 2,
        }

        with patch("questions.views.Question.objects.create") as mock_question_create:
            mock_question1 = Mock()
            mock_question1.id = 1
            mock_question1.number = 1
            mock_question1.content = "질문1"
            mock_question1.model_answer = "답1"
            mock_question1.explanation = "설명1"
            mock_question1.difficulty = "easy"

            mock_question2 = Mock()
            mock_question2.id = 2
            mock_question2.number = 2
            mock_question2.content = "질문2"
            mock_question2.model_answer = "답2"
            mock_question2.explanation = "설명2"
            mock_question2.difficulty = "medium"

            # 2개 personal assignment × 2개 질문 = 4번 호출
            mock_question_create.side_effect = [mock_question1, mock_question2, mock_question1, mock_question2]

            # When
            response = self.client.post("/api/questions/create/", request_data, format="json")

            # Then
            self.assertEqual(response.status_code, status.HTTP_200_OK)
            self.assertIn("assignment_id", response.data)
            self.assertIn("material_id", response.data)
            self.assertIn("summary_preview", response.data)
            self.assertIn("questions", response.data)
            self.assertEqual(len(response.data["questions"]), 2)

    @patch("questions.views.Assignment.objects.get")
    @patch("questions.views.Material.objects.get")
    @patch("questions.views.PersonalAssignment.objects.filter")
    def test_post_no_personal_assignments(
        self, mock_personal_assignment_filter, mock_material_get, mock_assignment_get
    ):
        """PersonalAssignment가 없는 경우 테스트"""
        # Given
        mock_assignment = Mock()
        mock_assignment.id = 1
        mock_assignment_get.return_value = mock_assignment

        mock_material = Mock()
        mock_material.id = 1
        mock_material.summary = "테스트 요약"
        mock_material_get.return_value = mock_material

        # PersonalAssignment가 없는 경우
        mock_personal_assignment_qs = Mock()
        mock_personal_assignment_qs.exists.return_value = False
        mock_personal_assignment_filter.return_value = mock_personal_assignment_qs

        request_data = {
            "assignment_id": 1,
            "material_id": 1,
            "total_number": 1,
        }

        # When
        response = self.client.post("/api/questions/create/", request_data, format="json")

        # Then
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertIn("error", response.data)
        self.assertEqual(response.data["error"], "No personal assignments found for assignment 1")

    @patch("questions.views.Question.objects.filter")
    @patch("questions.views.Assignment.objects.get")
    @patch("questions.views.Material.objects.get")
    @patch("questions.views.PersonalAssignment.objects.filter")
    @patch("questions.views.boto3.client")
    @patch("questions.views.tempfile.NamedTemporaryFile")
    @patch("questions.views.summarize_pdf_from_s3")
    @patch("questions.views.generate_base_quizzes")
    def test_post_success_without_summary(
        self,
        mock_generate,
        mock_summarize,
        mock_tempfile,
        mock_boto3,
        mock_personal_assignment_filter,
        mock_material_get,
        mock_assignment_get,
        mock_question_filter,
    ):
        """summary가 없는 경우 성공 테스트"""
        # Given
        mock_assignment = Mock()
        mock_assignment.id = 1
        mock_assignment_get.return_value = mock_assignment

        mock_material = Mock()
        mock_material.id = 1
        mock_material.summary = ""
        mock_material.s3_key = "test/test.pdf"
        mock_material_get.return_value = mock_material

        # PersonalAssignment Mock 설정
        mock_personal_assignment = Mock()
        mock_personal_assignment.id = 1

        mock_personal_assignment_qs = Mock()
        mock_personal_assignment_qs.exists.return_value = True
        mock_personal_assignment_qs.count.return_value = 1
        mock_personal_assignment_qs.first = Mock(return_value=mock_personal_assignment)
        mock_personal_assignment_qs.__iter__ = Mock(return_value=iter([mock_personal_assignment]))
        mock_personal_assignment_filter.return_value = mock_personal_assignment_qs

        mock_question_filter_qs = Mock()
        mock_question_filter_qs.exists.return_value = False
        mock_question_filter.return_value = mock_question_filter_qs

        mock_s3_client = Mock()
        mock_boto3.return_value = mock_s3_client

        mock_temp_file = Mock()
        mock_temp_file.name = "/tmp/test.pdf"
        mock_tempfile.return_value.__enter__.return_value = mock_temp_file

        mock_summarize.return_value = "요약된 텍스트"
        mock_generate.return_value = [
            Mock(question="질문1", explanation="설명1", model_answer="답1", difficulty="EASY"),
        ]

        request_data = {
            "assignment_id": 1,
            "material_id": 1,
            "total_number": 1,
        }

        with patch("questions.views.Question.objects.create") as mock_question_create:
            mock_question = Mock()
            mock_question.id = 1
            mock_question.number = 1
            mock_question.content = "질문1"
            mock_question.model_answer = "답1"
            mock_question.explanation = "설명1"
            mock_question.difficulty = "easy"
            mock_question_create.return_value = mock_question

            # When
            response = self.client.post("/api/questions/create/", request_data, format="json")

            # Then
            self.assertEqual(response.status_code, status.HTTP_200_OK)
            self.assertIn("assignment_id", response.data)
            self.assertIn("material_id", response.data)
            self.assertIn("summary_preview", response.data)
            self.assertIn("questions", response.data)

            # S3 다운로드가 호출되었는지 확인
            mock_s3_client.download_fileobj.assert_called_once()

            # PDF 요약이 호출되었는지 확인
            mock_summarize.assert_called_once_with("/tmp/test.pdf")

            # Material의 summary가 업데이트되었는지 확인
            self.assertEqual(mock_material.summary, "요약된 텍스트")
            mock_material.save.assert_called_once()

    @patch("questions.views.Assignment.objects.get")
    def test_post_invalid_assignment_id(self, mock_assignment_get):
        """잘못된 assignment_id 테스트"""
        # Given
        from assignments.models import Assignment

        mock_assignment_get.side_effect = Assignment.DoesNotExist()

        request_data = {
            "assignment_id": 999,
            "material_id": 1,
            "total_number": 1,
        }

        # When
        response = self.client.post("/api/questions/create/", request_data, format="json")

        # Then
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)
        self.assertIn("error", response.data)
        self.assertEqual(response.data["error"], "Invalid assignment_id")

    @patch("questions.views.Assignment.objects.get")
    @patch("questions.views.Material.objects.get")
    def test_post_invalid_material_id(self, mock_material_get, mock_assignment_get):
        """잘못된 material_id 테스트"""
        # Given
        mock_assignment = Mock()
        mock_assignment.id = 1
        mock_assignment_get.return_value = mock_assignment

        from assignments.models import Material

        mock_material_get.side_effect = Material.DoesNotExist()

        request_data = {
            "assignment_id": 1,
            "material_id": 999,
            "total_number": 1,
        }

        # When
        response = self.client.post("/api/questions/create/", request_data, format="json")

        # Then
        self.assertEqual(response.status_code, status.HTTP_404_NOT_FOUND)
        self.assertIn("error", response.data)
        self.assertEqual(response.data["error"], "Invalid material_id")

    @patch("questions.views.Assignment.objects.get")
    @patch("questions.views.Material.objects.get")
    @patch("questions.views.boto3.client")
    def test_post_s3_download_error(self, mock_boto3, mock_material_get, mock_assignment_get):
        """S3 다운로드 오류 테스트"""
        # Given
        mock_assignment = Mock()
        mock_assignment.id = 1
        mock_assignment_get.return_value = mock_assignment

        mock_material = Mock()
        mock_material.id = 1
        mock_material.summary = ""
        mock_material.s3_key = "test/test.pdf"
        mock_material_get.return_value = mock_material

        mock_s3_client = Mock()
        mock_s3_client.download_fileobj.side_effect = Exception("S3 오류")
        mock_boto3.return_value = mock_s3_client

        request_data = {
            "assignment_id": 1,
            "material_id": 1,
            "total_number": 1,
        }

        # When
        response = self.client.post("/api/questions/create/", request_data, format="json")

        # Then
        self.assertEqual(response.status_code, status.HTTP_500_INTERNAL_SERVER_ERROR)
        self.assertIn("error", response.data)

    @patch("questions.views.Assignment.objects.get")
    @patch("questions.views.Material.objects.get")
    @patch("questions.views.boto3.client")
    @patch("questions.views.tempfile.NamedTemporaryFile")
    @patch("questions.views.summarize_pdf_from_s3")
    def test_post_pdf_summarization_error(
        self, mock_summarize, mock_tempfile, mock_boto3, mock_material_get, mock_assignment_get
    ):
        """PDF 요약 오류 테스트"""
        # Given
        mock_assignment = Mock()
        mock_assignment.id = 1
        mock_assignment_get.return_value = mock_assignment

        mock_material = Mock()
        mock_material.id = 1
        mock_material.summary = ""
        mock_material.s3_key = "test/test.pdf"
        mock_material_get.return_value = mock_material

        mock_s3_client = Mock()
        mock_boto3.return_value = mock_s3_client

        mock_temp_file = Mock()
        mock_temp_file.name = "/tmp/test.pdf"
        mock_tempfile.return_value.__enter__.return_value = mock_temp_file

        mock_summarize.side_effect = Exception("PDF 요약 오류")

        request_data = {
            "assignment_id": 1,
            "material_id": 1,
            "total_number": 1,
        }

        # When
        response = self.client.post("/api/questions/create/", request_data, format="json")

        # Then
        self.assertEqual(response.status_code, status.HTTP_500_INTERNAL_SERVER_ERROR)
        self.assertIn("error", response.data)

    @patch("questions.views.Assignment.objects.get")
    @patch("questions.views.Material.objects.get")
    @patch("questions.views.generate_base_quizzes")
    def test_post_question_generation_error(self, mock_generate, mock_material_get, mock_assignment_get):
        """질문 생성 오류 테스트"""
        # Given
        mock_assignment = Mock()
        mock_assignment.id = 1
        mock_assignment_get.return_value = mock_assignment

        mock_material = Mock()
        mock_material.id = 1
        mock_material.summary = "테스트 요약"
        mock_material_get.return_value = mock_material

        mock_generate.side_effect = Exception("질문 생성 오류")

        request_data = {
            "assignment_id": 1,
            "material_id": 1,
            "total_number": 1,
        }

        # When
        response = self.client.post("/api/questions/create/", request_data, format="json")

        # Then
        self.assertEqual(response.status_code, status.HTTP_500_INTERNAL_SERVER_ERROR)
        self.assertIn("error", response.data)

    def test_post_invalid_request_data(self):
        """잘못된 요청 데이터 테스트"""
        # Given
        request_data = {
            "assignment_id": 1,
            "material_id": 1,
            # total_number 누락
        }

        # When
        response = self.client.post("/api/questions/create/", request_data, format="json")

        # Then
        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)

    @patch("questions.views.Question.objects.filter")
    @patch("questions.views.Assignment.objects.get")
    @patch("questions.views.Material.objects.get")
    @patch("questions.views.PersonalAssignment.objects.filter")
    @patch("questions.views.boto3.client")
    @patch("questions.views.tempfile.NamedTemporaryFile")
    @patch("questions.views.summarize_pdf_from_s3")
    @patch("questions.views.generate_base_quizzes")
    def test_post_empty_summary_handling(
        self,
        mock_generate,
        mock_summarize,
        mock_tempfile,
        mock_boto3,
        mock_personal_assignment_filter,
        mock_material_get,
        mock_assignment_get,
        mock_question_filter,
    ):
        """빈 summary 문자열 처리 테스트"""
        # Given
        mock_assignment = Mock()
        mock_assignment.id = 1
        mock_assignment_get.return_value = mock_assignment

        mock_material = Mock()
        mock_material.id = 1
        mock_material.summary = ""  # 빈 문자열
        mock_material.s3_key = "test/test.pdf"
        mock_material_get.return_value = mock_material

        # PersonalAssignment Mock 설정
        mock_personal_assignment = Mock()
        mock_personal_assignment.id = 1

        mock_personal_assignment_qs = Mock()
        mock_personal_assignment_qs.exists.return_value = True
        mock_personal_assignment_qs.count.return_value = 1
        mock_personal_assignment_qs.first = Mock(return_value=mock_personal_assignment)
        mock_personal_assignment_qs.__iter__ = Mock(return_value=iter([mock_personal_assignment]))
        mock_personal_assignment_filter.return_value = mock_personal_assignment_qs

        mock_question_filter_qs = Mock()
        mock_question_filter_qs.exists.return_value = False
        mock_question_filter.return_value = mock_question_filter_qs

        mock_s3_client = Mock()
        mock_boto3.return_value = mock_s3_client

        mock_temp_file = Mock()
        mock_temp_file.name = "/tmp/test.pdf"
        mock_tempfile.return_value.__enter__.return_value = mock_temp_file

        mock_summarize.return_value = "요약된 텍스트"
        mock_generate.return_value = [
            Mock(question="질문1", explanation="설명1", model_answer="답1", difficulty="EASY"),
        ]

        request_data = {
            "assignment_id": 1,
            "material_id": 1,
            "total_number": 1,
        }

        with patch("questions.views.Question.objects.create") as mock_question_create:
            mock_question = Mock()
            mock_question.id = 1
            mock_question.number = 1
            mock_question.content = "질문1"
            mock_question.model_answer = "답1"
            mock_question.explanation = "설명1"
            mock_question.difficulty = "easy"
            mock_question_create.return_value = mock_question

            # When
            response = self.client.post("/api/questions/create/", request_data, format="json")

            # Then
            self.assertEqual(response.status_code, status.HTTP_200_OK)
            # S3 다운로드가 호출되었는지 확인 (빈 문자열이므로)
            mock_s3_client.download_fileobj.assert_called_once()

    @patch("questions.views.Question.objects.filter")
    @patch("questions.views.Assignment.objects.get")
    @patch("questions.views.Material.objects.get")
    @patch("questions.views.PersonalAssignment.objects.filter")
    @patch("questions.views.boto3.client")
    @patch("questions.views.tempfile.NamedTemporaryFile")
    @patch("questions.views.summarize_pdf_from_s3")
    @patch("questions.views.generate_base_quizzes")
    def test_post_none_summary_handling(
        self,
        mock_generate,
        mock_summarize,
        mock_tempfile,
        mock_boto3,
        mock_personal_assignment_filter,
        mock_material_get,
        mock_assignment_get,
        mock_question_filter,
    ):
        """None summary 처리 테스트"""
        # Given
        mock_assignment = Mock()
        mock_assignment.id = 1
        mock_assignment_get.return_value = mock_assignment

        mock_material = Mock()
        mock_material.id = 1
        mock_material.summary = None
        mock_material.s3_key = "test/test.pdf"
        mock_material_get.return_value = mock_material

        # PersonalAssignment Mock 설정
        mock_personal_assignment = Mock()
        mock_personal_assignment.id = 1

        mock_personal_assignment_qs = Mock()
        mock_personal_assignment_qs.exists.return_value = True
        mock_personal_assignment_qs.count.return_value = 1
        mock_personal_assignment_qs.first = Mock(return_value=mock_personal_assignment)
        mock_personal_assignment_qs.__iter__ = Mock(return_value=iter([mock_personal_assignment]))
        mock_personal_assignment_filter.return_value = mock_personal_assignment_qs

        mock_question_filter_qs = Mock()
        mock_question_filter_qs.exists.return_value = False
        mock_question_filter.return_value = mock_question_filter_qs

        mock_s3_client = Mock()
        mock_boto3.return_value = mock_s3_client

        mock_temp_file = Mock()
        mock_temp_file.name = "/tmp/test.pdf"
        mock_tempfile.return_value.__enter__.return_value = mock_temp_file

        mock_summarize.return_value = "요약된 텍스트"
        mock_generate.return_value = [
            Mock(question="질문1", explanation="설명1", model_answer="답1", difficulty="EASY"),
        ]

        request_data = {
            "assignment_id": 1,
            "material_id": 1,
            "total_number": 1,
        }

        with patch("questions.views.Question.objects.create") as mock_question_create:
            mock_question = Mock()
            mock_question.id = 1
            mock_question.number = 1
            mock_question.content = "질문1"
            mock_question.model_answer = "답1"
            mock_question.explanation = "설명1"
            mock_question.difficulty = "easy"
            mock_question_create.return_value = mock_question

            # When
            response = self.client.post("/api/questions/create/", request_data, format="json")

            # Then
            self.assertEqual(response.status_code, status.HTTP_200_OK)
            # S3 다운로드가 호출되었는지 확인 (None이므로)
            mock_s3_client.download_fileobj.assert_called_once()

    @patch("questions.views.transaction.atomic")
    @patch("questions.views.Question.objects.filter")
    @patch("questions.views.Assignment.objects.get")
    @patch("questions.views.Material.objects.get")
    @patch("questions.views.PersonalAssignment.objects.filter")
    @patch("questions.views.generate_base_quizzes")
    def test_question_creation_fields(
        self,
        mock_generate,
        mock_personal_assignment_filter,
        mock_material_get,
        mock_assignment_get,
        mock_question_filter,
        mock_transaction,
    ):
        """생성된 Question 객체의 필드 확인"""
        # Given
        mock_assignment = Mock()
        mock_assignment.id = 1
        mock_assignment_get.return_value = mock_assignment

        mock_material = Mock()
        mock_material.id = 1
        mock_material.summary = "테스트 요약"
        mock_material_get.return_value = mock_material

        # PersonalAssignment Mock 설정
        from unittest.mock import MagicMock

        mock_personal_assignment = Mock()
        mock_personal_assignment.id = 1

        personal_assignments_list = [mock_personal_assignment]

        mock_personal_assignment_qs_obj = MagicMock()
        mock_personal_assignment_qs_obj.exists.return_value = True
        mock_personal_assignment_qs_obj.count.return_value = 1
        mock_personal_assignment_qs_obj.first.return_value = personal_assignments_list[0]
        mock_personal_assignment_qs_obj.__iter__.side_effect = lambda: iter(personal_assignments_list)
        mock_personal_assignment_filter.return_value = mock_personal_assignment_qs_obj

        mock_question_filter_qs = Mock()
        mock_question_filter_qs.exists.return_value = False
        mock_question_filter.return_value = mock_question_filter_qs

        mock_generate.return_value = [
            Mock(question="테스트 질문", explanation="테스트 설명", model_answer="테스트 답", difficulty="HARD"),
        ]

        mock_transaction_context = Mock()
        mock_transaction_context.__enter__ = Mock(return_value=None)
        mock_transaction_context.__exit__ = Mock(return_value=False)
        mock_transaction.return_value = mock_transaction_context

        request_data = {
            "assignment_id": 1,
            "material_id": 1,
            "total_number": 1,
        }

        with patch("questions.views.Question.objects.create") as mock_question_create:
            mock_question = Mock()
            mock_question.id = 1
            mock_question.number = 1
            mock_question.content = "테스트 질문"
            mock_question.model_answer = "테스트 답"
            mock_question.explanation = "테스트 설명"
            mock_question.difficulty = "hard"
            mock_question.recalled_num = 0
            mock_question.personal_assignment = None
            mock_question_create.return_value = mock_question

            # When
            response = self.client.post("/api/questions/create/", request_data, format="json")

            # Then
            self.assertEqual(response.status_code, status.HTTP_200_OK)

            # Question 생성 시 올바른 필드들이 전달되었는지 확인
            mock_question_create.assert_called_once()
            call_args = mock_question_create.call_args[1]
            self.assertEqual(call_args["content"], "테스트 질문")
            self.assertEqual(call_args["explanation"], "테스트 설명")
            self.assertEqual(call_args["model_answer"], "테스트 답")
            self.assertEqual(call_args["difficulty"], "hard")
            self.assertEqual(call_args["number"], 1)
            self.assertEqual(call_args["recalled_num"], 0)
            self.assertEqual(call_args["personal_assignment"], mock_personal_assignment)

    @patch("questions.views.Assignment.objects.get")
    @patch("questions.views.Material.objects.get")
    @patch("questions.views.boto3.client")
    @patch("questions.views.tempfile.NamedTemporaryFile")
    def test_post_s3_download_runtime_error_cannot_schedule(
        self, mock_tempfile, mock_boto3, mock_material_get, mock_assignment_get
    ):
        """S3 다운로드 시 RuntimeError (cannot schedule) 테스트 (line 84-86)"""
        mock_assignment = Mock()
        mock_assignment.id = 1
        mock_assignment_get.return_value = mock_assignment

        mock_material = Mock()
        mock_material.id = 1
        mock_material.summary = ""
        mock_material.s3_key = "test/test.pdf"
        mock_material_get.return_value = mock_material

        mock_s3_client = Mock()
        mock_boto3.return_value = mock_s3_client

        mock_temp_file = Mock()
        mock_temp_file.name = "/tmp/test.pdf"
        mock_temp_file.__enter__ = Mock(return_value=mock_temp_file)
        mock_temp_file.__exit__ = Mock(return_value=None)
        mock_tempfile.return_value = mock_temp_file

        mock_s3_client.download_fileobj.side_effect = RuntimeError("cannot schedule")

        request_data = {"assignment_id": 1, "material_id": 1, "total_number": 1}

        response = self.client.post("/api/questions/create/", request_data, format="json")

        self.assertEqual(response.status_code, status.HTTP_500_INTERNAL_SERVER_ERROR)
        self.assertIn("S3 파일 다운로드 중 서버 오류", response.data["error"])

    @patch("questions.views.Assignment.objects.get")
    @patch("questions.views.Material.objects.get")
    @patch("questions.views.boto3.client")
    @patch("questions.views.tempfile.NamedTemporaryFile")
    def test_post_s3_download_runtime_error_other(
        self, mock_tempfile, mock_boto3, mock_material_get, mock_assignment_get
    ):
        """S3 다운로드 시 RuntimeError (other) 테스트 (line 86)"""
        mock_assignment = Mock()
        mock_assignment.id = 1
        mock_assignment_get.return_value = mock_assignment

        mock_material = Mock()
        mock_material.id = 1
        mock_material.summary = ""
        mock_material.s3_key = "test/test.pdf"
        mock_material_get.return_value = mock_material

        mock_s3_client = Mock()
        mock_boto3.return_value = mock_s3_client

        mock_temp_file = Mock()
        mock_temp_file.name = "/tmp/test.pdf"
        mock_temp_file.__enter__ = Mock(return_value=mock_temp_file)
        mock_temp_file.__exit__ = Mock(return_value=None)
        mock_tempfile.return_value = mock_temp_file

        mock_s3_client.download_fileobj.side_effect = RuntimeError("other runtime error")

        request_data = {"assignment_id": 1, "material_id": 1, "total_number": 1}

        response = self.client.post("/api/questions/create/", request_data, format="json")

        self.assertEqual(response.status_code, status.HTTP_500_INTERNAL_SERVER_ERROR)

    @patch("questions.views.Assignment.objects.get")
    @patch("questions.views.Material.objects.get")
    @patch("questions.views.boto3.client")
    @patch("questions.views.tempfile.NamedTemporaryFile")
    @patch("questions.views.summarize_pdf_from_s3")
    def test_post_summarize_runtime_error_interpreter_shutdown(
        self, mock_summarize, mock_tempfile, mock_boto3, mock_material_get, mock_assignment_get
    ):
        """PDF 요약 시 RuntimeError (interpreter shutdown) 테스트 (line 91-94)"""
        mock_assignment = Mock()
        mock_assignment.id = 1
        mock_assignment_get.return_value = mock_assignment

        mock_material = Mock()
        mock_material.id = 1
        mock_material.summary = ""
        mock_material.s3_key = "test/test.pdf"
        mock_material_get.return_value = mock_material

        mock_s3_client = Mock()
        mock_boto3.return_value = mock_s3_client

        mock_temp_file = Mock()
        mock_temp_file.name = "/tmp/test.pdf"
        mock_temp_file.__enter__ = Mock(return_value=mock_temp_file)
        mock_temp_file.__exit__ = Mock(return_value=None)
        mock_tempfile.return_value = mock_temp_file

        mock_s3_client.download_fileobj.return_value = None

        mock_summarize.side_effect = RuntimeError("interpreter shutdown")

        request_data = {"assignment_id": 1, "material_id": 1, "total_number": 1}

        response = self.client.post("/api/questions/create/", request_data, format="json")

        self.assertEqual(response.status_code, status.HTTP_500_INTERNAL_SERVER_ERROR)
        self.assertIn("PDF 요약 중 서버 오류가 발생했습니다", response.data["error"])

    @patch("questions.views.Assignment.objects.get")
    @patch("questions.views.Material.objects.get")
    @patch("questions.views.boto3.client")
    @patch("questions.views.tempfile.NamedTemporaryFile")
    @patch("questions.views.summarize_pdf_from_s3")
    def test_post_summarize_runtime_error_other(
        self, mock_summarize, mock_tempfile, mock_boto3, mock_material_get, mock_assignment_get
    ):
        """PDF 요약 시 RuntimeError (other) 테스트 (line 94)"""
        mock_assignment = Mock()
        mock_assignment.id = 1
        mock_assignment_get.return_value = mock_assignment

        mock_material = Mock()
        mock_material.id = 1
        mock_material.summary = ""
        mock_material.s3_key = "test/test.pdf"
        mock_material_get.return_value = mock_material

        mock_s3_client = Mock()
        mock_boto3.return_value = mock_s3_client

        mock_temp_file = Mock()
        mock_temp_file.name = "/tmp/test.pdf"
        mock_temp_file.__enter__ = Mock(return_value=mock_temp_file)
        mock_temp_file.__exit__ = Mock(return_value=None)
        mock_tempfile.return_value = mock_temp_file

        mock_s3_client.download_fileobj.return_value = None

        mock_summarize.side_effect = RuntimeError("other runtime error")

        request_data = {"assignment_id": 1, "material_id": 1, "total_number": 1}

        response = self.client.post("/api/questions/create/", request_data, format="json")

        self.assertEqual(response.status_code, status.HTTP_500_INTERNAL_SERVER_ERROR)

    @patch("questions.views.Assignment.objects.get")
    @patch("questions.views.Material.objects.get")
    @patch("questions.views.boto3.client")
    @patch("questions.views.tempfile.NamedTemporaryFile")
    @patch("questions.views.summarize_pdf_from_s3")
    def test_post_summarize_timeout_exception(
        self, mock_summarize, mock_tempfile, mock_boto3, mock_material_get, mock_assignment_get
    ):
        """PDF 요약 시 TimeoutException 테스트 (line 96)"""
        from httpx import TimeoutException

        mock_assignment = Mock()
        mock_assignment.id = 1
        mock_assignment_get.return_value = mock_assignment

        mock_material = Mock()
        mock_material.id = 1
        mock_material.summary = ""
        mock_material.s3_key = "test/test.pdf"
        mock_material_get.return_value = mock_material

        mock_s3_client = Mock()
        mock_boto3.return_value = mock_s3_client

        mock_temp_file = Mock()
        mock_temp_file.name = "/tmp/test.pdf"
        mock_temp_file.__enter__ = Mock(return_value=mock_temp_file)
        mock_temp_file.__exit__ = Mock(return_value=None)
        mock_tempfile.return_value = mock_temp_file

        mock_s3_client.download_fileobj.return_value = None

        mock_summarize.side_effect = TimeoutException("Timeout")

        request_data = {"assignment_id": 1, "material_id": 1, "total_number": 1}

        response = self.client.post("/api/questions/create/", request_data, format="json")

        self.assertEqual(response.status_code, 500)
        self.assertIn("OpenAI API timeout 발생", response.data["error"])

    @patch("questions.views.Assignment.objects.get")
    @patch("questions.views.Material.objects.get")
    @patch("questions.views.boto3.client")
    @patch("questions.views.tempfile.NamedTemporaryFile")
    @patch("questions.views.summarize_pdf_from_s3")
    def test_post_summarize_openai_error(
        self, mock_summarize, mock_tempfile, mock_boto3, mock_material_get, mock_assignment_get
    ):
        """PDF 요약 시 OpenAIError 테스트 (line 98)"""
        from openai import OpenAIError

        mock_assignment = Mock()
        mock_assignment.id = 1
        mock_assignment_get.return_value = mock_assignment

        mock_material = Mock()
        mock_material.id = 1
        mock_material.summary = ""
        mock_material.s3_key = "test/test.pdf"
        mock_material_get.return_value = mock_material

        mock_s3_client = Mock()
        mock_boto3.return_value = mock_s3_client

        mock_temp_file = Mock()
        mock_temp_file.name = "/tmp/test.pdf"
        mock_temp_file.__enter__ = Mock(return_value=mock_temp_file)
        mock_temp_file.__exit__ = Mock(return_value=None)
        mock_tempfile.return_value = mock_temp_file

        mock_s3_client.download_fileobj.return_value = None

        mock_summarize.side_effect = OpenAIError("OpenAI API Error")

        request_data = {"assignment_id": 1, "material_id": 1, "total_number": 1}

        response = self.client.post("/api/questions/create/", request_data, format="json")

        self.assertEqual(response.status_code, 500)
        self.assertIn("OpenAI API 오류", response.data["error"])

    @patch("questions.views.Assignment.objects.get")
    @patch("questions.views.Material.objects.get")
    @patch("questions.views.boto3.client")
    @patch("questions.views.tempfile.NamedTemporaryFile")
    @patch("questions.views.summarize_pdf_from_s3")
    def test_post_summarize_network_error(
        self, mock_summarize, mock_tempfile, mock_boto3, mock_material_get, mock_assignment_get
    ):
        """PDF 요약 시 NetworkError 테스트 (line 100)"""
        from httpx import NetworkError

        mock_assignment = Mock()
        mock_assignment.id = 1
        mock_assignment_get.return_value = mock_assignment

        mock_material = Mock()
        mock_material.id = 1
        mock_material.summary = ""
        mock_material.s3_key = "test/test.pdf"
        mock_material_get.return_value = mock_material

        mock_s3_client = Mock()
        mock_boto3.return_value = mock_s3_client

        mock_temp_file = Mock()
        mock_temp_file.name = "/tmp/test.pdf"
        mock_temp_file.__enter__ = Mock(return_value=mock_temp_file)
        mock_temp_file.__exit__ = Mock(return_value=None)
        mock_tempfile.return_value = mock_temp_file

        mock_s3_client.download_fileobj.return_value = None

        mock_summarize.side_effect = NetworkError("Network Error")

        request_data = {"assignment_id": 1, "material_id": 1, "total_number": 1}

        response = self.client.post("/api/questions/create/", request_data, format="json")

        self.assertEqual(response.status_code, 500)
        self.assertIn("네트워크 오류", response.data["error"])

    @patch("questions.views.Assignment.objects.get")
    @patch("questions.views.Material.objects.get")
    @patch("questions.views.boto3.client")
    @patch("questions.views.tempfile.NamedTemporaryFile")
    @patch("questions.views.summarize_pdf_from_s3")
    def test_post_summarize_poppler_error(
        self, mock_summarize, mock_tempfile, mock_boto3, mock_material_get, mock_assignment_get
    ):
        """PDF 요약 시 poppler 에러 테스트 (line 104)"""
        mock_assignment = Mock()
        mock_assignment.id = 1
        mock_assignment_get.return_value = mock_assignment

        mock_material = Mock()
        mock_material.id = 1
        mock_material.summary = ""
        mock_material.s3_key = "test/test.pdf"
        mock_material_get.return_value = mock_material

        mock_s3_client = Mock()
        mock_boto3.return_value = mock_s3_client

        mock_temp_file = Mock()
        mock_temp_file.name = "/tmp/test.pdf"
        mock_temp_file.__enter__ = Mock(return_value=mock_temp_file)
        mock_temp_file.__exit__ = Mock(return_value=None)
        mock_tempfile.return_value = mock_temp_file

        mock_s3_client.download_fileobj.return_value = None

        mock_summarize.side_effect = Exception("poppler not found")

        request_data = {"assignment_id": 1, "material_id": 1, "total_number": 1}

        response = self.client.post("/api/questions/create/", request_data, format="json")

        self.assertEqual(response.status_code, 500)
        self.assertIn("poppler not found", response.data["error"])

    @patch("questions.views.transaction.atomic")
    @patch("questions.views.Question.objects.filter")
    @patch("questions.views.Assignment.objects.get")
    @patch("questions.views.Material.objects.get")
    @patch("questions.views.PersonalAssignment.objects.filter")
    @patch("questions.views.generate_base_quizzes")
    def test_post_generate_quizzes_runtime_error(
        self,
        mock_generate,
        mock_personal_assignment_filter,
        mock_material_get,
        mock_assignment_get,
        mock_question_filter,
        mock_transaction,
    ):
        """문제 생성 시 RuntimeError 테스트 (line 113-116)"""
        mock_assignment = Mock()
        mock_assignment.id = 1
        mock_assignment_get.return_value = mock_assignment

        mock_material = Mock()
        mock_material.id = 1
        mock_material.summary = "테스트 요약"
        mock_material_get.return_value = mock_material

        from unittest.mock import MagicMock

        mock_personal_assignment = Mock()
        mock_personal_assignment.id = 1

        personal_assignments_list = [mock_personal_assignment]

        mock_personal_assignment_qs_obj = MagicMock()
        mock_personal_assignment_qs_obj.exists.return_value = True
        mock_personal_assignment_qs_obj.first.return_value = personal_assignments_list[0]
        mock_personal_assignment_qs_obj.__iter__.side_effect = lambda: iter(personal_assignments_list)
        mock_personal_assignment_filter.return_value = mock_personal_assignment_qs_obj

        mock_question_filter_qs = Mock()
        mock_question_filter_qs.exists.return_value = False
        mock_question_filter.return_value = mock_question_filter_qs

        mock_generate.side_effect = RuntimeError("interpreter shutdown")

        mock_transaction_context = Mock()
        mock_transaction_context.__enter__ = Mock(return_value=None)
        mock_transaction_context.__exit__ = Mock(return_value=False)
        mock_transaction.return_value = mock_transaction_context

        request_data = {"assignment_id": 1, "material_id": 1, "total_number": 1}

        response = self.client.post("/api/questions/create/", request_data, format="json")

        self.assertEqual(response.status_code, status.HTTP_500_INTERNAL_SERVER_ERROR)
        self.assertIn("문제 생성 중 서버 오류가 발생했습니다", response.data["error"])

    @patch("questions.views.transaction.atomic")
    @patch("questions.views.Question.objects.filter")
    @patch("questions.views.Assignment.objects.get")
    @patch("questions.views.Material.objects.get")
    @patch("questions.views.PersonalAssignment.objects.filter")
    @patch("questions.views.generate_base_quizzes")
    def test_post_generate_quizzes_runtime_error_other(
        self,
        mock_generate,
        mock_personal_assignment_filter,
        mock_material_get,
        mock_assignment_get,
        mock_question_filter,
        mock_transaction,
    ):
        """문제 생성 시 RuntimeError (other) 테스트 (line 116)"""
        mock_assignment = Mock()
        mock_assignment.id = 1
        mock_assignment_get.return_value = mock_assignment

        mock_material = Mock()
        mock_material.id = 1
        mock_material.summary = "테스트 요약"
        mock_material_get.return_value = mock_material

        from unittest.mock import MagicMock

        mock_personal_assignment = Mock()
        mock_personal_assignment.id = 1

        personal_assignments_list = [mock_personal_assignment]

        mock_personal_assignment_qs_obj = MagicMock()
        mock_personal_assignment_qs_obj.exists.return_value = True
        mock_personal_assignment_qs_obj.first.return_value = personal_assignments_list[0]
        mock_personal_assignment_qs_obj.__iter__.side_effect = lambda: iter(personal_assignments_list)
        mock_personal_assignment_filter.return_value = mock_personal_assignment_qs_obj

        mock_question_filter_qs = Mock()
        mock_question_filter_qs.exists.return_value = False
        mock_question_filter.return_value = mock_question_filter_qs

        mock_generate.side_effect = RuntimeError("other runtime error")

        mock_transaction_context = Mock()
        mock_transaction_context.__enter__ = Mock(return_value=None)
        mock_transaction_context.__exit__ = Mock(return_value=False)
        mock_transaction.return_value = mock_transaction_context

        request_data = {"assignment_id": 1, "material_id": 1, "total_number": 1}

        response = self.client.post("/api/questions/create/", request_data, format="json")

        self.assertEqual(response.status_code, status.HTTP_500_INTERNAL_SERVER_ERROR)

    @patch("questions.views.transaction.atomic")
    @patch("questions.views.Question.objects.filter")
    @patch("questions.views.Assignment.objects.get")
    @patch("questions.views.Material.objects.get")
    @patch("questions.views.PersonalAssignment.objects.filter")
    @patch("questions.views.generate_base_quizzes")
    def test_post_base_question_already_exists(
        self,
        mock_generate,
        mock_personal_assignment_filter,
        mock_material_get,
        mock_assignment_get,
        mock_question_filter,
        mock_transaction,
    ):
        """Base question이 이미 존재하는 경우 테스트 (line 130)"""
        mock_assignment = Mock()
        mock_assignment.id = 1
        mock_assignment_get.return_value = mock_assignment

        mock_material = Mock()
        mock_material.id = 1
        mock_material.summary = "테스트 요약"
        mock_material_get.return_value = mock_material

        from unittest.mock import MagicMock

        mock_personal_assignment = Mock()
        mock_personal_assignment.id = 1

        personal_assignments_list = [mock_personal_assignment]

        mock_personal_assignment_qs_obj = MagicMock()
        mock_personal_assignment_qs_obj.exists.return_value = True
        mock_personal_assignment_qs_obj.first.return_value = personal_assignments_list[0]
        mock_personal_assignment_qs_obj.__iter__.side_effect = lambda: iter(personal_assignments_list)
        mock_personal_assignment_filter.return_value = mock_personal_assignment_qs_obj

        mock_question_filter_qs = Mock()
        mock_question_filter_qs.exists.return_value = True
        mock_question_filter.return_value = mock_question_filter_qs

        mock_transaction_context = Mock()
        mock_transaction_context.__enter__ = Mock(return_value=None)
        mock_transaction_context.__exit__ = Mock(return_value=False)
        mock_transaction.return_value = mock_transaction_context

        request_data = {"assignment_id": 1, "material_id": 1, "total_number": 1}

        response = self.client.post("/api/questions/create/", request_data, format="json")

        self.assertEqual(response.status_code, status.HTTP_400_BAD_REQUEST)
        self.assertIn("Base question already exists", response.data["error"])

    @patch("questions.views.transaction.atomic")
    @patch("questions.views.Question.objects.filter")
    @patch("questions.views.Assignment.objects.get")
    @patch("questions.views.Material.objects.get")
    @patch("questions.views.PersonalAssignment.objects.filter")
    @patch("questions.views.generate_base_quizzes")
    @patch("questions.views.Question.objects.create")
    def test_post_question_create_exception(
        self,
        mock_question_create,
        mock_generate,
        mock_personal_assignment_filter,
        mock_material_get,
        mock_assignment_get,
        mock_question_filter,
        mock_transaction,
    ):
        """Question 생성 시 Exception 테스트 (line 155-156)"""
        mock_assignment = Mock()
        mock_assignment.id = 1
        mock_assignment_get.return_value = mock_assignment

        mock_material = Mock()
        mock_material.id = 1
        mock_material.summary = "테스트 요약"
        mock_material_get.return_value = mock_material

        from unittest.mock import MagicMock

        mock_personal_assignment = Mock()
        mock_personal_assignment.id = 1

        personal_assignments_list = [mock_personal_assignment]

        mock_personal_assignment_qs_obj = MagicMock()
        mock_personal_assignment_qs_obj.exists.return_value = True
        mock_personal_assignment_qs_obj.first.return_value = personal_assignments_list[0]
        mock_personal_assignment_qs_obj.__iter__.side_effect = lambda: iter(personal_assignments_list)
        mock_personal_assignment_filter.return_value = mock_personal_assignment_qs_obj

        mock_question_filter_qs = Mock()
        mock_question_filter_qs.exists.return_value = False
        mock_question_filter.return_value = mock_question_filter_qs

        mock_generate.return_value = [
            Mock(question="질문1", explanation="설명1", model_answer="답1", difficulty="EASY")
        ]

        mock_transaction_context = Mock()
        mock_transaction_context.__enter__ = Mock(return_value=None)
        mock_transaction_context.__exit__ = Mock(return_value=False)
        mock_transaction.return_value = mock_transaction_context

        mock_question_create.side_effect = Exception("Question creation failed")

        request_data = {"assignment_id": 1, "material_id": 1, "total_number": 1}

        response = self.client.post("/api/questions/create/", request_data, format="json")

        self.assertEqual(response.status_code, status.HTTP_500_INTERNAL_SERVER_ERROR)
        self.assertIn("서버 오류", response.data["error"])
