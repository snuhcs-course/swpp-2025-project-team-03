"""
AnswerCorrectnessView unit tests
- GET /api/personal_assignments/{id}/answer-correctness/: 답안 정답 여부 정보 조회
"""

from datetime import timedelta

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
        status=PersonalAssignment.Status.IN_PROGRESS,
        solved_num=0,
    )


@pytest.fixture
def questions(personal_assignment):
    """기본 질문 3개 생성 (recalled_num=0)"""
    questions = []
    for i in range(1, 4):
        q = Question.objects.create(
            personal_assignment=personal_assignment,
            number=i,
            content=f"Question {i}",
            model_answer=f"Answer {i}",
            explanation=f"Explanation {i}",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )
        questions.append(q)
    return questions


@pytest.fixture
def tail_questions(personal_assignment, questions):
    """꼬리 질문 생성 (recalled_num>0)"""
    tail_questions = []
    for i in range(2):
        tail_q = Question.objects.create(
            personal_assignment=personal_assignment,
            number=i + 1,
            content=f"Tail Question {i + 1}",
            model_answer=f"Tail Answer {i + 1}",
            explanation=f"Tail Explanation {i + 1}",
            difficulty=Question.Difficulty.HARD,
            recalled_num=1,
            base_question=questions[i],
        )
        tail_questions.append(tail_q)
    return tail_questions


