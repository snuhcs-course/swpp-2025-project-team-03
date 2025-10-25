"""
AnswerSubmitView unit tests
- GET /api/personal_assignments/answer/: 다음 풀이할 문제 조회
- POST /api/personal_assignments/answer/: 음성 답안 제출
"""

import io
from datetime import timedelta
from unittest.mock import patch

import pytest
from assignments.models import Assignment
from catalog.models import Subject
from courses.models import CourseClass
from django.contrib.auth import get_user_model
from django.urls import reverse
from django.utils import timezone
from questions.models import Question
from rest_framework import status
from rest_framework.test import APIClient
from submissions.models import Answer, PersonalAssignment

Account = get_user_model()

pytestmark = pytest.mark.django_db


@pytest.fixture
def api_client():
    return APIClient()


@pytest.fixture
def teacher():
    return Account.objects.create_user(
        email="teacher@test.com", password="testpass123", display_name="Teacher", is_student=False
    )


@pytest.fixture
def student():
    return Account.objects.create_user(
        email="student@test.com", password="testpass123", display_name="Student", is_student=True
    )


@pytest.fixture
def subject():
    return Subject.objects.create(name="Math")


@pytest.fixture
def course_class(teacher, subject):
    return CourseClass.objects.create(
        teacher=teacher,
        subject=subject,
        name="Algebra 1",
        description="Desc",
        start_date=timezone.now(),
        end_date=timezone.now() + timedelta(days=30),
    )


@pytest.fixture
def assignment(course_class, subject):
    return Assignment.objects.create(
        course_class=course_class,
        subject=subject,
        title="HW 1",
        description="",
        total_questions=3,
        visible_from=timezone.now(),
        due_at=timezone.now() + timedelta(days=7),
        grade="",
    )


@pytest.fixture
def personal_assignment(student, assignment):
    return PersonalAssignment.objects.create(
        student=student,
        assignment=assignment,
        status=PersonalAssignment.Status.NOT_STARTED,
        solved_num=0,
    )


