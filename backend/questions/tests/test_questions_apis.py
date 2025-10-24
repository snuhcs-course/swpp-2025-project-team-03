from unittest.mock import Mock, patch

from django.test import TestCase
from rest_framework import status
from rest_framework.test import APIClient


class TestQuestionCreateView(TestCase):
    """QuestionCreateView 단위 테스트"""

    def setUp(self):
        """테스트 설정"""
        self.client = APIClient()

    @patch("questions.views.Assignment.objects.get")
    @patch("questions.views.Material.objects.get")
    @patch("questions.views.PersonalAssignment.objects.filter")
    @patch("questions.views.generate_base_quizzes")
    def test_post_success_with_existing_summary(
        self, mock_generate, mock_personal_assignment_filter, mock_material_get, mock_assignment_get
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

        mock_personal_assignment_qs = Mock()
        mock_personal_assignment_qs.exists.return_value = True
        mock_personal_assignment_qs.count.return_value = 2
        mock_personal_assignment_qs.first.return_value = mock_personal_assignment1
        mock_personal_assignment_qs.__iter__ = Mock(
            return_value=iter([mock_personal_assignment1, mock_personal_assignment2])
        )
        mock_personal_assignment_filter.return_value = mock_personal_assignment_qs

        mock_generate.return_value = [
            Mock(question="질문1", explanation="설명1", model_answer="답1", difficulty="EASY"),
            Mock(question="질문2", explanation="설명2", model_answer="답2", difficulty="MEDIUM"),
        ]

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
        self.assertEqual(response.data["error"], "No personal assignments found for this assignment")

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
        mock_personal_assignment_qs.first.return_value = mock_personal_assignment
        mock_personal_assignment_qs.__iter__ = Mock(return_value=iter([mock_personal_assignment]))
        mock_personal_assignment_filter.return_value = mock_personal_assignment_qs

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
        mock_personal_assignment_qs.first.return_value = mock_personal_assignment
        mock_personal_assignment_qs.__iter__ = Mock(return_value=iter([mock_personal_assignment]))
        mock_personal_assignment_filter.return_value = mock_personal_assignment_qs

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
        mock_personal_assignment_qs.first.return_value = mock_personal_assignment
        mock_personal_assignment_qs.__iter__ = Mock(return_value=iter([mock_personal_assignment]))
        mock_personal_assignment_filter.return_value = mock_personal_assignment_qs

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

    @patch("questions.views.Assignment.objects.get")
    @patch("questions.views.Material.objects.get")
    @patch("questions.views.PersonalAssignment.objects.filter")
    @patch("questions.views.generate_base_quizzes")
    def test_question_creation_fields(
        self, mock_generate, mock_personal_assignment_filter, mock_material_get, mock_assignment_get
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
        mock_personal_assignment = Mock()
        mock_personal_assignment.id = 1

        mock_personal_assignment_qs = Mock()
        mock_personal_assignment_qs.exists.return_value = True
        mock_personal_assignment_qs.count.return_value = 1
        mock_personal_assignment_qs.first.return_value = mock_personal_assignment
        mock_personal_assignment_qs.__iter__ = Mock(return_value=iter([mock_personal_assignment]))
        mock_personal_assignment_filter.return_value = mock_personal_assignment_qs

        mock_generate.return_value = [
            Mock(question="테스트 질문", explanation="테스트 설명", model_answer="테스트 답", difficulty="HARD"),
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
