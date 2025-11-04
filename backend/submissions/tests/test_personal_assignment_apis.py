"""
PersonalAssignment API 유닛 테스트 (DB 사용, Factory 미사용)
- PersonalAssignmentListView 테스트
- PersonalAssignmentQuestionsView 테스트
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
from submissions.models import PersonalAssignment

Account = get_user_model()

# 모든 테스트에서 DB 접근 허용
pytestmark = pytest.mark.django_db

# pytest 실행 예시
# pytest submissions/tests/test_personal_assignment_apis_simple.py -v


@pytest.fixture
def api_client():
    return APIClient()


@pytest.fixture
def teacher():
    """테스트용 교사 계정"""
    return Account.objects.create_user(
        email="teacher@test.com", password="testpass123", display_name="Test Teacher", is_student=False
    )


@pytest.fixture
def student1():
    return Account.objects.create_user(
        email="student1@test.com", password="testpass123", display_name="Test Student 1", is_student=True
    )


@pytest.fixture
def student2():
    return Account.objects.create_user(
        email="student2@test.com", password="testpass123", display_name="Test Student 2", is_student=True
    )


@pytest.fixture
def subject():
    """테스트용 과목"""
    return Subject.objects.create(name="Test Subject")


@pytest.fixture
def course_class(teacher, subject):
    """테스트용 강의 클래스"""
    return CourseClass.objects.create(
        teacher=teacher,
        subject=subject,
        name="Test Class",
        description="Test Description",
        start_date=timezone.now(),
        end_date=timezone.now() + timedelta(days=90),
    )


@pytest.fixture
def assignment(course_class, subject):
    """테스트용 과제"""
    return Assignment.objects.create(
        course_class=course_class,
        subject=subject,
        title="Test Assignment",
        description="Test Assignment Description",
        total_questions=3,
        visible_from=timezone.now(),
        due_at=timezone.now() + timedelta(days=7),
        grade="",
    )


@pytest.fixture
def personal_assignment1(student1, assignment):
    """테스트용 PersonalAssignment 1"""
    return PersonalAssignment.objects.create(
        student=student1,
        assignment=assignment,
        status=PersonalAssignment.Status.NOT_STARTED,
        solved_num=0,
    )


@pytest.fixture
def personal_assignment2(student2, assignment):
    """테스트용 PersonalAssignment 2"""
    return PersonalAssignment.objects.create(
        student=student2,
        assignment=assignment,
        status=PersonalAssignment.Status.IN_PROGRESS,
        solved_num=2,
        started_at=timezone.now(),
    )


@pytest.fixture
def questions(personal_assignment1):
    """personal_assignment1에 연결된 기본 문제(recalled_num=0) 생성"""
    questions = []
    for i in range(1, 4):
        q = Question.objects.create(
            personal_assignment=personal_assignment1,
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
def tail_questions(personal_assignment1, questions):
    """personal_assignment1에 연결된 꼬리 질문(recalled_num>0) 생성"""
    tail_questions = []
    for i, base_q in enumerate(questions[:2], start=1):
        tail_q = Question.objects.create(
            personal_assignment=personal_assignment1,
            number=10 + i,
            content=f"Tail Question {i}",
            model_answer=f"Tail Answer {i}",
            explanation=f"Tail Explanation {i}",
            difficulty=Question.Difficulty.HARD,
            recalled_num=1,
            base_question=base_q,
        )
        tail_questions.append(tail_q)
    return tail_questions


class TestPersonalAssignmentListView:
    """PersonalAssignmentListView API 테스트"""

    def test_list_by_assignment_id_success(self, api_client, personal_assignment1, personal_assignment2):
        """assignment_id로 필터링 성공 테스트"""
        url = reverse("personal-assignment-list")
        response = api_client.get(url, {"assignment_id": personal_assignment1.assignment.id})

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert len(response.data["data"]) == 2
        assert response.data["message"] == "개인 과제 목록 조회 성공"

    def test_list_by_student_id_success(self, api_client, student1, personal_assignment1):
        """student_id로 필터링 성공 테스트"""
        url = reverse("personal-assignment-list")
        response = api_client.get(url, {"student_id": student1.id})

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert len(response.data["data"]) == 1
        assert response.data["data"][0]["student"]["id"] == student1.id

    def test_list_by_both_params_success(self, api_client, student1, personal_assignment1):
        """assignment_id와 student_id 둘 다로 필터링 성공 테스트"""
        url = reverse("personal-assignment-list")
        response = api_client.get(url, {"assignment_id": personal_assignment1.assignment.id, "student_id": student1.id})

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert len(response.data["data"]) == 1
        assert response.data["data"][0]["student"]["id"] == student1.id
        assert response.data["data"][0]["assignment"]["id"] == personal_assignment1.assignment.id

    def test_list_missing_params_error(self, api_client):
        """파라미터 없이 요청 시 에러 테스트"""
        url = reverse("personal-assignment-list")
        response = api_client.get(url)

        assert response.status_code == status.HTTP_400_BAD_REQUEST
        assert response.data["success"] is False
        assert "assignment_id 또는 student_id" in response.data["message"]

    def test_list_empty_result(self, api_client):
        """존재하지 않는 student_id로 조회 시 빈 리스트 반환"""
        url = reverse("personal-assignment-list")
        response = api_client.get(url, {"student_id": 99999})

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert len(response.data["data"]) == 0

    def test_list_includes_correct_fields(self, api_client, personal_assignment1):
        """응답에 필요한 필드가 모두 포함되어 있는지 테스트"""
        url = reverse("personal-assignment-list")
        response = api_client.get(url, {"assignment_id": personal_assignment1.assignment.id})

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"][0]

        # PersonalAssignment 필드 확인 (id 포함)
        assert "id" in data
        assert "status" in data
        assert "solved_num" in data
        assert "started_at" in data
        assert "submitted_at" in data

        # 중첩된 student 필드 확인
        assert "student" in data
        assert "id" in data["student"]
        assert "email" in data["student"]

        # 중첩된 assignment 필드 확인
        assert "assignment" in data
        assert "id" in data["assignment"]
        assert "title" in data["assignment"]

    def test_list_personal_assignment_id_field(self, api_client, personal_assignment1):
        """PersonalAssignment의 id 필드가 올바르게 반환되는지 테스트"""
        url = reverse("personal-assignment-list")
        response = api_client.get(url, {"assignment_id": personal_assignment1.assignment.id})

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"][0]

        # PersonalAssignment의 id가 포함되어야 함
        assert "id" in data
        assert data["id"] == personal_assignment1.id
        assert isinstance(data["id"], int)

    def test_list_unexpected_exception(self, api_client, personal_assignment1, monkeypatch):
        """예상치 못한 예외 발생 시 500 에러"""

        def mock_filter(*args, **kwargs):
            raise Exception("Unexpected database error")

        url = reverse("personal-assignment-list")
        monkeypatch.setattr(PersonalAssignment.objects, "select_related", lambda *args: mock_filter())
        response = api_client.get(url, {"assignment_id": personal_assignment1.assignment.id})

        assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
        assert response.data["success"] is False


class TestPersonalAssignmentQuestionsView:
    """PersonalAssignmentQuestionsView API 테스트"""

    def test_get_questions_success(self, api_client, personal_assignment1, questions):
        """문제 목록 조회 성공 테스트"""
        url = reverse("personal-assignment-questions", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert len(response.data["data"]) == 3  # 기본 문제 3개만
        assert response.data["message"] == "개인 과제 문제 목록 조회 성공"

    def test_get_questions_only_base_questions(self, api_client, personal_assignment1, questions, tail_questions):
        """recalled_num=0인 기본 문제만 반환하는지 테스트"""
        url = reverse("personal-assignment-questions", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]

        # 기본 문제만 3개 (꼬리 질문 제외)
        assert len(data) == 3

        # 모든 문제의 id가 기본 문제들과 일치
        returned_ids = {q["id"] for q in data}
        base_question_ids = {q.id for q in questions}
        assert returned_ids == base_question_ids

    def test_get_questions_ordered_by_number(self, api_client, personal_assignment1, questions):
        """문제가 number 순으로 정렬되어 있는지 테스트"""
        url = reverse("personal-assignment-questions", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]

        # number가 오름차순인지 확인
        numbers = [q["number"] for q in data]
        assert numbers == sorted(numbers)
        assert numbers == [1, 2, 3]

    def test_get_questions_not_found(self, api_client):
        """존재하지 않는 PersonalAssignment ID로 조회 시 404 에러"""
        url = reverse("personal-assignment-questions", kwargs={"id": 99999})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_404_NOT_FOUND
        assert response.data["success"] is False
        assert "찾을 수 없습니다" in response.data["message"]

    def test_get_questions_correct_fields(self, api_client, personal_assignment1, questions):
        """응답에 필요한 필드가 모두 포함되어 있는지 테스트"""
        url = reverse("personal-assignment-questions", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        question_data = response.data["data"][0]

        # 필수 필드 확인
        assert "id" in question_data
        assert "number" in question_data
        assert "question" in question_data
        assert "answer" in question_data
        assert "explanation" in question_data
        assert "difficulty" in question_data

    def test_get_questions_empty_list(self, api_client, personal_assignment2):
        """문제가 없는 PersonalAssignment 조회 시 빈 리스트 반환"""
        url = reverse("personal-assignment-questions", kwargs={"id": personal_assignment2.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert len(response.data["data"]) == 0

    def test_get_questions_field_mapping(self, api_client, personal_assignment1, questions):
        """모델 필드와 응답 필드가 올바르게 매핑되는지 테스트"""
        url = reverse("personal-assignment-questions", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        question_data = response.data["data"][0]
        original_question = questions[0]

        # 필드 매핑 검증
        assert question_data["question"] == original_question.content  # content -> question
        assert question_data["answer"] == original_question.model_answer  # model_answer -> answer
        assert question_data["explanation"] == original_question.explanation
        assert question_data["difficulty"] == original_question.difficulty

    def test_get_questions_unexpected_exception(self, api_client, personal_assignment1, monkeypatch):
        """예상치 못한 예외 발생 시 500 에러"""

        def mock_get(*args, **kwargs):
            raise Exception("Unexpected error")

        url = reverse("personal-assignment-questions", kwargs={"id": personal_assignment1.id})
        monkeypatch.setattr(PersonalAssignment.objects, "get", mock_get)
        response = api_client.get(url)

        assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
        assert response.data["success"] is False


# ============================================================================
# 3. PersonalAssignmentStatisticsView 테스트
# ============================================================================


class TestPersonalAssignmentStatisticsView:
    """개인 과제 통계 조회 API 테스트"""

    @pytest.fixture
    def questions(self, personal_assignment1):
        """테스트용 질문 3개 생성"""
        return [
            Question.objects.create(
                personal_assignment=personal_assignment1,
                number=i,
                content=f"테스트 질문 {i}",
                model_answer=f"테스트 정답 {i}",
                explanation=f"테스트 설명 {i}",
                difficulty=Question.Difficulty.MEDIUM,
                recalled_num=0,
            )
            for i in range(1, 4)
        ]

    @pytest.fixture
    def answers(self, student1, questions):
        """테스트용 답변 생성 (3문제 중 2문제 답변, 1문제는 정답)"""
        from submissions.models import Answer

        return [
            Answer.objects.create(
                question=questions[0],
                student=student1,
                text_answer="학생 답변 1",
                state=Answer.State.CORRECT,
                eval_grade=0.95,
                started_at=timezone.now(),
                submitted_at=timezone.now(),
            ),
            Answer.objects.create(
                question=questions[1],
                student=student1,
                text_answer="학생 답변 2",
                state=Answer.State.INCORRECT,
                eval_grade=0.55,
                started_at=timezone.now(),
                submitted_at=timezone.now(),
            ),
        ]

    def test_get_statistics_success(self, api_client, personal_assignment1, questions, answers):
        """통계 조회 성공 테스트"""
        url = reverse("personal-assignment-statistics", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        data = response.data["data"]

        # 필수 필드 확인
        assert "total_questions" in data
        assert "answered_questions" in data
        assert "correct_answers" in data
        assert "accuracy" in data
        assert "total_problem" in data
        assert "solved_problem" in data
        assert "progress" in data

    def test_get_statistics_correct_counts(self, api_client, personal_assignment1, questions, answers, assignment):
        """통계 수치가 정확한지 테스트"""
        url = reverse("personal-assignment-statistics", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]

        # 총 문제 수: 3개 (질문 3개)
        assert data["total_questions"] == 3

        # 답변한 문제 수: 2개
        assert data["answered_questions"] == 2

        # 정답 수: 1개
        assert data["correct_answers"] == 1

        # 정확도: 1/2 * 100 = 50%
        assert data["accuracy"] == 50.0

        # 총 문제 수 (assignment의 total_questions)
        assert data["total_problem"] == assignment.total_questions

        # 해결한 문제 수 (personal_assignment의 solved_num)
        assert data["solved_problem"] == personal_assignment1.solved_num

    def test_get_statistics_accuracy_calculation(self, api_client, personal_assignment1, questions, answers):
        """정확도 계산이 올바른지 테스트"""
        url = reverse("personal-assignment-statistics", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]

        # accuracy = (correct_answers / answered_questions) * 100
        expected_accuracy = (1 / 2) * 100
        assert data["accuracy"] == expected_accuracy

    def test_get_statistics_progress_calculation(self, api_client, personal_assignment1, questions, assignment):
        """진행률 계산이 올바른지 테스트"""
        # solved_num을 2로 설정
        personal_assignment1.solved_num = 2
        personal_assignment1.save()

        url = reverse("personal-assignment-statistics", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]

        # progress = (solved_problem / total_problem) * 100
        # assignment.total_questions = 3, solved_num = 2
        expected_progress = (2 / 3) * 100
        assert abs(data["progress"] - expected_progress) < 0.01  # 부동소수점 오차 허용

    def test_get_statistics_no_answers(self, api_client, personal_assignment1, questions):
        """답변이 없을 때 통계 조회 테스트"""
        url = reverse("personal-assignment-statistics", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]

        assert data["total_questions"] == 3
        assert data["answered_questions"] == 0
        assert data["correct_answers"] == 0
        assert data["accuracy"] == 0  # 답변이 없으면 0%

    def test_get_statistics_all_correct(self, api_client, personal_assignment1, questions, student1):
        """모든 답변이 정답일 때 테스트"""
        from submissions.models import Answer

        # 3개 문제 모두 정답으로 답변
        for question in questions:
            Answer.objects.create(
                question=question,
                student=student1,
                text_answer="정답",
                state=Answer.State.CORRECT,
                eval_grade=0.95,
                started_at=timezone.now(),
                submitted_at=timezone.now(),
            )

        url = reverse("personal-assignment-statistics", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]

        assert data["answered_questions"] == 3
        assert data["correct_answers"] == 3
        assert data["accuracy"] == 100.0

    def test_get_statistics_all_incorrect(self, api_client, personal_assignment1, questions, student1):
        """모든 답변이 오답일 때 테스트"""
        from submissions.models import Answer

        # 3개 문제 모두 오답으로 답변
        for question in questions:
            Answer.objects.create(
                question=question,
                student=student1,
                text_answer="오답",
                state=Answer.State.INCORRECT,
                eval_grade=0.45,
                started_at=timezone.now(),
                submitted_at=timezone.now(),
            )

        url = reverse("personal-assignment-statistics", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]

        assert data["answered_questions"] == 3
        assert data["correct_answers"] == 0
        assert data["accuracy"] == 0.0

    def test_get_statistics_not_found(self, api_client):
        """존재하지 않는 PersonalAssignment ID로 조회 시 404 에러"""
        url = reverse("personal-assignment-statistics", kwargs={"id": 99999})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_404_NOT_FOUND
        assert response.data["success"] is False
        assert "찾을 수 없습니다" in response.data["message"]

    def test_get_statistics_with_tail_questions(self, api_client, personal_assignment1, questions, student1):
        """꼬리 질문이 있을 때 통계에 포함되는지 테스트"""
        from submissions.models import Answer

        # 기본 질문에 답변
        Answer.objects.create(
            question=questions[0],
            student=student1,
            text_answer="답변",
            state=Answer.State.INCORRECT,
            eval_grade=0.55,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        # 꼬리 질문 생성 (recalled_num > 0)
        tail_question = Question.objects.create(
            personal_assignment=personal_assignment1,
            number=4,
            content="꼬리 질문",
            model_answer="꼬리 정답",
            explanation="꼬리 설명",
            difficulty=Question.Difficulty.MEDIUM,
            recalled_num=1,
            base_question=questions[0],
        )

        # 꼬리 질문에도 답변
        Answer.objects.create(
            question=tail_question,
            student=student1,
            text_answer="꼬리 답변",
            state=Answer.State.CORRECT,
            eval_grade=0.85,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("personal-assignment-statistics", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]

        # 총 문제 수에 꼬리 질문도 포함: 3 + 1 = 4
        assert data["total_questions"] == 4

        # 답변한 문제 수: 2개 (기본 1개 + 꼬리 1개)
        assert data["answered_questions"] == 2

        # 정답 수: 1개 (꼬리 질문만 정답)
        assert data["correct_answers"] == 1

    def test_get_statistics_zero_total_problem(self, api_client, personal_assignment1, assignment):
        """total_questions가 0일 때 progress 계산 테스트"""
        # assignment의 total_questions를 0으로 설정
        assignment.total_questions = 0
        assignment.save()

        url = reverse("personal-assignment-statistics", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]

        # total_problem이 0이면 progress는 0이어야 함
        assert data["total_problem"] == 0
        assert data["progress"] == 0

    def test_get_statistics_serializer_validation(self, api_client, personal_assignment1, questions, answers):
        """응답 데이터가 serializer 검증을 통과하는지 테스트"""
        url = reverse("personal-assignment-statistics", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]

        # 모든 필드가 숫자 타입인지 확인
        assert isinstance(data["total_questions"], int)
        assert isinstance(data["answered_questions"], int)
        assert isinstance(data["correct_answers"], int)
        assert isinstance(data["accuracy"], (int, float))
        assert isinstance(data["total_problem"], int)
        assert isinstance(data["solved_problem"], int)
        assert isinstance(data["progress"], (int, float))

        # 범위 검증
        assert data["total_questions"] >= 0
        assert data["answered_questions"] >= 0
        assert data["correct_answers"] >= 0
        assert 0 <= data["accuracy"] <= 100
        assert data["total_problem"] >= 0
        assert data["solved_problem"] >= 0
        assert 0 <= data["progress"] <= 100

    def test_get_statistics_unexpected_exception(self, api_client, personal_assignment1, monkeypatch):
        """예상치 못한 예외 발생 시 500 에러"""

        def mock_get(*args, **kwargs):
            raise Exception("Database connection failed")

        url = reverse("personal-assignment-statistics", kwargs={"id": personal_assignment1.id})
        monkeypatch.setattr(PersonalAssignment.objects, "get", mock_get)
        response = api_client.get(url)

        assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
        assert response.data["success"] is False

    # ============================================================================
    # average_score 테스트
    # ============================================================================

    def test_average_score_all_correct_on_first_try(self, api_client, personal_assignment1, student1):
        """모든 문제를 첫 시도(recalled_num=0)에 정답 맞춘 경우 - 평균 100점"""
        from submissions.models import Answer

        # 3개의 base question 생성
        base_questions = [
            Question.objects.create(
                personal_assignment=personal_assignment1,
                number=i,
                content=f"문제 {i}",
                model_answer=f"정답 {i}",
                recalled_num=0,
            )
            for i in range(1, 4)
        ]

        # 모든 base question에 정답 답변
        for q in base_questions:
            Answer.objects.create(
                question=q,
                student=student1,
                text_answer="정답",
                state=Answer.State.CORRECT,
                started_at=timezone.now(),
                submitted_at=timezone.now(),
            )

        url = reverse("personal-assignment-statistics", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]
        assert "average_score" in data
        # 3문제 모두 100점 -> 평균 100점
        assert data["average_score"] == 100.0

    def test_average_score_correct_on_second_try(self, api_client, personal_assignment1, student1):
        """recalled_num=1에서 처음으로 정답 맞춘 경우 - 75점"""
        from submissions.models import Answer

        # base question (recalled_num=0)
        base_q = Question.objects.create(
            personal_assignment=personal_assignment1,
            number=1,
            content="문제 1",
            model_answer="정답 1",
            recalled_num=0,
        )

        # tail question (recalled_num=1)
        tail_q1 = Question.objects.create(
            personal_assignment=personal_assignment1,
            number=1,
            content="문제 1-1",
            model_answer="정답 1-1",
            recalled_num=1,
            base_question=base_q,
        )

        # base question에 오답
        Answer.objects.create(
            question=base_q,
            student=student1,
            text_answer="오답",
            state=Answer.State.INCORRECT,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        # tail question에 정답
        Answer.objects.create(
            question=tail_q1,
            student=student1,
            text_answer="정답",
            state=Answer.State.CORRECT,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("personal-assignment-statistics", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]
        # recalled_num=1에서 첫 정답 -> 75점
        assert data["average_score"] == 75.0

    def test_average_score_correct_on_third_try(self, api_client, personal_assignment1, student1):
        """recalled_num=2에서 처음으로 정답 맞춘 경우 - 50점"""
        from submissions.models import Answer

        base_q = Question.objects.create(
            personal_assignment=personal_assignment1,
            number=1,
            content="문제 1",
            model_answer="정답 1",
            recalled_num=0,
        )

        tail_q1 = Question.objects.create(
            personal_assignment=personal_assignment1,
            number=1,
            content="문제 1-1",
            model_answer="정답 1-1",
            recalled_num=1,
            base_question=base_q,
        )

        tail_q2 = Question.objects.create(
            personal_assignment=personal_assignment1,
            number=1,
            content="문제 1-2",
            model_answer="정답 1-2",
            recalled_num=2,
            base_question=base_q,
        )

        # recalled_num=0, 1 모두 오답
        Answer.objects.create(
            question=base_q,
            student=student1,
            text_answer="오답",
            state=Answer.State.INCORRECT,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )
        Answer.objects.create(
            question=tail_q1,
            student=student1,
            text_answer="오답",
            state=Answer.State.INCORRECT,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        # recalled_num=2에서 정답
        Answer.objects.create(
            question=tail_q2,
            student=student1,
            text_answer="정답",
            state=Answer.State.CORRECT,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("personal-assignment-statistics", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]
        # recalled_num=2에서 첫 정답 -> 50점
        assert data["average_score"] == 50.0

    def test_average_score_correct_on_fourth_try(self, api_client, personal_assignment1, student1):
        """recalled_num=3에서 처음으로 정답 맞춘 경우 - 25점"""
        from submissions.models import Answer

        base_q = Question.objects.create(
            personal_assignment=personal_assignment1,
            number=1,
            content="문제 1",
            model_answer="정답 1",
            recalled_num=0,
        )

        # recalled_num 1, 2, 3 생성
        tail_questions = []
        for i in range(1, 4):
            tail_q = Question.objects.create(
                personal_assignment=personal_assignment1,
                number=1,
                content=f"문제 1-{i}",
                model_answer=f"정답 1-{i}",
                recalled_num=i,
                base_question=base_q,
            )
            tail_questions.append(tail_q)

        # recalled_num=0, 1, 2 모두 오답
        Answer.objects.create(
            question=base_q,
            student=student1,
            text_answer="오답",
            state=Answer.State.INCORRECT,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )
        Answer.objects.create(
            question=tail_questions[0],
            student=student1,
            text_answer="오답",
            state=Answer.State.INCORRECT,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )
        Answer.objects.create(
            question=tail_questions[1],
            student=student1,
            text_answer="오답",
            state=Answer.State.INCORRECT,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        # recalled_num=3에서 정답
        Answer.objects.create(
            question=tail_questions[2],
            student=student1,
            text_answer="정답",
            state=Answer.State.CORRECT,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("personal-assignment-statistics", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]
        # recalled_num=3에서 첫 정답 -> 25점
        assert data["average_score"] == 25.0

    def test_average_score_all_incorrect(self, api_client, personal_assignment1, student1):
        """모든 recalled_num에서 오답인 경우 - 0점"""
        from submissions.models import Answer

        base_q = Question.objects.create(
            personal_assignment=personal_assignment1,
            number=1,
            content="문제 1",
            model_answer="정답 1",
            recalled_num=0,
        )

        tail_questions = []
        for i in range(1, 4):
            tail_q = Question.objects.create(
                personal_assignment=personal_assignment1,
                number=1,
                content=f"문제 1-{i}",
                model_answer=f"정답 1-{i}",
                recalled_num=i,
                base_question=base_q,
            )
            tail_questions.append(tail_q)

        # 모든 recalled_num에서 오답
        Answer.objects.create(
            question=base_q,
            student=student1,
            text_answer="오답",
            state=Answer.State.INCORRECT,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )
        for tail_q in tail_questions:
            Answer.objects.create(
                question=tail_q,
                student=student1,
                text_answer="오답",
                state=Answer.State.INCORRECT,
                started_at=timezone.now(),
                submitted_at=timezone.now(),
            )

        url = reverse("personal-assignment-statistics", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]
        # 모두 오답 -> 0점
        assert data["average_score"] == 0.0

    def test_average_score_no_answers(self, api_client, personal_assignment1):
        """답변이 전혀 없는 경우 - 0점"""
        # base questions만 생성, 답변 없음
        for i in range(1, 4):
            Question.objects.create(
                personal_assignment=personal_assignment1,
                number=i,
                content=f"문제 {i}",
                model_answer=f"정답 {i}",
                recalled_num=0,
            )

        url = reverse("personal-assignment-statistics", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]
        # 답변 없음 -> 0점
        assert data["average_score"] == 0.0

    def test_average_score_partial_answers_with_previous_correct(self, api_client, personal_assignment1, student1):
        """일부만 답변했지만 이전에 정답이 있는 경우"""
        from submissions.models import Answer

        base_q = Question.objects.create(
            personal_assignment=personal_assignment1,
            number=1,
            content="문제 1",
            model_answer="정답 1",
            recalled_num=0,
        )

        tail_q1 = Question.objects.create(
            personal_assignment=personal_assignment1,
            number=1,
            content="문제 1-1",
            model_answer="정답 1-1",
            recalled_num=1,
            base_question=base_q,
        )

        tail_q2 = Question.objects.create(
            personal_assignment=personal_assignment1,
            number=1,
            content="문제 1-2",
            model_answer="정답 1-2",
            recalled_num=2,
            base_question=base_q,
        )

        # recalled_num=0 오답, recalled_num=1 정답
        Answer.objects.create(
            question=base_q,
            student=student1,
            text_answer="오답",
            state=Answer.State.INCORRECT,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )
        Answer.objects.create(
            question=tail_q1,
            student=student1,
            text_answer="정답",
            state=Answer.State.CORRECT,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )
        # recalled_num=2는 답변 없음

        url = reverse("personal-assignment-statistics", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]
        # recalled_num=1에서 정답 -> 75점
        assert data["average_score"] == 75.0

    def test_average_score_partial_answers_all_incorrect(self, api_client, personal_assignment1, student1):
        """일부만 답변했고 이전이 모두 오답인 경우 - 0점"""
        from submissions.models import Answer

        base_q = Question.objects.create(
            personal_assignment=personal_assignment1,
            number=1,
            content="문제 1",
            model_answer="정답 1",
            recalled_num=0,
        )

        tail_q1 = Question.objects.create(
            personal_assignment=personal_assignment1,
            number=1,
            content="문제 1-1",
            model_answer="정답 1-1",
            recalled_num=1,
            base_question=base_q,
        )

        tail_q2 = Question.objects.create(
            personal_assignment=personal_assignment1,
            number=1,
            content="문제 1-2",
            model_answer="정답 1-2",
            recalled_num=2,
            base_question=base_q,
        )

        # recalled_num=0, 1 모두 오답
        Answer.objects.create(
            question=base_q,
            student=student1,
            text_answer="오답",
            state=Answer.State.INCORRECT,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )
        Answer.objects.create(
            question=tail_q1,
            student=student1,
            text_answer="오답",
            state=Answer.State.INCORRECT,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )
        # recalled_num=2는 답변 없음

        url = reverse("personal-assignment-statistics", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]
        # 이전이 모두 오답이고 다음 답변 없음 -> 0점
        assert data["average_score"] == 0.0

    def test_average_score_multiple_questions_mixed_results(self, api_client, personal_assignment1, student1):
        """여러 문제가 있고 각각 다른 recalled_num에서 정답인 경우"""
        from submissions.models import Answer

        # 문제 1: recalled_num=0에서 정답 (100점)
        q1_base = Question.objects.create(
            personal_assignment=personal_assignment1,
            number=1,
            content="문제 1",
            model_answer="정답 1",
            recalled_num=0,
        )
        Answer.objects.create(
            question=q1_base,
            student=student1,
            text_answer="정답",
            state=Answer.State.CORRECT,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        # 문제 2: recalled_num=1에서 정답 (75점)
        q2_base = Question.objects.create(
            personal_assignment=personal_assignment1,
            number=2,
            content="문제 2",
            model_answer="정답 2",
            recalled_num=0,
        )
        q2_tail1 = Question.objects.create(
            personal_assignment=personal_assignment1,
            number=2,
            content="문제 2-1",
            model_answer="정답 2-1",
            recalled_num=1,
            base_question=q2_base,
        )
        Answer.objects.create(
            question=q2_base,
            student=student1,
            text_answer="오답",
            state=Answer.State.INCORRECT,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )
        Answer.objects.create(
            question=q2_tail1,
            student=student1,
            text_answer="정답",
            state=Answer.State.CORRECT,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        # 문제 3: 모두 오답 (0점)
        q3_base = Question.objects.create(
            personal_assignment=personal_assignment1,
            number=3,
            content="문제 3",
            model_answer="정답 3",
            recalled_num=0,
        )
        Answer.objects.create(
            question=q3_base,
            student=student1,
            text_answer="오답",
            state=Answer.State.INCORRECT,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("personal-assignment-statistics", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]
        # (100 + 75 + 0) / 3 = 58.333...
        expected_score = (100 + 75 + 0) / 3
        assert abs(data["average_score"] - expected_score) < 0.01

    def test_average_score_with_no_base_questions(self, api_client, personal_assignment1):
        """base question이 없는 경우 - 0점"""
        url = reverse("personal-assignment-statistics", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]
        # base question이 없으면 0점
        assert data["average_score"] == 0.0

    def test_average_score_only_answers_for_some_questions(self, api_client, personal_assignment1, student1):
        """일부 문제만 답변한 경우 - 답변 없는 문제는 0점 처리"""
        from submissions.models import Answer

        # 문제 1: 정답 (100점)
        q1 = Question.objects.create(
            personal_assignment=personal_assignment1,
            number=1,
            content="문제 1",
            model_answer="정답 1",
            recalled_num=0,
        )
        Answer.objects.create(
            question=q1,
            student=student1,
            text_answer="정답",
            state=Answer.State.CORRECT,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        # 문제 2: 답변 없음 (0점)
        Question.objects.create(
            personal_assignment=personal_assignment1,
            number=2,
            content="문제 2",
            model_answer="정답 2",
            recalled_num=0,
        )

        # 문제 3: 오답 (0점)
        q3 = Question.objects.create(
            personal_assignment=personal_assignment1,
            number=3,
            content="문제 3",
            model_answer="정답 3",
            recalled_num=0,
        )
        Answer.objects.create(
            question=q3,
            student=student1,
            text_answer="오답",
            state=Answer.State.INCORRECT,
            started_at=timezone.now(),
            submitted_at=timezone.now(),
        )

        url = reverse("personal-assignment-statistics", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]
        # (100 + 0 + 0) / 3 = 33.333...
        expected_score = 100 / 3
        assert abs(data["average_score"] - expected_score) < 0.01

    def test_average_score_serializer_field_exists(self, api_client, personal_assignment1):
        """average_score 필드가 응답에 포함되는지 확인"""
        Question.objects.create(
            personal_assignment=personal_assignment1,
            number=1,
            content="문제 1",
            model_answer="정답 1",
            recalled_num=0,
        )

        url = reverse("personal-assignment-statistics", kwargs={"id": personal_assignment1.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        data = response.data["data"]
        assert "average_score" in data
        assert isinstance(data["average_score"], (int, float))
        assert 0 <= data["average_score"] <= 100
