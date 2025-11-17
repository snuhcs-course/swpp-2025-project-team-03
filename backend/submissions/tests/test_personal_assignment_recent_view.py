"""
PersonalAssignmentRecentView unit tests
- GET /api/personal_assignments/recent/: 최근 개인 과제 조회
- next_question_id 제거 후 personal_assignment_id만 반환하도록 수정됨
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
        solved_num=1,
    )


@pytest.fixture
def questions(personal_assignment):
    """기본 질문 3개 생성"""
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


class TestPersonalAssignmentRecentViewUpdated:
    """PersonalAssignmentRecentView API 테스트 (personal_assignment_id만 반환)"""

    def test_missing_student_id(self, api_client):
        """student_id 파라미터가 없으면 400 에러"""
        url = reverse("personal-assignment-recent")
        resp = api_client.get(url)
        assert resp.status_code == status.HTTP_400_BAD_REQUEST
        assert resp.data["success"] is False
        assert "student_id는 필수 파라미터입니다" in resp.data["message"]

    def test_no_personal_assignments_found(self, api_client):
        """해당 학생의 개인 과제가 없는 경우 404 에러"""
        url = reverse("personal-assignment-recent")
        resp = api_client.get(url, {"student_id": 99999})
        assert resp.status_code == status.HTTP_404_NOT_FOUND
        assert resp.data["success"] is False
        assert "해당 학생의 개인 과제가 없습니다" in resp.data["message"]

    def test_returns_recent_answer_personal_assignment(self, api_client, student, personal_assignment, questions):
        """최근 답변이 있고 PA가 SUBMITTED 아닌 경우 해당 PA 반환"""
        # 첫 번째 질문에 답변
        Answer.objects.create(
            question=questions[0],
            student=student,
            text_answer="answer",
            state=Answer.State.CORRECT,
            eval_grade=0.8,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("personal-assignment-recent")
        resp = api_client.get(url, {"student_id": student.id})

        assert resp.status_code == status.HTTP_200_OK
        assert resp.data["success"] is True
        assert resp.data["data"]["personal_assignment_id"] == personal_assignment.id
        # next_question_id는 더 이상 반환하지 않음
        assert "next_question_id" not in resp.data["data"]

    def test_returns_latest_not_started_or_in_progress_pa(self, api_client, student, assignment, course_class, subject):
        """답변이 없는 경우 NOT_STARTED 또는 IN_PROGRESS 상태의 personal_assignment 반환"""
        # 두 개의 personal_assignment 생성
        assignment2 = Assignment.objects.create(
            course_class=course_class,
            subject=subject,
            title="HW 2",
            description="",
            total_questions=2,
            due_at=timezone.now() + timedelta(days=7),
            grade="",
        )

        pa1 = PersonalAssignment.objects.create(
            student=student,
            assignment=assignment,
            status=PersonalAssignment.Status.NOT_STARTED,
            solved_num=0,
        )

        pa2 = PersonalAssignment.objects.create(
            student=student,
            assignment=assignment2,
            status=PersonalAssignment.Status.IN_PROGRESS,
            solved_num=0,
        )

        url = reverse("personal-assignment-recent")
        resp = api_client.get(url, {"student_id": student.id})

        assert resp.status_code == status.HTTP_200_OK
        # 가장 최근에 생성된 pa2 반환 (id가 더 큼)
        assert resp.data["data"]["personal_assignment_id"] == pa2.id

    def test_skips_submitted_personal_assignment(self, api_client, student, assignment, course_class, subject):
        """SUBMITTED 상태의 PA는 건너뛰고 다음 PA를 찾음"""
        # 첫 번째 PA (SUBMITTED 상태)
        pa1 = PersonalAssignment.objects.create(
            student=student,
            assignment=assignment,
            status=PersonalAssignment.Status.SUBMITTED,
            solved_num=3,
        )

        q1 = Question.objects.create(
            personal_assignment=pa1,
            number=1,
            content="Q1",
            model_answer="A1",
            explanation="E1",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )

        # pa1에 답변 (최근 답변)
        Answer.objects.create(
            question=q1,
            student=student,
            text_answer="answer1",
            state=Answer.State.CORRECT,
            eval_grade=0.8,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        # 두 번째 PA (IN_PROGRESS 상태)
        assignment2 = Assignment.objects.create(
            course_class=course_class,
            subject=subject,
            title="HW 2",
            description="",
            total_questions=2,
            due_at=timezone.now() + timedelta(days=7),
            grade="",
        )

        pa2 = PersonalAssignment.objects.create(
            student=student,
            assignment=assignment2,
            status=PersonalAssignment.Status.IN_PROGRESS,
            solved_num=1,
        )

        q2 = Question.objects.create(
            personal_assignment=pa2,
            number=1,
            content="Q2",
            model_answer="A2",
            explanation="E2",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )

        # pa2에 이전 답변
        Answer.objects.create(
            question=q2,
            student=student,
            text_answer="answer2",
            state=Answer.State.CORRECT,
            eval_grade=0.8,
            started_at=timezone.now() - timedelta(minutes=10),
            submitted_at=timezone.now() - timedelta(minutes=10),
        )

        url = reverse("personal-assignment-recent")
        resp = api_client.get(url, {"student_id": student.id})

        assert resp.status_code == status.HTTP_200_OK
        # SUBMITTED인 pa1을 건너뛰고 IN_PROGRESS인 pa2 선택
        assert resp.data["data"]["personal_assignment_id"] == pa2.id

    def test_response_fields_complete(self, api_client, student, personal_assignment, questions):
        """응답에 personal_assignment_id 필드만 포함되어 있는지 확인"""
        Answer.objects.create(
            question=questions[0],
            student=student,
            text_answer="answer",
            state=Answer.State.CORRECT,
            eval_grade=0.8,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("personal-assignment-recent")
        resp = api_client.get(url, {"student_id": student.id})

        assert resp.status_code == status.HTTP_200_OK
        assert "personal_assignment_id" in resp.data["data"]
        assert isinstance(resp.data["data"]["personal_assignment_id"], int)
        # next_question_id는 제거됨
        assert "next_question_id" not in resp.data["data"]

    def test_success_message(self, api_client, student, personal_assignment, questions):
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

        url = reverse("personal-assignment-recent")
        resp = api_client.get(url, {"student_id": student.id})

        assert resp.status_code == status.HTTP_200_OK
        assert resp.data["success"] is True
        assert resp.data["message"] == "최근 진행 과제 조회 성공"

    def test_most_recent_answer_determines_pa(self, api_client, student, assignment, course_class, subject):
        """가장 최근 답변이 있는 personal_assignment를 선택"""
        # 두 개의 personal_assignment 생성
        assignment2 = Assignment.objects.create(
            course_class=course_class,
            subject=subject,
            title="HW 2",
            description="",
            total_questions=2,
            due_at=timezone.now() + timedelta(days=7),
            grade="",
        )

        pa1 = PersonalAssignment.objects.create(
            student=student,
            assignment=assignment,
            status=PersonalAssignment.Status.IN_PROGRESS,
            solved_num=1,
        )

        pa2 = PersonalAssignment.objects.create(
            student=student,
            assignment=assignment2,
            status=PersonalAssignment.Status.IN_PROGRESS,
            solved_num=1,
        )

        # 각각에 질문 추가
        q1 = Question.objects.create(
            personal_assignment=pa1,
            number=1,
            content="Q1",
            model_answer="A1",
            explanation="E1",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )

        q2 = Question.objects.create(
            personal_assignment=pa2,
            number=1,
            content="Q2",
            model_answer="A2",
            explanation="E2",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )

        # pa1에 먼저 답변
        Answer.objects.create(
            question=q1,
            student=student,
            text_answer="answer1",
            state=Answer.State.CORRECT,
            eval_grade=0.8,
            started_at=timezone.now() - timedelta(minutes=10),
            submitted_at=timezone.now() - timedelta(minutes=10),
        )

        # pa2에 나중에 답변 (더 최근)
        Answer.objects.create(
            question=q2,
            student=student,
            text_answer="answer2",
            state=Answer.State.CORRECT,
            eval_grade=0.8,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("personal-assignment-recent")
        resp = api_client.get(url, {"student_id": student.id})

        assert resp.status_code == status.HTTP_200_OK
        # 가장 최근 답변이 있는 pa2 선택
        assert resp.data["data"]["personal_assignment_id"] == pa2.id

    def test_serializer_validation(self, api_client, student, personal_assignment, questions):
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

        url = reverse("personal-assignment-recent")
        resp = api_client.get(url, {"student_id": student.id})

        assert resp.status_code == status.HTTP_200_OK
        # Serializer가 올바르게 동작했는지 확인
        assert "data" in resp.data
        assert "personal_assignment_id" in resp.data["data"]
        # next_question_id는 더 이상 포함되지 않음
        assert "next_question_id" not in resp.data["data"]

    def test_unexpected_exception(self, api_client, student, personal_assignment, monkeypatch):
        """예상치 못한 예외 발생 시 500 에러"""

        def mock_filter(*args, **kwargs):
            raise Exception("Unexpected database error")

        url = reverse("personal-assignment-recent")
        monkeypatch.setattr(Answer.objects, "filter", mock_filter)
        resp = api_client.get(url, {"student_id": student.id})

        assert resp.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
        assert resp.data["success"] is False
        assert "오류가 발생했습니다" in resp.data["message"]

    def test_for_loop_finds_non_submitted_pa(self, api_client, student, assignment, course_class, subject):
        """for 루프를 통해 SUBMITTED 아닌 PA를 찾는지 확인"""
        # 3개의 PA 생성: SUBMITTED, SUBMITTED, IN_PROGRESS
        assignment2 = Assignment.objects.create(
            course_class=course_class,
            subject=subject,
            title="HW 2",
            description="",
            total_questions=2,
            due_at=timezone.now() + timedelta(days=7),
            grade="",
        )

        assignment3 = Assignment.objects.create(
            course_class=course_class,
            subject=subject,
            title="HW 3",
            description="",
            total_questions=2,
            due_at=timezone.now() + timedelta(days=7),
            grade="",
        )

        pa1 = PersonalAssignment.objects.create(
            student=student,
            assignment=assignment,
            status=PersonalAssignment.Status.SUBMITTED,
            solved_num=3,
        )

        pa2 = PersonalAssignment.objects.create(
            student=student,
            assignment=assignment2,
            status=PersonalAssignment.Status.SUBMITTED,
            solved_num=2,
        )

        pa3 = PersonalAssignment.objects.create(
            student=student,
            assignment=assignment3,
            status=PersonalAssignment.Status.IN_PROGRESS,
            solved_num=1,
        )

        # 각각에 질문 추가
        q1 = Question.objects.create(
            personal_assignment=pa1,
            number=1,
            content="Q1",
            model_answer="A1",
            explanation="E1",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )

        q2 = Question.objects.create(
            personal_assignment=pa2,
            number=1,
            content="Q2",
            model_answer="A2",
            explanation="E2",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )

        q3 = Question.objects.create(
            personal_assignment=pa3,
            number=1,
            content="Q3",
            model_answer="A3",
            explanation="E3",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=0,
        )

        # 답변 추가 (제출 시간 순서: pa3 -> pa2 -> pa1)
        Answer.objects.create(
            question=q3,
            student=student,
            text_answer="answer3",
            state=Answer.State.CORRECT,
            eval_grade=0.8,
            started_at=timezone.now() - timedelta(minutes=30),
            submitted_at=timezone.now() - timedelta(minutes=30),
        )

        Answer.objects.create(
            question=q2,
            student=student,
            text_answer="answer2",
            state=Answer.State.CORRECT,
            eval_grade=0.8,
            started_at=timezone.now() - timedelta(minutes=10),
            submitted_at=timezone.now() - timedelta(minutes=10),
        )

        Answer.objects.create(
            question=q1,
            student=student,
            text_answer="answer1",
            state=Answer.State.CORRECT,
            eval_grade=0.8,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("personal-assignment-recent")
        resp = api_client.get(url, {"student_id": student.id})

        assert resp.status_code == status.HTTP_200_OK
        # 가장 최근 답변은 pa1이지만 SUBMITTED이므로 건너뛰고,
        # pa2도 SUBMITTED이므로 건너뛰고,
        # pa3이 IN_PROGRESS이므로 선택
        assert resp.data["data"]["personal_assignment_id"] == pa3.id

    def test_null_submitted_at_is_excluded(self, api_client, student, personal_assignment, questions):
        """submitted_at이 NULL인 답변은 무시됨"""
        # submitted_at이 NULL인 답변 생성
        Answer.objects.create(
            question=questions[0],
            student=student,
            text_answer="answer",
            state=Answer.State.CORRECT,
            eval_grade=0.8,
            started_at=timezone.now(),
            submitted_at=None,  # NULL
        )

        url = reverse("personal-assignment-recent")
        resp = api_client.get(url, {"student_id": student.id})

        # submitted_at이 NULL이므로 최근 답변으로 취급하지 않음
        # 따라서 personal_assignment를 반환
        assert resp.status_code == status.HTTP_200_OK
        assert resp.data["data"]["personal_assignment_id"] == personal_assignment.id
