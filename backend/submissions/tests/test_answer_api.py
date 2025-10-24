"""
AnswerSubmitView API 유닛 테스트
- 음성 파일 업로드 및 답안 제출 API 테스트
- Mock을 사용하여 외부 의존성(STT, ML 추론, Tail Question 생성) 격리
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

# 모든 테스트에서 DB 접근 허용
pytestmark = pytest.mark.django_db


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
def student():
    return Account.objects.create_user(
        email="student@test.com", password="testpass123", display_name="Test Student", is_student=True
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
def personal_assignment(student, assignment):
    """테스트용 PersonalAssignment"""
    return PersonalAssignment.objects.create(
        student=student,
        assignment=assignment,
        status=PersonalAssignment.Status.NOT_STARTED,
        solved_num=0,
    )


@pytest.fixture
def question(personal_assignment):
    """테스트용 Question"""
    return Question.objects.create(
        personal_assignment=personal_assignment,
        number=1,
        content="테스트 질문입니다.",
        model_answer="테스트 정답입니다.",
        explanation="테스트 설명입니다.",
        difficulty=Question.Difficulty.MEDIUM,
        recalled_num=0,
    )


@pytest.fixture
def mock_audio_file():
    """테스트용 가짜 WAV 파일"""
    # 간단한 WAV 헤더를 가진 바이트 데이터 생성
    wav_header = b"RIFF" + b"\x00" * 4 + b"WAVE" + b"fmt " + b"\x00" * 20 + b"data" + b"\x00" * 4
    wav_data = wav_header + b"\x00" * 1000  # 1000 바이트의 오디오 데이터
    return io.BytesIO(wav_data)


@pytest.fixture
def mock_features():
    """테스트용 Mock Feature 데이터"""
    return {
        "script": "학생의 음성 답변 텍스트입니다.",
        "total_length": 10.5,  # 10.5초
        "total_silence_sec": 1.2,
        "percent_silence": 11.4,
        # ... 기타 음향 특징들
        "feature_1": 0.5,
        "feature_2": 0.7,
    }


@pytest.fixture
def mock_inference_result():
    """테스트용 Mock Inference 결과"""
    return {
        "pred_cont": 0.85,  # Confidence score
        "pred_label": 1,
    }


@pytest.fixture
def mock_tail_question_payload_ask():
    """테스트용 Mock Tail Question 생성 결과 (ASK plan)"""
    return {
        "is_correct": False,
        "confidence": 0.85,
        "plan": "ASK",
        "recalled_time": 1,
        "tail_question": {
            "question": "추가 질문입니다.",
            "model_answer": "추가 정답입니다.",
            "explanation": "추가 설명입니다.",
            "difficulty": "medium",
        },
    }


@pytest.fixture
def mock_tail_question_payload_pass():
    """테스트용 Mock Tail Question 생성 결과 (PASS plan)"""
    return {
        "is_correct": True,
        "confidence": 0.95,
        "plan": "PASS",
        "recalled_time": 0,
    }


# ============================================================================
# 1. 기본 파라미터 검증 테스트
# ============================================================================


def test_answer_submit_missing_student_id(api_client, question, mock_audio_file):
    """studentId가 없을 때 400 에러 반환"""
    url = reverse("answer")

    mock_audio_file.name = "test.wav"

    data = {
        # "studentId": 생략
        "questionId": question.id,
        "audioFile": mock_audio_file,
    }

    response = api_client.post(url, data, format="multipart")

    assert response.status_code == status.HTTP_400_BAD_REQUEST
    assert response.data["success"] is False
    assert "studentId" in response.data["message"]


def test_answer_submit_missing_question_id(api_client, student, mock_audio_file):
    """questionId가 없을 때 400 에러 반환"""
    url = reverse("answer")

    mock_audio_file.name = "test.wav"

    data = {
        "studentId": student.id,
        # "questionId": 생략
        "audioFile": mock_audio_file,
    }

    response = api_client.post(url, data, format="multipart")

    assert response.status_code == status.HTTP_400_BAD_REQUEST
    assert response.data["success"] is False
    assert "questionId" in response.data["message"]


def test_answer_submit_missing_audio_file(api_client, student, question):
    """audioFile이 없을 때 400 에러 반환"""
    url = reverse("answer")

    data = {
        "studentId": student.id,
        "questionId": question.id,
        # "audioFile": 생략
    }

    response = api_client.post(url, data, format="multipart")

    assert response.status_code == status.HTTP_400_BAD_REQUEST
    assert response.data["success"] is False
    assert "audioFile" in response.data["message"]


def test_answer_submit_invalid_file_format(api_client, student, question):
    """WAV 파일이 아닐 때 400 에러 반환"""
    url = reverse("answer")

    invalid_file = io.BytesIO(b"some audio data")
    invalid_file.name = "test.mp3"  # WAV가 아닌 파일

    data = {
        "studentId": student.id,
        "questionId": question.id,
        "audioFile": invalid_file,
    }

    response = api_client.post(url, data, format="multipart")

    assert response.status_code == status.HTTP_400_BAD_REQUEST
    assert response.data["success"] is False
    assert ".wav" in response.data["message"]


# ============================================================================
# 2. DB 검증 테스트
# ============================================================================


def test_answer_submit_student_not_found(api_client, question, mock_audio_file):
    """존재하지 않는 학생 ID로 요청 시 404 에러 반환"""
    url = reverse("answer")

    mock_audio_file.name = "test.wav"

    data = {
        "studentId": 99999,  # 존재하지 않는 ID
        "questionId": question.id,
        "audioFile": mock_audio_file,
    }

    response = api_client.post(url, data, format="multipart")

    assert response.status_code == status.HTTP_404_NOT_FOUND
    assert response.data["success"] is False
    assert "학생을 찾을 수 없습니다" in response.data["message"]


def test_answer_submit_question_not_found(api_client, student, mock_audio_file):
    """존재하지 않는 문제 ID로 요청 시 404 에러 반환"""
    url = reverse("answer")

    mock_audio_file.name = "test.wav"

    data = {
        "studentId": student.id,
        "questionId": 99999,  # 존재하지 않는 ID
        "audioFile": mock_audio_file,
    }

    response = api_client.post(url, data, format="multipart")

    assert response.status_code == status.HTTP_404_NOT_FOUND
    assert response.data["success"] is False
    assert "문제를 찾을 수 없습니다" in response.data["message"]


def test_answer_submit_non_student_account(api_client, teacher, question, mock_audio_file):
    """학생이 아닌 계정으로 요청 시 404 에러 반환"""
    url = reverse("answer")

    mock_audio_file.name = "test.wav"

    data = {
        "studentId": teacher.id,  # 교사 계정
        "questionId": question.id,
        "audioFile": mock_audio_file,
    }

    response = api_client.post(url, data, format="multipart")

    assert response.status_code == status.HTTP_404_NOT_FOUND
    assert response.data["success"] is False


# ============================================================================
# 3. STT 실패 테스트
# ============================================================================


@patch("submissions.views.extract_all_features")
def test_answer_submit_stt_failed_empty_transcript(
    mock_extract_features, api_client, student, question, mock_audio_file
):
    """STT 결과가 빈 문자열일 때 400 에러 반환"""
    url = reverse("answer")

    # STT 결과가 빈 문자열인 경우
    mock_extract_features.return_value = {
        "script": "",  # 빈 문자열
        "total_length": 10.0,
    }

    mock_audio_file.name = "test.wav"

    data = {
        "studentId": student.id,
        "questionId": question.id,
        "audioFile": mock_audio_file,
    }

    response = api_client.post(url, data, format="multipart")

    assert response.status_code == status.HTTP_400_BAD_REQUEST
    assert response.data["success"] is False
    assert "음성 인식 결과가 없습니다" in response.data["message"]


# ============================================================================
# 4. ML 추론 실패 테스트
# ============================================================================


@patch("submissions.views.generate_tail_question")
@patch("submissions.views.run_inference")
@patch("submissions.views.extract_all_features")
def test_answer_submit_inference_failed(
    mock_extract_features, mock_inference, mock_tail_gen, api_client, student, question, mock_audio_file, mock_features
):
    """ML 추론 실패 시 500 에러 반환"""
    url = reverse("answer")

    mock_extract_features.return_value = mock_features
    mock_inference.side_effect = Exception("Inference error")

    mock_audio_file.name = "test.wav"

    data = {
        "studentId": student.id,
        "questionId": question.id,
        "audioFile": mock_audio_file,
    }

    response = api_client.post(url, data, format="multipart")

    assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
    assert response.data["success"] is False
    assert "답변 평가 중 오류가 발생했습니다" in response.data["message"]


# ============================================================================
# 5. Tail Question 생성 실패 테스트
# ============================================================================


@patch("submissions.views.generate_tail_question")
@patch("submissions.views.run_inference")
@patch("submissions.views.extract_all_features")
def test_answer_submit_tail_question_generation_failed(
    mock_extract_features,
    mock_inference,
    mock_tail_gen,
    api_client,
    student,
    question,
    mock_audio_file,
    mock_features,
    mock_inference_result,
):
    """Tail Question 생성 실패 시 500 에러 반환"""
    url = reverse("answer")

    mock_extract_features.return_value = mock_features
    mock_inference.return_value = mock_inference_result
    mock_tail_gen.side_effect = Exception("Tail question generation error")

    mock_audio_file.name = "test.wav"

    data = {
        "studentId": student.id,
        "questionId": question.id,
        "audioFile": mock_audio_file,
    }

    response = api_client.post(url, data, format="multipart")

    assert response.status_code == status.HTTP_500_INTERNAL_SERVER_ERROR
    assert response.data["success"] is False
    assert "꼬리 질문 생성 중 오류가 발생했습니다" in response.data["message"]


# ============================================================================
# 6. 정상 동작 테스트 - PASS plan (tail question 없음)
# ============================================================================


@patch("submissions.views.generate_tail_question")
@patch("submissions.views.run_inference")
@patch("submissions.views.extract_all_features")
def test_answer_submit_success_pass_plan(
    mock_extract_features,
    mock_inference,
    mock_tail_gen,
    api_client,
    student,
    question,
    mock_audio_file,
    mock_features,
    mock_inference_result,
    mock_tail_question_payload_pass,
):
    """정답으로 판정되어 tail question이 생성되지 않는 경우 (PASS plan)"""
    url = reverse("answer")

    mock_extract_features.return_value = mock_features
    mock_inference.return_value = mock_inference_result
    mock_tail_gen.return_value = mock_tail_question_payload_pass

    mock_audio_file.name = "test.wav"

    data = {
        "studentId": student.id,
        "questionId": question.id,
        "audioFile": mock_audio_file,
    }

    response = api_client.post(url, data, format="multipart")

    # 응답 검증
    assert response.status_code == status.HTTP_201_CREATED
    assert response.data["success"] is True
    assert response.data["data"]["is_correct"] is True
    assert response.data["data"]["tail_question"] is None

    # Answer 레코드 생성 확인
    answer = Answer.objects.get(question=question, student=student)
    assert answer.text_answer == mock_features["script"]
    assert answer.state == Answer.State.CORRECT
    assert answer.eval_grade == mock_inference_result["pred_cont"]
    assert answer.started_at is not None
    assert answer.submitted_at is not None

    # Tail Question이 생성되지 않았는지 확인
    tail_questions = Question.objects.filter(personal_assignment=question.personal_assignment, recalled_num__gt=0)
    assert tail_questions.count() == 0


# ============================================================================
# 7. 정상 동작 테스트 - ASK plan (tail question 생성)
# ============================================================================


@patch("submissions.views.generate_tail_question")
@patch("submissions.views.run_inference")
@patch("submissions.views.extract_all_features")
def test_answer_submit_success_ask_plan(
    mock_extract_features,
    mock_inference,
    mock_tail_gen,
    api_client,
    student,
    question,
    mock_audio_file,
    mock_features,
    mock_inference_result,
    mock_tail_question_payload_ask,
):
    """오답으로 판정되어 tail question이 생성되는 경우 (ASK plan)"""
    url = reverse("answer")

    mock_extract_features.return_value = mock_features
    mock_inference.return_value = mock_inference_result
    mock_tail_gen.return_value = mock_tail_question_payload_ask

    mock_audio_file.name = "test.wav"

    data = {
        "studentId": student.id,
        "questionId": question.id,
        "audioFile": mock_audio_file,
    }

    response = api_client.post(url, data, format="multipart")

    # 응답 검증
    assert response.status_code == status.HTTP_201_CREATED
    assert response.data["success"] is True
    assert response.data["data"]["is_correct"] is False
    assert response.data["data"]["tail_question"] is not None

    # Tail Question 데이터 검증
    tail_q = response.data["data"]["tail_question"]
    assert tail_q["question"] == mock_tail_question_payload_ask["tail_question"]["question"]
    assert tail_q["answer"] == mock_tail_question_payload_ask["tail_question"]["model_answer"]

    # Answer 레코드 생성 확인
    answer = Answer.objects.get(question=question, student=student)
    assert answer.text_answer == mock_features["script"]
    assert answer.state == Answer.State.INCORRECT
    assert answer.eval_grade == mock_inference_result["pred_cont"]

    # Tail Question이 생성되었는지 확인
    tail_questions = Question.objects.filter(personal_assignment=question.personal_assignment, recalled_num__gt=0)
    assert tail_questions.count() == 1

    tail_question = tail_questions.first()
    assert tail_question.content == mock_tail_question_payload_ask["tail_question"]["question"]
    assert tail_question.model_answer == mock_tail_question_payload_ask["tail_question"]["model_answer"]
    assert tail_question.base_question == question
    assert tail_question.recalled_num == 1


# ============================================================================
# 8. Answer 업데이트 테스트
# ============================================================================


@patch("submissions.views.generate_tail_question")
@patch("submissions.views.run_inference")
@patch("submissions.views.extract_all_features")
def test_answer_submit_update_existing_answer(
    mock_extract_features,
    mock_inference,
    mock_tail_gen,
    api_client,
    student,
    question,
    mock_audio_file,
    mock_features,
    mock_inference_result,
    mock_tail_question_payload_pass,
):
    """기존 Answer가 있을 때 업데이트되는지 확인"""
    url = reverse("answer")

    # 기존 Answer 생성
    existing_answer = Answer.objects.create(
        question=question,
        student=student,
        text_answer="이전 답변",
        state=Answer.State.INCORRECT,
        eval_grade=0.5,
        started_at=timezone.now(),
        submitted_at=timezone.now(),
    )

    mock_extract_features.return_value = mock_features
    mock_inference.return_value = mock_inference_result
    mock_tail_gen.return_value = mock_tail_question_payload_pass

    mock_audio_file.name = "test.wav"

    data = {
        "studentId": student.id,
        "questionId": question.id,
        "audioFile": mock_audio_file,
    }

    response = api_client.post(url, data, format="multipart")

    assert response.status_code == status.HTTP_201_CREATED

    # Answer가 하나만 존재하는지 확인 (새로 생성되지 않고 업데이트됨)
    answers = Answer.objects.filter(question=question, student=student)
    assert answers.count() == 1

    # 업데이트된 내용 확인
    updated_answer = answers.first()
    assert updated_answer.id == existing_answer.id  # 같은 레코드
    assert updated_answer.text_answer == mock_features["script"]  # 업데이트됨
    assert updated_answer.state == Answer.State.CORRECT  # 업데이트됨
    assert updated_answer.eval_grade == mock_inference_result["pred_cont"]  # 업데이트됨


# ============================================================================
# 9. 음성 파일 길이 기반 started_at 계산 테스트
# ============================================================================


@patch("submissions.views.generate_tail_question")
@patch("submissions.views.run_inference")
@patch("submissions.views.extract_all_features")
def test_answer_submit_started_at_calculation(
    mock_extract_features,
    mock_inference,
    mock_tail_gen,
    api_client,
    student,
    question,
    mock_audio_file,
    mock_inference_result,
    mock_tail_question_payload_pass,
):
    """음성 파일 길이를 기반으로 started_at이 올바르게 계산되는지 확인"""
    url = reverse("answer")

    # 음성 파일 길이 15.5초
    features_with_length = {
        "script": "학생의 답변",
        "total_length": 15.5,
        "feature_1": 0.5,
    }

    mock_extract_features.return_value = features_with_length
    mock_inference.return_value = mock_inference_result
    mock_tail_gen.return_value = mock_tail_question_payload_pass

    mock_audio_file.name = "test.wav"

    before_request_time = timezone.now()

    data = {
        "studentId": student.id,
        "questionId": question.id,
        "audioFile": mock_audio_file,
    }

    response = api_client.post(url, data, format="multipart")

    after_request_time = timezone.now()

    assert response.status_code == status.HTTP_201_CREATED

    answer = Answer.objects.get(question=question, student=student)

    # started_at이 submitted_at보다 약 15.5초 이전이어야 함
    time_diff = (answer.submitted_at - answer.started_at).total_seconds()
    assert 14.0 < time_diff < 17.0  # 오차 범위 허용

    # started_at이 요청 시간보다 이전이어야 함
    assert answer.started_at < after_request_time


# ============================================================================
# 10. PersonalAssignment 상태 변경 테스트
# ============================================================================


@patch("submissions.views.generate_tail_question")
@patch("submissions.views.run_inference")
@patch("submissions.views.extract_all_features")
def test_personal_assignment_status_not_started_to_in_progress(
    mock_extract_features,
    mock_inference,
    mock_tail_gen,
    api_client,
    student,
    question,
    personal_assignment,
    mock_audio_file,
    mock_features,
    mock_inference_result,
    mock_tail_question_payload_pass,
):
    """PersonalAssignment 상태가 NOT_STARTED에서 IN_PROGRESS로 변경되는지 테스트"""
    url = reverse("answer")

    # PersonalAssignment 상태를 NOT_STARTED로 설정
    personal_assignment.status = PersonalAssignment.Status.NOT_STARTED
    personal_assignment.save()

    mock_extract_features.return_value = mock_features
    mock_inference.return_value = mock_inference_result
    mock_tail_gen.return_value = mock_tail_question_payload_pass

    mock_audio_file.name = "test.wav"

    data = {
        "studentId": student.id,
        "questionId": question.id,
        "audioFile": mock_audio_file,
    }

    response = api_client.post(url, data, format="multipart")

    assert response.status_code == status.HTTP_201_CREATED

    # PersonalAssignment 상태가 IN_PROGRESS로 변경되었는지 확인
    personal_assignment.refresh_from_db()
    assert personal_assignment.status == PersonalAssignment.Status.IN_PROGRESS


@patch("submissions.views.generate_tail_question")
@patch("submissions.views.run_inference")
@patch("submissions.views.extract_all_features")
def test_personal_assignment_status_in_progress_remains(
    mock_extract_features,
    mock_inference,
    mock_tail_gen,
    api_client,
    student,
    question,
    personal_assignment,
    mock_audio_file,
    mock_features,
    mock_inference_result,
    mock_tail_question_payload_ask,
):
    """PersonalAssignment 상태가 이미 IN_PROGRESS일 때 그대로 유지되는지 테스트"""
    url = reverse("answer")

    # PersonalAssignment 상태를 IN_PROGRESS로 설정
    personal_assignment.status = PersonalAssignment.Status.IN_PROGRESS
    personal_assignment.save()

    mock_extract_features.return_value = mock_features
    mock_inference.return_value = mock_inference_result
    mock_tail_gen.return_value = mock_tail_question_payload_ask

    mock_audio_file.name = "test.wav"

    data = {
        "studentId": student.id,
        "questionId": question.id,
        "audioFile": mock_audio_file,
    }

    response = api_client.post(url, data, format="multipart")

    assert response.status_code == status.HTTP_201_CREATED

    # PersonalAssignment 상태가 여전히 IN_PROGRESS인지 확인
    personal_assignment.refresh_from_db()
    assert personal_assignment.status == PersonalAssignment.Status.IN_PROGRESS


@pytest.fixture
def mock_tail_question_payload_only_correct():
    """테스트용 Mock Tail Question 생성 결과 (ONLY_CORRECT plan)"""
    return {
        "is_correct": True,
        "confidence": 0.95,
        "plan": "ONLY_CORRECT",
        "recalled_time": 0,
    }


@patch("submissions.views.generate_tail_question")
@patch("submissions.views.run_inference")
@patch("submissions.views.extract_all_features")
def test_personal_assignment_solved_num_increment_only_correct(
    mock_extract_features,
    mock_inference,
    mock_tail_gen,
    api_client,
    student,
    question,
    personal_assignment,
    mock_audio_file,
    mock_features,
    mock_inference_result,
    mock_tail_question_payload_only_correct,
):
    """plan이 ONLY_CORRECT이고 정답일 때 solved_num이 증가하는지 테스트"""
    url = reverse("answer")

    # PersonalAssignment 초기 상태 설정
    personal_assignment.status = PersonalAssignment.Status.IN_PROGRESS
    personal_assignment.solved_num = 0
    personal_assignment.save()

    mock_extract_features.return_value = mock_features
    mock_inference.return_value = mock_inference_result
    mock_tail_gen.return_value = mock_tail_question_payload_only_correct

    mock_audio_file.name = "test.wav"

    data = {
        "studentId": student.id,
        "questionId": question.id,
        "audioFile": mock_audio_file,
    }

    response = api_client.post(url, data, format="multipart")

    assert response.status_code == status.HTTP_201_CREATED

    # solved_num이 1 증가했는지 확인
    personal_assignment.refresh_from_db()
    assert personal_assignment.solved_num == 1
    assert personal_assignment.status == PersonalAssignment.Status.SUBMITTED


@patch("submissions.views.generate_tail_question")
@patch("submissions.views.run_inference")
@patch("submissions.views.extract_all_features")
def test_personal_assignment_solved_num_not_increment_when_incorrect(
    mock_extract_features,
    mock_inference,
    mock_tail_gen,
    api_client,
    student,
    question,
    personal_assignment,
    mock_audio_file,
    mock_features,
    mock_inference_result,
):
    """plan이 ONLY_CORRECT이지만 오답일 때 solved_num이 증가하지 않는지 테스트"""
    url = reverse("answer")

    # PersonalAssignment 초기 상태 설정
    personal_assignment.status = PersonalAssignment.Status.IN_PROGRESS
    personal_assignment.solved_num = 0
    personal_assignment.save()

    # 오답으로 판정되는 payload
    only_correct_but_incorrect = {
        "is_correct": False,
        "confidence": 0.55,
        "plan": "ONLY_CORRECT",
        "recalled_time": 0,
    }

    mock_extract_features.return_value = mock_features
    mock_inference.return_value = mock_inference_result
    mock_tail_gen.return_value = only_correct_but_incorrect

    mock_audio_file.name = "test.wav"

    data = {
        "studentId": student.id,
        "questionId": question.id,
        "audioFile": mock_audio_file,
    }

    response = api_client.post(url, data, format="multipart")

    assert response.status_code == status.HTTP_201_CREATED

    # solved_num이 증가하지 않았는지 확인
    personal_assignment.refresh_from_db()
    assert personal_assignment.solved_num == 0
    # 상태가 SUBMITTED로 변경되지 않았는지 확인
    assert personal_assignment.status == PersonalAssignment.Status.IN_PROGRESS


@patch("submissions.views.generate_tail_question")
@patch("submissions.views.run_inference")
@patch("submissions.views.extract_all_features")
def test_personal_assignment_solved_num_not_increment_with_pass_plan(
    mock_extract_features,
    mock_inference,
    mock_tail_gen,
    api_client,
    student,
    question,
    personal_assignment,
    mock_audio_file,
    mock_features,
    mock_inference_result,
    mock_tail_question_payload_pass,
):
    """plan이 PASS일 때 solved_num이 증가하지 않는지 테스트"""
    url = reverse("answer")

    # PersonalAssignment 초기 상태 설정
    personal_assignment.status = PersonalAssignment.Status.IN_PROGRESS
    personal_assignment.solved_num = 0
    personal_assignment.save()

    mock_extract_features.return_value = mock_features
    mock_inference.return_value = mock_inference_result
    mock_tail_gen.return_value = mock_tail_question_payload_pass

    mock_audio_file.name = "test.wav"

    data = {
        "studentId": student.id,
        "questionId": question.id,
        "audioFile": mock_audio_file,
    }

    response = api_client.post(url, data, format="multipart")

    assert response.status_code == status.HTTP_201_CREATED

    # solved_num이 증가하지 않았는지 확인 (PASS는 solved_num을 증가시키지 않음)
    personal_assignment.refresh_from_db()
    assert personal_assignment.solved_num == 0
    # 상태는 여전히 IN_PROGRESS
    assert personal_assignment.status == PersonalAssignment.Status.IN_PROGRESS


@patch("submissions.views.generate_tail_question")
@patch("submissions.views.run_inference")
@patch("submissions.views.extract_all_features")
def test_personal_assignment_status_from_not_started_to_submitted(
    mock_extract_features,
    mock_inference,
    mock_tail_gen,
    api_client,
    student,
    question,
    personal_assignment,
    mock_audio_file,
    mock_features,
    mock_inference_result,
    mock_tail_question_payload_only_correct,
):
    """NOT_STARTED 상태에서 ONLY_CORRECT로 정답 제출 시 IN_PROGRESS를 거쳐 SUBMITTED로 변경되는지 테스트"""
    url = reverse("answer")

    # PersonalAssignment 초기 상태 설정
    personal_assignment.status = PersonalAssignment.Status.NOT_STARTED
    personal_assignment.solved_num = 0
    personal_assignment.save()

    mock_extract_features.return_value = mock_features
    mock_inference.return_value = mock_inference_result
    mock_tail_gen.return_value = mock_tail_question_payload_only_correct

    mock_audio_file.name = "test.wav"

    data = {
        "studentId": student.id,
        "questionId": question.id,
        "audioFile": mock_audio_file,
    }

    response = api_client.post(url, data, format="multipart")

    assert response.status_code == status.HTTP_201_CREATED

    # PersonalAssignment 상태가 SUBMITTED로 변경되었는지 확인
    personal_assignment.refresh_from_db()
    assert personal_assignment.status == PersonalAssignment.Status.SUBMITTED
    assert personal_assignment.solved_num == 1