# -----------------------------
# GET: 다음 풀이할 문제 조회
# -----------------------------
class TestAnswerSubmitViewGET:
    def test_missing_param(self, api_client):
        url = reverse("answer")
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_400_BAD_REQUEST
        assert resp.data["success"] is False

    def test_not_found_pa(self, api_client):
        url = reverse("answer")
        resp = api_client.get(url, {"personal_assignment_id": 999999})
        assert resp.status_code == status.HTTP_404_NOT_FOUND

    def test_no_questions(self, api_client, personal_assignment):
        url = reverse("answer")
        resp = api_client.get(url, {"personal_assignment_id": personal_assignment.id})
        assert resp.status_code == status.HTTP_404_NOT_FOUND

    def test_selects_unanswered_in_group(self, api_client, personal_assignment, student):
        # number=1 base answered, tail1 unanswered -> returns tail1
        base = Question.objects.create(
            personal_assignment=personal_assignment,
            number=1,
            content="Q1",
            model_answer="A1",
            explanation="E1",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )
        tail1 = Question.objects.create(
            personal_assignment=personal_assignment,
            number=1,
            content="TQ1",
            model_answer="TA1",
            explanation="TE1",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=1,
            base_question=base,
        )
        Answer.objects.create(
            question=base,
            student=student,
            text_answer="ans",
            state=Answer.State.CORRECT,
            eval_grade=0.8,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )
        url = reverse("answer")
        resp = api_client.get(url, {"personal_assignment_id": personal_assignment.id})
        assert resp.status_code == status.HTTP_200_OK
        data = resp.data["data"]
        assert data["id"] == tail1.id
        assert data["number"] == "1-1"

    def test_skips_recalled_three_to_next_group(self, api_client, personal_assignment, student):
        # number=1 with recalled 0..3 all answered → should move to number=2 base
        qs = []
        for recalled in range(4):
            q = Question.objects.create(
                personal_assignment=personal_assignment,
                number=1,
                content=f"Q1-{recalled}",
                model_answer="A",
                explanation="E",
                difficulty=Question.Difficulty.MEDIUM,
                recalled_num=recalled,
            )
            qs.append(q)
            Answer.objects.create(
                question=q,
                student=student,
                text_answer="ans",
                state=Answer.State.INCORRECT,
                eval_grade=0.5,
                started_at=timezone.now(),
                submitted_at=timezone.now(),
            )

        q2 = Question.objects.create(
            personal_assignment=personal_assignment,
            number=2,
            content="Q2",
            model_answer="A2",
            explanation="E2",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )

        url = reverse("answer")
        resp = api_client.get(url, {"personal_assignment_id": personal_assignment.id})
        assert resp.status_code == status.HTTP_200_OK
        assert resp.data["data"]["id"] == q2.id
        assert resp.data["data"]["number"] == "2"

    def test_picks_lowest_unanswered_tail(self, api_client, personal_assignment, student):
        base = Question.objects.create(
            personal_assignment=personal_assignment,
            number=3,
            content="Q3",
            model_answer="A3",
            explanation="E3",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )
        tail1 = Question.objects.create(
            personal_assignment=personal_assignment,
            number=3,
            content="TQ3-1",
            model_answer="TA3-1",
            explanation="TE3-1",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=1,
            base_question=base,
        )
        tail2 = Question.objects.create(
            personal_assignment=personal_assignment,
            number=3,
            content="TQ3-2",
            model_answer="TA3-2",
            explanation="TE3-2",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=2,
            base_question=base,
        )
        # base, tail1 answered; tail2 unanswered → should pick tail2
        for q in (base, tail1):
            Answer.objects.create(
                question=q,
                student=student,
                text_answer="ans",
                state=Answer.State.CORRECT,
                eval_grade=0.9,
                started_at=timezone.now(),
                submitted_at=timezone.now(),
            )

        url = reverse("answer")
        resp = api_client.get(url, {"personal_assignment_id": personal_assignment.id})
        assert resp.status_code == status.HTTP_200_OK
        assert resp.data["data"]["id"] == tail2.id
        assert resp.data["data"]["number"] == "3-2"


