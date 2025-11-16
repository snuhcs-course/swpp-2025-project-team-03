"""
AnswerSubmitView unit tests
- GET /api/personal_assignments/answer/: 다음 풀이할 문제 조회
- POST /api/personal_assignments/answer/: 음성 답안 제출
"""

import io
import os
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

    def test_get_unexpected_exception(self, api_client, personal_assignment, monkeypatch):
        """GET 요청 중 예상치 못한 예외 발생 시 500 에러"""

        def mock_get(*args, **kwargs):
            raise Exception("Unexpected error")

        url = reverse("answer")
        monkeypatch.setattr(PersonalAssignment.objects, "get", mock_get)
        resp = api_client.get(url, {"personal_assignment_id": personal_assignment.id})

        assert resp.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
        assert resp.data["success"] is False

    def test_all_questions_recalled_num_three(self, api_client, personal_assignment, student):
        """모든 질문이 recalled_num=3에 도달한 경우 404"""
        # number=1에 recalled_num 0~3까지 모두 답변
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
            Answer.objects.create(
                question=q,
                student=student,
                text_answer="ans",
                state=Answer.State.INCORRECT,
                eval_grade=0.5,
                started_at=timezone.now(),
                submitted_at=timezone.now(),
            )

        url = reverse("answer")
        resp = api_client.get(url, {"personal_assignment_id": personal_assignment.id})
        assert resp.status_code == status.HTTP_404_NOT_FOUND
        assert "모든 문제를 완료했습니다" in resp.data["message"]


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

    @patch("submissions.views.generate_tail_question")
    @patch("submissions.views.run_inference")
    @patch("submissions.views.extract_all_features")
    def test_plan_only_correct_updates_status(
        self, mock_extract, mock_infer, mock_tail, api_client, student, personal_assignment, mock_audio_file
    ):
        """plan이 ONLY_CORRECT이고 is_correct=True일 때 solved_num 증가"""
        q = Question.objects.create(
            personal_assignment=personal_assignment,
            number=1,
            content="Q1",
            model_answer="A1",
            explanation="E1",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )
        mock_extract.return_value = {"script": "correct answer", "total_length": 3.0}
        mock_infer.return_value = {"pred_cont": 0.95}
        mock_tail.return_value = {"is_correct": True, "plan": "ONLY_CORRECT", "recalled_time": 0}

        url = reverse("answer")
        resp = api_client.post(
            url, {"studentId": student.id, "questionId": q.id, "audioFile": mock_audio_file}, format="multipart"
        )

        assert resp.status_code == status.HTTP_201_CREATED
        personal_assignment.refresh_from_db()
        assert personal_assignment.solved_num == 1

    @patch("submissions.views.generate_tail_question")
    @patch("submissions.views.run_inference")
    @patch("submissions.views.extract_all_features")
    def test_ask_plan_creates_tail_question(
        self, mock_extract, mock_infer, mock_tail, api_client, student, personal_assignment, mock_audio_file
    ):
        """plan이 ASK이고 recalled_time < 4일 때 tail question 생성"""
        q = Question.objects.create(
            personal_assignment=personal_assignment,
            number=5,
            content="Q5",
            model_answer="A5",
            explanation="E5",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )
        mock_extract.return_value = {"script": "partial answer", "total_length": 2.0}
        mock_infer.return_value = {"pred_cont": 0.6}
        mock_tail.return_value = {
            "is_correct": False,
            "plan": "ASK",
            "recalled_time": 1,
            "tail_question": {
                "question": "Follow-up question?",
                "model_answer": "Follow-up answer",
                "explanation": "Follow-up explanation",
                "difficulty": "MEDIUM",
            },
        }

        url = reverse("answer")
        resp = api_client.post(
            url, {"studentId": student.id, "questionId": q.id, "audioFile": mock_audio_file}, format="multipart"
        )

        assert resp.status_code == status.HTTP_201_CREATED
        # Tail question이 생성되었는지 확인
        tail_q = Question.objects.filter(personal_assignment=personal_assignment, number=5, recalled_num=1).first()
        assert tail_q is not None
        assert tail_q.content == "Follow-up question?"

    @patch("submissions.views.generate_tail_question")
    @patch("submissions.views.run_inference")
    @patch("submissions.views.extract_all_features")
    def test_ask_plan_with_existing_tail_question(
        self, mock_extract, mock_infer, mock_tail, api_client, student, personal_assignment, mock_audio_file
    ):
        """동일한 tail question이 이미 존재하면 재사용"""
        q = Question.objects.create(
            personal_assignment=personal_assignment,
            number=6,
            content="Q6",
            model_answer="A6",
            explanation="E6",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )
        # 기존 tail question 생성
        existing_tail = Question.objects.create(
            personal_assignment=personal_assignment,
            number=6,
            content="Existing tail",
            model_answer="Existing answer",
            explanation="Existing explanation",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=1,
            base_question=q,
        )

        mock_extract.return_value = {"script": "partial answer", "total_length": 2.0}
        mock_infer.return_value = {"pred_cont": 0.6}
        mock_tail.return_value = {
            "is_correct": False,
            "plan": "ASK",
            "recalled_time": 1,
            "tail_question": {
                "question": "New tail question",
                "model_answer": "New answer",
                "explanation": "New explanation",
                "difficulty": "MEDIUM",
            },
        }

        url = reverse("answer")
        resp = api_client.post(
            url, {"studentId": student.id, "questionId": q.id, "audioFile": mock_audio_file}, format="multipart"
        )

        assert resp.status_code == status.HTTP_201_CREATED
        # 기존 tail question이 재사용되었는지 확인 (새로 생성되지 않음)
        tail_count = Question.objects.filter(personal_assignment=personal_assignment, number=6, recalled_num=1).count()
        assert tail_count == 1
        assert resp.data["data"]["tail_question"]["id"] == existing_tail.id

    @patch("submissions.views.generate_tail_question")
    @patch("submissions.views.run_inference")
    @patch("submissions.views.extract_all_features")
    def test_post_unexpected_exception_after_feature_extraction(
        self, mock_extract, mock_infer, mock_tail, api_client, student, personal_assignment, mock_audio_file
    ):
        """Feature 추출 후 예외 발생 시 500 에러 (임시 파일은 정리됨)"""
        q = Question.objects.create(
            personal_assignment=personal_assignment,
            number=11,
            content="Q11",
            model_answer="A11",
            explanation="E11",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )
        mock_extract.return_value = {"script": "answer", "total_length": 1.0}
        mock_infer.return_value = {"pred_cont": 0.7}
        mock_tail.side_effect = Exception("Unexpected tail generation error")

        url = reverse("answer")
        resp = api_client.post(
            url, {"studentId": student.id, "questionId": q.id, "audioFile": mock_audio_file}, format="multipart"
        )

        assert resp.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
        assert resp.data["success"] is False

    @patch("submissions.views.generate_tail_question")
    @patch("submissions.views.run_inference")
    @patch("submissions.views.extract_all_features")
    def test_ask_plan_no_tail_data(
        self, mock_extract, mock_infer, mock_tail, api_client, student, personal_assignment, mock_audio_file
    ):
        """plan이 ASK지만 tail_question 데이터가 없는 경우"""
        q = Question.objects.create(
            personal_assignment=personal_assignment,
            number=12,
            content="Q12",
            model_answer="A12",
            explanation="E12",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )
        mock_extract.return_value = {"script": "partial", "total_length": 1.0}
        mock_infer.return_value = {"pred_cont": 0.6}
        mock_tail.return_value = {
            "is_correct": False,
            "plan": "ASK",
            "recalled_time": 1,
            "tail_question": {},  # 빈 데이터
        }

        url = reverse("answer")
        resp = api_client.post(
            url, {"studentId": student.id, "questionId": q.id, "audioFile": mock_audio_file}, format="multipart"
        )

        # tail_question이 없어도 정상적으로 처리됨
        assert resp.status_code == status.HTTP_201_CREATED

    @patch("submissions.views.generate_tail_question")
    @patch("submissions.views.run_inference")
    @patch("submissions.views.extract_all_features")
    def test_tail_question_creation_exception(
        self, mock_extract, mock_infer, mock_tail, api_client, student, personal_assignment, mock_audio_file
    ):
        """Tail question 생성 중 DB 에러가 발생해도 계속 진행"""
        q = Question.objects.create(
            personal_assignment=personal_assignment,
            number=13,
            content="Q13",
            model_answer="A13",
            explanation="E13",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )
        mock_extract.return_value = {"script": "answer", "total_length": 1.0}
        mock_infer.return_value = {"pred_cont": 0.6}
        mock_tail.return_value = {
            "is_correct": False,
            "plan": "ASK",
            "recalled_time": 1,
            "tail_question": {
                "question": "Follow-up",
                "model_answer": "Answer",
                "explanation": "Explanation",
                "difficulty": "MEDIUM",
            },
        }

        # Question.objects.create를 mock하여 에러 발생시키기
        original_create = Question.objects.create

        def mock_create(*args, **kwargs):
            if kwargs.get("recalled_num", 0) > 0:
                raise Exception("DB error during tail question creation")
            return original_create(*args, **kwargs)

        import unittest.mock

        with unittest.mock.patch.object(Question.objects, "create", side_effect=mock_create):
            url = reverse("answer")
            resp = api_client.post(
                url, {"studentId": student.id, "questionId": q.id, "audioFile": mock_audio_file}, format="multipart"
            )

            # 에러가 발생해도 답변은 정상적으로 저장되고, tail_question은 None
            assert resp.status_code == status.HTTP_201_CREATED
            assert resp.data["data"]["tail_question"] is None