class TestAnswerCorrectnessView:
    """AnswerCorrectnessView API 테스트"""

    def test_not_found_personal_assignment(self, api_client):
        """존재하지 않는 PersonalAssignment ID로 조회 시 500 에러"""
        url = reverse("answer-correctness", kwargs={"id": 999999})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
        assert resp.data["success"] is False

    def test_no_questions_returns_empty_list(self, api_client, personal_assignment):
        """질문이 없는 경우 빈 리스트 반환"""
        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK
        assert resp.data["success"] is True
        assert len(resp.data["data"]) == 0

    def test_no_answers_returns_empty_list(self, api_client, personal_assignment, questions):
        """답안이 없는 경우 빈 리스트 반환 (continue 로직)"""
        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK
        assert resp.data["success"] is True
        assert len(resp.data["data"]) == 0

    def test_all_questions_included(self, api_client, personal_assignment, questions, tail_questions, student):
        """기본 질문+꼬리 질문 모두 조회됨"""
        # 기본 질문에만 답변
        for q in questions:
            Answer.objects.create(
                question=q,
                student=student,
                text_answer="student answer",
                state=Answer.State.CORRECT,
                eval_grade=0.8,
                started_at=timezone.now(),
                submitted_at=timezone.now(),
            )

        # 꼬리 질문에도 답변
        for tq in tail_questions:
            Answer.objects.create(
                question=tq,
                student=student,
                text_answer="tail answer",
                state=Answer.State.INCORRECT,
                eval_grade=0.4,
                started_at=timezone.now(),
                submitted_at=timezone.now(),
            )

        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK
        # 기본 질문 3개 + 꼬리 질문 2개 = 5개 반환
        assert len(resp.data["data"]) == 5

    def test_correct_answer_state(self, api_client, personal_assignment, questions, student):
        """정답인 경우 is_correct=True"""
        Answer.objects.create(
            question=questions[0],
            student=student,
            text_answer="correct answer",
            state=Answer.State.CORRECT,
            eval_grade=0.85,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK
        assert len(resp.data["data"]) == 1
        data = resp.data["data"][0]
        assert data["is_correct"] is True

    def test_incorrect_answer_state(self, api_client, personal_assignment, questions, student):
        """오답인 경우 is_correct=False"""
        Answer.objects.create(
            question=questions[0],
            student=student,
            text_answer="wrong answer",
            state=Answer.State.INCORRECT,
            eval_grade=0.3,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK
        assert len(resp.data["data"]) == 1
        data = resp.data["data"][0]
        assert data["is_correct"] is False

    def test_mixed_correct_incorrect_answers(self, api_client, personal_assignment, questions, student):
        """정답/오답이 섞인 경우"""
        # 첫 번째는 정답
        Answer.objects.create(
            question=questions[0],
            student=student,
            text_answer="correct",
            state=Answer.State.CORRECT,
            eval_grade=0.9,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        # 두 번째는 오답
        Answer.objects.create(
            question=questions[1],
            student=student,
            text_answer="wrong",
            state=Answer.State.INCORRECT,
            eval_grade=0.4,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK
        assert len(resp.data["data"]) == 2

        # 정답 확인
        correct_item = next(item for item in resp.data["data"] if item["question_content"] == "Question 1")
        assert correct_item["is_correct"] is True

        # 오답 확인
        incorrect_item = next(item for item in resp.data["data"] if item["question_content"] == "Question 2")
        assert incorrect_item["is_correct"] is False

    def test_ordered_by_number(self, api_client, personal_assignment, questions, student):
        """질문이 number 순으로 정렬되어 반환됨"""
        # 역순으로 답변 생성
        for q in reversed(questions):
            Answer.objects.create(
                question=q,
                student=student,
                text_answer="answer",
                state=Answer.State.CORRECT,
                eval_grade=0.8,
                started_at=timezone.now(),
                submitted_at=timezone.now(),
            )

        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK

        # number 순서 확인
        contents = [item["question_content"] for item in resp.data["data"]]
        assert contents == ["Question 1", "Question 2", "Question 3"]

    def test_response_fields_complete(self, api_client, personal_assignment, questions, student):
        """응답에 필요한 모든 필드가 포함되어 있는지 확인"""
        submitted_time = timezone.now()
        Answer.objects.create(
            question=questions[0],
            student=student,
            text_answer="answer",
            state=Answer.State.CORRECT,
            eval_grade=0.8,
            started_at=timezone.now() - timedelta(seconds=10),
            submitted_at=submitted_time,
        )

        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK

        data = resp.data["data"][0]
        assert "question_content" in data
        assert "question_model_answer" in data
        assert "is_correct" in data
        assert "answered_at" in data

        # 값 확인
        assert data["question_content"] == "Question 1"
        assert data["question_model_answer"] == "Answer 1"
        assert data["is_correct"] is True

    def test_partial_answers(self, api_client, personal_assignment, questions, student):
        """일부 질문만 답변한 경우 답변된 것만 반환"""
        # 첫 번째와 세 번째 질문만 답변
        Answer.objects.create(
            question=questions[0],
            student=student,
            text_answer="answer1",
            state=Answer.State.CORRECT,
            eval_grade=0.8,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        Answer.objects.create(
            question=questions[2],
            student=student,
            text_answer="answer3",
            state=Answer.State.INCORRECT,
            eval_grade=0.5,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK
        assert len(resp.data["data"]) == 2

        contents = [item["question_content"] for item in resp.data["data"]]
        assert "Question 1" in contents
        assert "Question 3" in contents
        assert "Question 2" not in contents

    def test_answered_at_field(self, api_client, personal_assignment, questions, student):
        """answered_at 필드가 올바르게 반환됨"""
        submitted_time = timezone.now()
        Answer.objects.create(
            question=questions[0],
            student=student,
            text_answer="answer",
            state=Answer.State.CORRECT,
            eval_grade=0.8,
            started_at=timezone.now() - timedelta(seconds=10),
            submitted_at=submitted_time,
        )

        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK

        data = resp.data["data"][0]
        # answered_at이 submitted_at과 동일한지 확인 (ISO format)
        assert data["answered_at"] is not None

    def test_all_questions_answered_correctly(self, api_client, personal_assignment, questions, student):
        """모든 질문에 정답한 경우"""
        for q in questions:
            Answer.objects.create(
                question=q,
                student=student,
                text_answer="correct",
                state=Answer.State.CORRECT,
                eval_grade=0.9,
                started_at=timezone.now(),
                submitted_at=timezone.now(),
            )

        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK
        assert len(resp.data["data"]) == 3

        # 모두 정답인지 확인
        for item in resp.data["data"]:
            assert item["is_correct"] is True

    def test_all_questions_answered_incorrectly(self, api_client, personal_assignment, questions, student):
        """모든 질문에 오답한 경우"""
        for q in questions:
            Answer.objects.create(
                question=q,
                student=student,
                text_answer="wrong",
                state=Answer.State.INCORRECT,
                eval_grade=0.3,
                started_at=timezone.now(),
                submitted_at=timezone.now(),
            )

        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK
        assert len(resp.data["data"]) == 3

        # 모두 오답인지 확인
        for item in resp.data["data"]:
            assert item["is_correct"] is False

    def test_success_message(self, api_client, personal_assignment, questions, student):
        """성공 메시지 확인"""
        Answer.objects.create(
            question=questions[0],
            student=student,
            text_answer="answer",
            state=Answer.State.CORRECT,
            eval_grade=0.8,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK
        assert resp.data["success"] is True
        assert resp.data["message"] == "답안 정답 여부 정보 조회 성공"

    def test_serializer_validation(self, api_client, personal_assignment, questions, student):
        """Serializer를 통한 데이터 검증"""
        Answer.objects.create(
            question=questions[0],
            student=student,
            text_answer="answer",
            state=Answer.State.CORRECT,
            eval_grade=0.8,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK

        data = resp.data["data"][0]
        # 필드 타입 확인
        assert isinstance(data["question_content"], str)
        assert isinstance(data["question_model_answer"], str)
        assert isinstance(data["is_correct"], bool)
        assert isinstance(data["answered_at"], str)  # ISO format string

    def test_correctness_unexpected_exception(self, api_client, personal_assignment, monkeypatch):
        """예상치 못한 예외 발생 시 500 에러"""

        def mock_get(*args, **kwargs):
            raise Exception("Database error")

        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        monkeypatch.setattr(PersonalAssignment.objects, "get", mock_get)
        resp = api_client.get(url)

        assert resp.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
        assert resp.data["success"] is False

    def test_question_number_base_question(self, api_client, personal_assignment, questions, student):
        """recalled_num=0인 기본 질문의 question_number는 숫자만 표시"""
        Answer.objects.create(
            question=questions[0],
            student=student,
            text_answer="my answer",
            state=Answer.State.CORRECT,
            eval_grade=0.8,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK

        data = resp.data["data"][0]
        # recalled_num=0이므로 question_number는 "1"이어야 함
        assert data["question_number"] == "1"
        assert "-" not in data["question_number"]

    def test_question_number_tail_question(self, api_client, personal_assignment, tail_questions, student):
        """recalled_num>0인 꼬리 질문의 question_number는 'number-recalled_num' 형식"""
        # tail_questions[0]은 number=1, recalled_num=1
        Answer.objects.create(
            question=tail_questions[0],
            student=student,
            text_answer="tail answer",
            state=Answer.State.INCORRECT,
            eval_grade=0.5,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK

        data = resp.data["data"][0]
        # recalled_num=1이므로 question_number는 "1-1"이어야 함
        assert data["question_number"] == "1-1"

    def test_question_number_multiple_tail_questions(self, api_client, personal_assignment, questions, student):
        """여러 단계의 꼬리 질문에 대한 question_number 형식 확인"""
        # recalled_num=2인 꼬리 질문 생성
        tail_q2 = Question.objects.create(
            personal_assignment=personal_assignment,
            number=1,
            content="Tail Question 2",
            model_answer="Tail Answer 2",
            explanation="Tail Explanation 2",
            difficulty=Question.Difficulty.HARD,
            recalled_num=2,
            base_question=questions[0],
        )

        # recalled_num=3인 꼬리 질문 생성
        tail_q3 = Question.objects.create(
            personal_assignment=personal_assignment,
            number=1,
            content="Tail Question 3",
            model_answer="Tail Answer 3",
            explanation="Tail Explanation 3",
            difficulty=Question.Difficulty.HARD,
            recalled_num=3,
            base_question=questions[0],
        )

        # 답변 생성
        Answer.objects.create(
            question=tail_q2,
            student=student,
            text_answer="answer for tail 2",
            state=Answer.State.CORRECT,
            eval_grade=0.7,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        Answer.objects.create(
            question=tail_q3,
            student=student,
            text_answer="answer for tail 3",
            state=Answer.State.CORRECT,
            eval_grade=0.8,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK
        assert len(resp.data["data"]) == 2

        # question_number 확인
        question_numbers = [item["question_number"] for item in resp.data["data"]]
        assert "1-2" in question_numbers
        assert "1-3" in question_numbers

    def test_student_answer_field_exists(self, api_client, personal_assignment, questions, student):
        """student_answer 필드가 응답에 포함되는지 확인"""
        student_answer_text = "This is my student answer"
        Answer.objects.create(
            question=questions[0],
            student=student,
            text_answer=student_answer_text,
            state=Answer.State.CORRECT,
            eval_grade=0.8,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK

        data = resp.data["data"][0]
        assert "student_answer" in data
        assert isinstance(data["student_answer"], str)

    def test_student_answer_content(self, api_client, personal_assignment, questions, student):
        """student_answer에 올바른 답변 내용이 포함되는지 확인"""
        student_answer_text = "이것은 학생의 답변입니다"
        Answer.objects.create(
            question=questions[0],
            student=student,
            text_answer=student_answer_text,
            state=Answer.State.CORRECT,
            eval_grade=0.9,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK

        data = resp.data["data"][0]
        assert data["student_answer"] == student_answer_text

    def test_student_answer_multiple_questions(self, api_client, personal_assignment, questions, student):
        """여러 질문에 대한 student_answer가 올바르게 반환되는지 확인"""
        answers_map = {
            questions[0]: "첫 번째 답변",
            questions[1]: "두 번째 답변",
            questions[2]: "세 번째 답변",
        }

        for question, answer_text in answers_map.items():
            Answer.objects.create(
                question=question,
                student=student,
                text_answer=answer_text,
                state=Answer.State.CORRECT,
                eval_grade=0.8,
                started_at=timezone.now(),
                submitted_at=timezone.now(),
            )

        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK
        assert len(resp.data["data"]) == 3

        # 각 답변의 student_answer가 올바른지 확인
        for item in resp.data["data"]:
            question_content = item["question_content"]
            question_number = int(question_content.split()[-1])  # "Question 1" -> 1
            expected_answer = answers_map[questions[question_number - 1]]
            assert item["student_answer"] == expected_answer

    def test_complete_response_with_all_new_fields(
        self, api_client, personal_assignment, questions, tail_questions, student
    ):
        """모든 새로운 필드(student_answer, question_number)가 포함된 완전한 응답 확인"""
        # 기본 질문 답변
        Answer.objects.create(
            question=questions[0],
            student=student,
            text_answer="Base question answer",
            state=Answer.State.CORRECT,
            eval_grade=0.85,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        # 꼬리 질문 답변
        Answer.objects.create(
            question=tail_questions[0],
            student=student,
            text_answer="Tail question answer",
            state=Answer.State.INCORRECT,
            eval_grade=0.4,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK
        assert len(resp.data["data"]) == 2

        # 기본 질문 확인
        base_item = next(item for item in resp.data["data"] if item["question_number"] == "1")
        assert base_item["student_answer"] == "Base question answer"
        assert base_item["question_number"] == "1"
        assert base_item["is_correct"] is True
        assert "question_content" in base_item
        assert "question_model_answer" in base_item
        assert "answered_at" in base_item

        # 꼬리 질문 확인
        tail_item = next(item for item in resp.data["data"] if item["question_number"] == "1-1")
        assert tail_item["student_answer"] == "Tail question answer"
        assert tail_item["question_number"] == "1-1"
        assert tail_item["is_correct"] is False
        assert "question_content" in tail_item
        assert "question_model_answer" in tail_item
        assert "answered_at" in tail_item

    def test_explanation_field_exists(self, api_client, personal_assignment, questions, student):
        """explanation 필드가 응답에 포함되는지 확인"""
        Answer.objects.create(
            question=questions[0],
            student=student,
            text_answer="my answer",
            state=Answer.State.CORRECT,
            eval_grade=0.8,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK

        data = resp.data["data"][0]
        assert "explanation" in data
        assert isinstance(data["explanation"], str)

    def test_explanation_content(self, api_client, personal_assignment, questions, student):
        """explanation에 올바른 해설 내용이 포함되는지 확인"""
        Answer.objects.create(
            question=questions[0],
            student=student,
            text_answer="my answer",
            state=Answer.State.CORRECT,
            eval_grade=0.9,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK

        data = resp.data["data"][0]
        # questions[0]의 explanation은 "Explanation 1"
        assert data["explanation"] == "Explanation 1"

    def test_explanation_for_multiple_questions(self, api_client, personal_assignment, questions, student):
        """여러 질문에 대한 explanation이 올바르게 반환되는지 확인"""
        for q in questions:
            Answer.objects.create(
                question=q,
                student=student,
                text_answer="answer",
                state=Answer.State.CORRECT,
                eval_grade=0.8,
                started_at=timezone.now(),
                submitted_at=timezone.now(),
            )

        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK
        assert len(resp.data["data"]) == 3

        # 각 질문의 explanation 확인
        for i, item in enumerate(resp.data["data"]):
            expected_explanation = f"Explanation {i + 1}"
            assert item["explanation"] == expected_explanation

    def test_explanation_for_tail_questions(self, api_client, personal_assignment, tail_questions, student):
        """꼬리 질문의 explanation이 올바르게 반환되는지 확인"""
        Answer.objects.create(
            question=tail_questions[0],
            student=student,
            text_answer="tail answer",
            state=Answer.State.INCORRECT,
            eval_grade=0.5,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK

        data = resp.data["data"][0]
        # tail_questions[0]의 explanation은 "Tail Explanation 1"
        assert data["explanation"] == "Tail Explanation 1"

    def test_all_fields_in_response(self, api_client, personal_assignment, questions, student):
        """응답에 모든 필드(question_content, question_model_answer, student_answer, is_correct, answered_at, question_number, explanation)가 포함되는지 확인"""
        Answer.objects.create(
            question=questions[0],
            student=student,
            text_answer="student's answer",
            state=Answer.State.CORRECT,
            eval_grade=0.85,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK

        data = resp.data["data"][0]
        # 모든 필수 필드 확인
        required_fields = [
            "question_content",
            "question_model_answer",
            "student_answer",
            "is_correct",
            "answered_at",
            "question_number",
            "explanation",
        ]
        for field in required_fields:
            assert field in data, f"Field '{field}' is missing from response"

        # 값 검증
        assert data["question_content"] == "Question 1"
        assert data["question_model_answer"] == "Answer 1"
        assert data["student_answer"] == "student's answer"
        assert data["is_correct"] is True
        assert data["question_number"] == "1"
        assert data["explanation"] == "Explanation 1"

    def test_field_types_validation(self, api_client, personal_assignment, questions, student):
        """모든 필드의 타입이 올바른지 확인"""
        Answer.objects.create(
            question=questions[0],
            student=student,
            text_answer="answer text",
            state=Answer.State.CORRECT,
            eval_grade=0.8,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("answer-correctness", kwargs={"id": personal_assignment.id})
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_200_OK

        data = resp.data["data"][0]
        # 필드 타입 검증
        assert isinstance(data["question_content"], str)
        assert isinstance(data["question_model_answer"], str)
        assert isinstance(data["student_answer"], str)
        assert isinstance(data["is_correct"], bool)
        assert isinstance(data["answered_at"], str)  # ISO format string
        assert isinstance(data["question_number"], str)
        assert isinstance(data["explanation"], str)