# -----------------------------
# POST: 음성 답안 제출
# -----------------------------
class TestAnswerSubmitViewPOST:
    @pytest.fixture
    def mock_audio_file(self):
        wav_header = b"RIFF" + b"\x00" * 4 + b"WAVE" + b"fmt " + b"\x00" * 20 + b"data" + b"\x00" * 4
        wav_data = wav_header + b"\x00" * 1024
        f = io.BytesIO(wav_data)
        f.name = "test.wav"
        return f

    def test_missing_student(self, api_client, mock_audio_file, personal_assignment):
        # create a question
        q = Question.objects.create(
            personal_assignment=personal_assignment,
            number=1,
            content="Q1",
            model_answer="A1",
            explanation="E1",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )
        url = reverse("answer")
        resp = api_client.post(
            url,
            {"questionId": q.id, "audioFile": mock_audio_file},
            format="multipart",
        )
        assert resp.status_code == status.HTTP_400_BAD_REQUEST

    def test_missing_question(self, api_client, student, mock_audio_file):
        url = reverse("answer")
        resp = api_client.post(url, {"studentId": student.id, "audioFile": mock_audio_file}, format="multipart")
        assert resp.status_code == status.HTTP_400_BAD_REQUEST

    def test_missing_file(self, api_client, student):
        url = reverse("answer")
        resp = api_client.post(url, {"studentId": student.id, "questionId": 1}, format="multipart")
        assert resp.status_code == status.HTTP_400_BAD_REQUEST

    def test_student_not_found(self, api_client, personal_assignment, mock_audio_file):
        q = Question.objects.create(
            personal_assignment=personal_assignment,
            number=1,
            content="Q1",
            model_answer="A1",
            explanation="E1",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )
        url = reverse("answer")
        resp = api_client.post(
            url, {"studentId": 999999, "questionId": q.id, "audioFile": mock_audio_file}, format="multipart"
        )
        assert resp.status_code == status.HTTP_404_NOT_FOUND

    def test_question_not_found(self, api_client, student, mock_audio_file):
        url = reverse("answer")
        resp = api_client.post(
            url, {"studentId": student.id, "questionId": 999999, "audioFile": mock_audio_file}, format="multipart"
        )
        assert resp.status_code == status.HTTP_404_NOT_FOUND

    @patch("submissions.views.extract_all_features")
    def test_stt_empty_script(self, mock_extract, api_client, student, personal_assignment, mock_audio_file):
        q = Question.objects.create(
            personal_assignment=personal_assignment,
            number=5,
            content="Q",
            model_answer="A",
            explanation="E",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )
        mock_extract.return_value = {"script": " ", "total_length": 0.0}
        url = reverse("answer")
        resp = api_client.post(
            url, {"studentId": student.id, "questionId": q.id, "audioFile": mock_audio_file}, format="multipart"
        )
        assert resp.status_code == status.HTTP_400_BAD_REQUEST

    @patch("submissions.views.run_inference")
    @patch("submissions.views.extract_all_features")
    def test_inference_missing_pred_cont(
        self, mock_extract, mock_infer, api_client, student, personal_assignment, mock_audio_file
    ):
        q = Question.objects.create(
            personal_assignment=personal_assignment,
            number=6,
            content="Q",
            model_answer="A",
            explanation="E",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )
        mock_extract.return_value = {"script": "ok", "total_length": 1.0}
        mock_infer.return_value = {}
        url = reverse("answer")
        resp = api_client.post(
            url, {"studentId": student.id, "questionId": q.id, "audioFile": mock_audio_file}, format="multipart"
        )
        assert resp.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR

    @patch("submissions.views.generate_tail_question")
    @patch("submissions.views.run_inference")
    @patch("submissions.views.extract_all_features")
    def test_tail_generation_failure(
        self, mock_extract, mock_infer, mock_tail, api_client, student, personal_assignment, mock_audio_file
    ):
        q = Question.objects.create(
            personal_assignment=personal_assignment,
            number=7,
            content="Q",
            model_answer="A",
            explanation="E",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )
        mock_extract.return_value = {"script": "ok", "total_length": 1.0}
        mock_infer.return_value = {"pred_cont": 0.7}
        mock_tail.return_value = None
        url = reverse("answer")
        resp = api_client.post(
            url, {"studentId": student.id, "questionId": q.id, "audioFile": mock_audio_file}, format="multipart"
        )
        assert resp.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR

    @patch("submissions.views.generate_tail_question")
    @patch("submissions.views.run_inference")
    @patch("submissions.views.extract_all_features")
    def test_updates_existing_answer(
        self, mock_extract, mock_infer, mock_tail, api_client, student, personal_assignment, mock_audio_file
    ):
        q = Question.objects.create(
            personal_assignment=personal_assignment,
            number=8,
            content="Q",
            model_answer="A",
            explanation="E",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )
        # existing answer
        Answer.objects.create(
            question=q,
            student=student,
            text_answer="old",
            state=Answer.State.INCORRECT,
            eval_grade=0.1,
            started_at=timezone.now() - timedelta(seconds=30),
            submitted_at=timezone.now() - timedelta(seconds=20),
        )
        mock_extract.return_value = {"script": "new", "total_length": 2.0}
        mock_infer.return_value = {"pred_cont": 0.9}
        mock_tail.return_value = {"is_correct": True, "plan": "PASS", "recalled_time": 0}
        url = reverse("answer")
        resp = api_client.post(
            url, {"studentId": student.id, "questionId": q.id, "audioFile": mock_audio_file}, format="multipart"
        )
        assert resp.status_code == status.HTTP_201_CREATED
        ans = Answer.objects.get(question=q, student=student)
        assert ans.text_answer == "new"
        assert ans.eval_grade == 0.9

    @patch("submissions.views.generate_tail_question")
    @patch("submissions.views.run_inference")
    @patch("submissions.views.extract_all_features")
    def test_ask_with_recalled_time_4_behaves_like_pass(
        self, mock_extract, mock_infer, mock_tail, api_client, student, personal_assignment, mock_audio_file
    ):
        q = Question.objects.create(
            personal_assignment=personal_assignment,
            number=3,
            content="Q3",
            model_answer="A3",
            explanation="E3",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )
        # intentionally DO NOT create next base to avoid known serializer path
        mock_extract.return_value = {"script": "ok", "total_length": 1.0}
        mock_infer.return_value = {"pred_cont": 0.5}
        mock_tail.return_value = {"is_correct": False, "plan": "ASK", "recalled_time": 4, "tail_question": {}}
        url = reverse("answer")
        resp = api_client.post(
            url, {"studentId": student.id, "questionId": q.id, "audioFile": mock_audio_file}, format="multipart"
        )
        assert resp.status_code == status.HTTP_201_CREATED
        # When recalled_time >=4 and no next base exists, tail_question should be None
        body = resp.data["data"]
        assert body["tail_question"] is None

    def test_invalid_extension(self, api_client, student, personal_assignment):
        q = Question.objects.create(
            personal_assignment=personal_assignment,
            number=1,
            content="Q1",
            model_answer="A1",
            explanation="E1",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )
        bad = io.BytesIO(b"dummy")
        bad.name = "x.txt"
        url = reverse("answer")
        resp = api_client.post(url, {"studentId": student.id, "questionId": q.id, "audioFile": bad}, format="multipart")
        assert resp.status_code == status.HTTP_400_BAD_REQUEST

    @patch("submissions.views.generate_tail_question")
    @patch("submissions.views.run_inference")
    @patch("submissions.views.extract_all_features")
    def test_success_pass_no_next_base(
        self,
        mock_extract,
        mock_infer,
        mock_tail,
        api_client,
        student,
        personal_assignment,
        mock_audio_file,
    ):
        # use question with a high number to avoid finding next base in view
        q = Question.objects.create(
            personal_assignment=personal_assignment,
            number=99,
            content="Q",
            model_answer="A",
            explanation="E",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )
        mock_extract.return_value = {"script": "student answer", "total_length": 12.3}
        mock_infer.return_value = {"pred_cont": 0.8}
        mock_tail.return_value = {"is_correct": True, "plan": "PASS", "recalled_time": 0}

        url = reverse("answer")
        resp = api_client.post(
            url, {"studentId": student.id, "questionId": q.id, "audioFile": mock_audio_file}, format="multipart"
        )
        assert resp.status_code == status.HTTP_201_CREATED
        body = resp.data["data"]
        # When PASS and no next base exists, view returns tail_question None
        assert body["is_correct"] is True
        assert body["tail_question"] is None

    @patch("submissions.views.generate_tail_question")
    @patch("submissions.views.run_inference")
    @patch("submissions.views.extract_all_features")
    def test_started_at_calculation(
        self,
        mock_extract,
        mock_infer,
        mock_tail,
        api_client,
        student,
        personal_assignment,
        mock_audio_file,
    ):
        q = Question.objects.create(
            personal_assignment=personal_assignment,
            number=10,
            content="Q",
            model_answer="A",
            explanation="E",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )
        mock_extract.return_value = {"script": "hello", "total_length": 15.5}
        mock_infer.return_value = {"pred_cont": 0.7}
        mock_tail.return_value = {"is_correct": True, "plan": "PASS", "recalled_time": 0}

        url = reverse("answer")
        resp = api_client.post(
            url, {"studentId": student.id, "questionId": q.id, "audioFile": mock_audio_file}, format="multipart"
        )
        assert resp.status_code == status.HTTP_201_CREATED

        ans = Answer.objects.get(question=q, student=student)
        # submitted_at - started_at ~ 15.5 seconds
        delta = (ans.submitted_at - ans.started_at).total_seconds()
        assert 14.0 < delta < 17.0
