from datetime import timedelta
from unittest.mock import patch

import pytest
from assignments.models import Assignment
from catalog.models import Subject
from courses.models import CourseClass, Enrollment
from django.contrib.auth import get_user_model
from django.urls import reverse
from django.utils import timezone
from questions.models import Question
from rest_framework import status
from rest_framework.test import APIClient
from submissions.models import Answer, PersonalAssignment

Account = get_user_model()

# 모든 테스트에서 DB 접근 허용
pytestmark = pytest.mark.django_db

# pytest 실행 예시
# pytest reports/tests/test_reports_apis.py -v


@pytest.fixture
def api_client():
    return APIClient()


@pytest.fixture
def teacher():
    return Account.objects.create_user(
        email="teacher@test.com", password="testpass123", display_name="Test Teacher", is_student=False
    )


@pytest.fixture
def student():
    return Account.objects.create_user(
        email="student@test.com", password="testpass123", display_name="Test Student", is_student=True
    )


@pytest.fixture
def subject():
    return Subject.objects.create(name="과학")


@pytest.fixture
def course_class(teacher, subject):
    return CourseClass.objects.create(
        teacher=teacher,
        subject=subject,
        name="과학 기초반",
        description="과학 기초 과정",
        start_date=timezone.now(),
        end_date=timezone.now() + timedelta(days=90),
    )


@pytest.fixture
def enrollment(student, course_class):
    return Enrollment.objects.create(student=student, course_class=course_class, status=Enrollment.Status.ENROLLED)


@pytest.fixture
def assignment(course_class, subject):
    return Assignment.objects.create(
        course_class=course_class,
        subject=subject,
        title="호흡과 배설 단원 과제",
        description="호흡 운동의 원리와 배설의 과정의 이해도를 평가하는 과제입니다.",
        due_at=timezone.now() + timedelta(days=2),
        grade="중학교 2학년",
    )


@pytest.fixture
def personal_assignment(student, assignment):
    return PersonalAssignment.objects.create(
        student=student,
        assignment=assignment,
        status=PersonalAssignment.Status.SUBMITTED,
        started_at=timezone.now() - timedelta(hours=2),
        submitted_at=timezone.now() - timedelta(hours=1),
    )


@pytest.fixture
def question(personal_assignment):
    return Question.objects.create(
        personal_assignment=personal_assignment,
        number=1,
        content="호흡 운동에서 가슴이 부풀어 오르는 이유는 무엇인가요?",
        topic="호흡과 배설",
        achievement_code="4과01-01",
        recalled_num=0,
        explanation="호흡 운동 시 횡격막과 늑간근의 수축으로 인해 흉강의 부피가 증가합니다.",
        model_answer="횡격막과 늑간근이 수축하여 흉강의 부피가 증가하기 때문입니다.",
        difficulty=Question.Difficulty.MEDIUM,
    )


@pytest.fixture
def answer(student, question):
    return Answer.objects.create(
        question=question,
        student=student,
        started_at=timezone.now() - timedelta(hours=2),
        submitted_at=timezone.now() - timedelta(hours=1),
        state=Answer.State.CORRECT,
        text_answer="횡격막과 늑간근이 수축하여 흉강의 부피가 증가하기 때문입니다.",
        eval_grade=0.9,
    )


