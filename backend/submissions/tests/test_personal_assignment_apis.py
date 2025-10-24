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

        # PersonalAssignment 필드 확인
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