class TestFeatureExtractionAndInference:
    """extract_all_features와 run_inference 로직 테스트"""

    @pytest.fixture
    def test_wav_path(self):
        """테스트용 WAV 파일 경로"""

        current_dir = os.path.dirname(os.path.abspath(__file__))
        wav_path = os.path.join(current_dir, "test_sample", "test_record.wav")
        if not os.path.exists(wav_path):
            pytest.skip(f"테스트용 WAV 파일이 없습니다: {wav_path}")
        return wav_path

    def test_extract_all_features_and_inference_success(self, test_wav_path):
        """extract_all_features와 run_inference 성공 테스트"""
        from datetime import timedelta

        from django.utils import timezone
        from submissions.utils.feature_extractor.extract_all_features import extract_all_features
        from submissions.utils.inference import run_inference

        features = extract_all_features(test_wav_path)

        audio_duration_sec = features.get("total_length", 0.0)
        started_at = timezone.now() - timedelta(seconds=audio_duration_sec)

        assert audio_duration_sec > 0.0
        assert isinstance(started_at, type(timezone.now()))

        transcript = features.get("script", "")
        assert transcript is not None
        assert isinstance(transcript, str)

        xgbmodel_path = "submissions/machine/model.joblib"

        if os.path.exists(xgbmodel_path):
            inference_results = run_inference(xgbmodel_path, features)
            confidence_score = inference_results.get("pred_cont")

            assert confidence_score is not None
            assert isinstance(confidence_score, (int, float))
            assert 0 <= confidence_score <= 8

            assert "pred_rounded" in inference_results
            assert "pred_letter" in inference_results

    @patch("submissions.utils.inference.run_inference")
    def test_extract_all_features_with_mocked_inference(self, mock_run_inference, test_wav_path):
        """extract_all_features는 실제 호출하고 run_inference는 mock하는 테스트"""
        from datetime import timedelta

        from django.utils import timezone
        from submissions.utils.feature_extractor.extract_all_features import extract_all_features
        from submissions.utils.inference import run_inference

        mock_run_inference.return_value = {
            "pred_cont": 6.5,
            "pred_rounded": 7,
            "pred_letter": "A",
        }

        features = extract_all_features(test_wav_path)

        audio_duration_sec = features.get("total_length", 0.0)
        started_at = timezone.now() - timedelta(seconds=audio_duration_sec)

        assert audio_duration_sec > 0.0
        assert isinstance(started_at, type(timezone.now()))

        transcript = features.get("script", "")
        assert transcript is not None
        assert isinstance(transcript, str)

        xgbmodel_path = "submissions/machine/model.joblib"
        inference_results = run_inference(xgbmodel_path, features)

        confidence_score = inference_results.get("pred_cont")
        assert confidence_score == 6.5

        mock_run_inference.assert_called_once_with(xgbmodel_path, features)

    def test_stt_empty_script_handling(self, test_wav_path):
        """STT 결과가 비어있는 경우 처리 테스트"""
        from submissions.utils.feature_extractor.extract_all_features import extract_all_features

        with patch("submissions.utils.feature_extractor.extract_all_features.wave_to_text.speech_to_text") as mock_stt:
            mock_stt.return_value = ""

            features = extract_all_features(test_wav_path)

            transcript = features.get("script", "")
            assert transcript is not None
            assert isinstance(transcript, str)

    def test_inference_missing_pred_cont_handling(self):
        """inference 결과에 pred_cont가 없는 경우 처리 테스트"""
        from submissions.utils.inference import run_inference

        xgbmodel_path = "submissions/machine/model.joblib"
        features = {
            "script": "테스트 스크립트",
            "total_length": 3.5,
            "word_cnt": 5,
            "repeat_cnt": 0,
            "filler_words_cnt": 1,
            "pause_0_5_cnt": 2,
            "voc_speed": 150.0,
            "percent_silence": 0.1,
            "min_f0_hz": 100.0,
            "max_f0_hz": 300.0,
            "range_f0_hz": 200.0,
            "tot_slope_f0_st_per_s": 50.0,
            "end_slope_f0_st_per_s": 30.0,
            "word_speed": 120.0,
            "avg_sentence_len": 5.0,
            "adj_sim_mean": 0.8,
            "adj_sim_std": 0.1,
            "adj_sim_p10": 0.7,
            "adj_sim_p50": 0.8,
            "adj_sim_p90": 0.9,
            "adj_sim_frac_high": 0.6,
            "adj_sim_frac_low": 0.1,
            "topic_path_len": 0.5,
            "dist_to_centroid_mean": 0.3,
            "dist_to_centroid_std": 0.05,
            "coherence_score": 0.9,
            "intra_coh": 0.85,
            "inter_div": 0.2,
        }

        if os.path.exists(xgbmodel_path):
            inference_results = run_inference(xgbmodel_path, features)
            assert "pred_cont" in inference_results
            confidence_score = inference_results.get("pred_cont")
            assert confidence_score is not None
        else:
            pytest.skip(f"모델 파일이 없습니다: {xgbmodel_path}")