class TestCurriculumAnalysisView:
    """성취기준 분석 API 테스트"""

    @patch("reports.views.parse_curriculum")
    def test_curriculum_analysis_success(
        self, mock_parse_curriculum, api_client, student, course_class, question, answer
    ):
        """성취기준 분석 성공 테스트"""
        # Mock parse_curriculum 반환값
        mock_statistics = {
            "total_questions": 1,
            "total_correct": 1,
            "overall_accuracy": 100.0,
            "achievement_statistics": {
                "4과01-01": {
                    "total_questions": 1,
                    "correct_questions": 1,
                    "accuracy": 100.0,
                    "content": "호흡 운동의 원리와 배설의 과정을 이해하고, 호흡계와 배설계의 구조와 기능을 설명할 수 있다.",
                }
            },
        }
        mock_parse_curriculum.return_value = mock_statistics

        url = reverse("reports:report-analysis", kwargs={"class_id": course_class.id, "student_id": student.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert response.data["data"]["total_questions"] == 1
        assert response.data["data"]["total_correct"] == 1
        assert response.data["data"]["overall_accuracy"] == 100.0
        assert "4과01-01" in response.data["data"]["achievement_statistics"]

        # parse_curriculum이 올바른 인자로 호출되었는지 확인
        mock_parse_curriculum.assert_called_once_with(student.id, course_class.id)

    def test_curriculum_analysis_invalid_class_id(self, api_client, student):
        """잘못된 클래스 ID 테스트"""
        url = reverse("reports:report-analysis", kwargs={"class_id": 0, "student_id": student.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_400_BAD_REQUEST
        assert response.data["success"] is False
        assert "클래스 ID는 양수여야 합니다" in response.data["message"]

    def test_curriculum_analysis_invalid_student_id(self, api_client, course_class):
        """잘못된 학생 ID 테스트"""
        url = reverse("reports:report-analysis", kwargs={"class_id": course_class.id, "student_id": 0})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_400_BAD_REQUEST
        assert response.data["success"] is False
        assert "학생 ID는 양수여야 합니다" in response.data["message"]

    def test_curriculum_analysis_negative_class_id(self, api_client, student):
        """음수 클래스 ID 테스트"""
        # Django URL 패턴이 양수만 허용하므로 직접 URL을 구성
        url = f"/api/reports/-1/{student.id}/"
        response = api_client.get(url)

        assert response.status_code == status.HTTP_404_NOT_FOUND

    def test_curriculum_analysis_negative_student_id(self, api_client, course_class):
        """음수 학생 ID 테스트"""
        # Django URL 패턴이 양수만 허용하므로 직접 URL을 구성
        url = f"/api/reports/{course_class.id}/-1/"
        response = api_client.get(url)

        assert response.status_code == status.HTTP_404_NOT_FOUND

    @patch("reports.views.parse_curriculum")
    def test_curriculum_analysis_parse_error(self, mock_parse_curriculum, api_client, student, course_class):
        """parse_curriculum 함수 오류 테스트"""
        mock_parse_curriculum.side_effect = Exception("Database connection error")

        url = reverse("reports:report-analysis", kwargs={"class_id": course_class.id, "student_id": student.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
        assert response.data["success"] is False
        assert "성취기준 분석 중 오류가 발생했습니다" in response.data["message"]

    @patch("reports.views.parse_curriculum")
    def test_curriculum_analysis_invalid_response_data(self, mock_parse_curriculum, api_client, student, course_class):
        """잘못된 응답 데이터 테스트"""
        # 잘못된 형식의 통계량 반환
        mock_statistics = {
            "total_questions": "invalid",  # 문자열이어야 하는데 정수여야 함
            "total_correct": 1,
            "overall_accuracy": 100.0,
            "achievement_statistics": {},
        }
        mock_parse_curriculum.return_value = mock_statistics

        url = reverse("reports:report-analysis", kwargs={"class_id": course_class.id, "student_id": student.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
        assert response.data["success"] is False
        assert "응답 데이터 생성 중 오류가 발생했습니다" in response.data["message"]

    @patch("reports.views.parse_curriculum")
    def test_curriculum_analysis_empty_statistics(self, mock_parse_curriculum, api_client, student, course_class):
        """빈 통계량 테스트"""
        mock_statistics = {
            "total_questions": 0,
            "total_correct": 0,
            "overall_accuracy": 0.0,
            "achievement_statistics": {},
        }
        mock_parse_curriculum.return_value = mock_statistics

        url = reverse("reports:report-analysis", kwargs={"class_id": course_class.id, "student_id": student.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert response.data["data"]["total_questions"] == 0
        assert response.data["data"]["achievement_statistics"] == {}

    @patch("reports.views.parse_curriculum")
    def test_curriculum_analysis_multiple_achievements(self, mock_parse_curriculum, api_client, student, course_class):
        """여러 성취기준이 있는 경우 테스트"""
        mock_statistics = {
            "total_questions": 3,
            "total_correct": 2,
            "overall_accuracy": 66.7,
            "achievement_statistics": {
                "4과01-01": {
                    "total_questions": 2,
                    "correct_questions": 1,
                    "accuracy": 50.0,
                    "content": "호흡 운동의 원리와 배설의 과정을 이해하고, 호흡계와 배설계의 구조와 기능을 설명할 수 있다.",
                },
                "4과01-02": {
                    "total_questions": 1,
                    "correct_questions": 1,
                    "accuracy": 100.0,
                    "content": "신장의 구조와 기능을 이해하고, 배설의 과정을 설명할 수 있다.",
                },
            },
        }
        mock_parse_curriculum.return_value = mock_statistics

        url = reverse("reports:report-analysis", kwargs={"class_id": course_class.id, "student_id": student.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK
        assert response.data["success"] is True
        assert response.data["data"]["total_questions"] == 3
        assert response.data["data"]["total_correct"] == 2
        assert response.data["data"]["overall_accuracy"] == 66.7

        achievement_stats = response.data["data"]["achievement_statistics"]
        assert len(achievement_stats) == 2
        assert "4과01-01" in achievement_stats
        assert "4과01-02" in achievement_stats
        assert achievement_stats["4과01-01"]["accuracy"] == 50.0
        assert achievement_stats["4과01-02"]["accuracy"] == 100.0

    @patch("reports.views.parse_curriculum")
    def test_curriculum_analysis_response_format(self, mock_parse_curriculum, api_client, student, course_class):
        """응답 형식 검증 테스트"""
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

        url = reverse("reports:report-analysis", kwargs={"class_id": course_class.id, "student_id": student.id})
        response = api_client.get(url)

        assert response.status_code == status.HTTP_200_OK

        # 응답 필드 확인
        assert "success" in response.data
        assert "data" in response.data
        assert "message" in response.data
        assert "error" in response.data

        # data 필드 확인
        data = response.data["data"]
        assert "total_questions" in data
        assert "total_correct" in data
        assert "overall_accuracy" in data
        assert "achievement_statistics" in data

        # 타입 확인
        assert isinstance(data["total_questions"], int)
        assert isinstance(data["total_correct"], int)
        assert isinstance(data["overall_accuracy"], float)
        assert isinstance(data["achievement_statistics"], dict)

    def test_curriculum_analysis_nonexistent_class(self, api_client, student):
        """존재하지 않는 클래스 ID 테스트"""
        url = reverse("reports:report-analysis", kwargs={"class_id": 99999, "student_id": student.id})
        response = api_client.get(url)

        # URL 파라미터 검증은 통과하지만, parse_curriculum에서 처리될 것
        assert response.status_code in [status.HTTP_200_OK, status.HTTP_500_INTERNAL_SERVER_ERROR]

    def test_curriculum_analysis_nonexistent_student(self, api_client, course_class):
        """존재하지 않는 학생 ID 테스트"""
        url = reverse("reports:report-analysis", kwargs={"class_id": course_class.id, "student_id": 99999})
        response = api_client.get(url)

        # URL 파라미터 검증은 통과하지만, parse_curriculum에서 처리될 것
        assert response.status_code in [status.HTTP_200_OK, status.HTTP_500_INTERNAL_SERVER_ERROR]
