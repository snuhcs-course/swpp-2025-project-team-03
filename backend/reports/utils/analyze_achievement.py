import csv
import json
import logging
import os
import threading
import time
from concurrent.futures import ThreadPoolExecutor, as_completed

import requests
from django.conf import settings
from django.db.models import Q
from questions.models import Question
from submissions.models import Answer, PersonalAssignment

from .achievement_inference import filter_standards_by_model

logger = logging.getLogger(__name__)

# 성취기준을 찾을 수 없음을 나타내는 특별한 값
# 이 값이 저장되면 다음 실행 시 다시 처리하지 않음
ACHIEVEMENT_CODE_NOT_FOUND = "__NOT_FOUND__"

# Thread-safe storage for timing statistics
_timing_stats_lock = threading.Lock()


def _reset_timing_stats():
    """Reset timing statistics for a new batch of questions."""
    global _timing_stats
    with _timing_stats_lock:
        _timing_stats = {
            "model_times": [],  # RoBERTa model inference times
            "gpt_times": [],  # GPT API call times
            "total_times": [],  # Total time per question
        }


def _add_timing_stat(model_time: float, gpt_time: float, total_time: float):
    """Add timing statistics in a thread-safe manner."""
    with _timing_stats_lock:
        _timing_stats["model_times"].append(model_time)
        _timing_stats["gpt_times"].append(gpt_time)
        _timing_stats["total_times"].append(total_time)


def _log_timing_summary():
    """Log summary statistics for all processed questions."""
    with _timing_stats_lock:
        if not _timing_stats["total_times"]:
            logger.info("[TIMING] No questions were processed.")
            return

        n = len(_timing_stats["total_times"])
        model_times = _timing_stats["model_times"]
        gpt_times = _timing_stats["gpt_times"]
        total_times = _timing_stats["total_times"]

        logger.info("=" * 60)
        logger.info(f"[TIMING SUMMARY] 처리된 문제 수: {n}개")
        logger.info("-" * 60)

        if model_times:
            logger.info(
                f"[TIMING] RoBERTa Model: "
                f"평균={sum(model_times) / len(model_times):.3f}s, "
                f"최소={min(model_times):.3f}s, "
                f"최대={max(model_times):.3f}s, "
                f"총합={sum(model_times):.3f}s"
            )

        if gpt_times:
            logger.info(
                f"[TIMING] GPT API: "
                f"평균={sum(gpt_times) / len(gpt_times):.3f}s, "
                f"최소={min(gpt_times):.3f}s, "
                f"최대={max(gpt_times):.3f}s, "
                f"총합={sum(gpt_times):.3f}s"
            )

        logger.info(
            f"[TIMING] 문제당 총 시간: "
            f"평균={sum(total_times) / len(total_times):.3f}s, "
            f"최소={min(total_times):.3f}s, "
            f"최대={max(total_times):.3f}s"
        )
        logger.info("=" * 60)


def parse_curriculum(student_id, class_id):
    """
    특정 학생과 클래스에 해당하는 질문들의 성취기준을 자동으로 매핑하고 통계량을 계산하는 함수

    Args:
        student_id: 학생 ID
        class_id: 클래스 ID

    Returns:
        dict: 통계량 정보
        {
            'total_questions': 전체 문제 수,
            'total_correct': 맞춘 문제 수,
            'overall_accuracy': 전체 정답률 (%),
            'achievement_statistics': {
                'achievement_code': {
                    'total_questions': 해당 성취기준 문제 수,
                    'correct_questions': 맞춘 문제 수,
                    'accuracy': 정답률 (%),
                    'content': 성취기준 내용
                }
            }
        }
    """

    # 1. 해당 학생과 클래스에 해당하는 질문들 찾기
    # achievement_code가 null이거나 빈 문자열인 것만 처리
    # ACHIEVEMENT_CODE_NOT_FOUND 값이 있으면 이미 처리된 것이므로 제외
    questions = (
        Question.objects.filter(
            personal_assignment__student_id=student_id,
            personal_assignment__assignment__course_class_id=class_id,
        )
        .filter(Q(achievement_code__isnull=True) | Q(achievement_code=""))
        .select_related("personal_assignment__assignment__subject", "personal_assignment__assignment")
    )

    logger.info(
        f"Found {questions.count()} questions without achievement_code for student {student_id} in class {class_id}"
    )

    # 2. CSV 파일 읽기
    current_dir = os.path.dirname(os.path.abspath(__file__))
    csv_file_path = os.path.join(current_dir, "achievement_standards.csv")
    achievement_standards = []

    try:
        with open(csv_file_path, "r", encoding="cp949") as file:
            reader = csv.DictReader(file)
            for row in reader:
                achievement_standards.append(row)
    except UnicodeDecodeError:
        # cp949로 읽기 실패시 utf-8로 시도
        with open(csv_file_path, "r", encoding="utf-8") as file:
            reader = csv.DictReader(file)
            for row in reader:
                achievement_standards.append(row)

    logger.info(f"Loaded {len(achievement_standards)} achievement standards from CSV")

    # 3. 각 질문을 처리하는 헬퍼 함수
    def process_question(question, achievement_standards):
        try:
            # 과목명과 학년 정보 추출
            subject_name = question.personal_assignment.assignment.subject.name
            grade = question.personal_assignment.assignment.grade

            # 학년에서 학교 단계 추출
            school_level = None
            if "초" in grade:
                school_level = "초등학교"
            elif "중" in grade:
                school_level = "중학교"
            elif "고" in grade:
                school_level = "고등학교"

            if not school_level:
                logger.warning(f"Could not determine school level from grade '{grade}' for question {question.id}")
                return

            # 해당 과목과 학교 단계에 맞는 성취기준 필터링
            relevant_standards = [
                std for std in achievement_standards if std["subject"] == subject_name and std["school"] == school_level
            ]

            if not relevant_standards:
                logger.warning(
                    f"No achievement standards found for subject '{subject_name}' and school '{school_level}' for question {question.id}"
                )
                return

            logger.info(f"Processing Question {question.id}:")
            logger.info(f"Subject: {subject_name}, School: {school_level}")
            logger.debug(f"Question content: {question.content}")
            logger.info(f"Found {len(relevant_standards)} relevant achievement standards")

            # roberta와 GPT API를 통해 가장 적합한 성취기준 찾기
            best_achievement_code = find_best_achievement_code(
                question_content=question.content,
                answer=question.model_answer,
                explanation=question.explanation,
                achievement_standards=relevant_standards,
            )

            if best_achievement_code:
                # achievement_code 업데이트
                question.achievement_code = best_achievement_code
                question.save()
                logger.info(f"Updated achievement_code to: {best_achievement_code}")
            else:
                # 성취기준을 찾지 못한 경우 NOT_FOUND로 저장하여
                # 다음 실행 시 다시 처리하지 않도록 함
                question.achievement_code = ACHIEVEMENT_CODE_NOT_FOUND
                question.save()
                logger.warning(
                    f"Could not determine best achievement code for question {question.id}, "
                    f"marked as {ACHIEVEMENT_CODE_NOT_FOUND}"
                )

        except Exception as e:
            logger.error(f"Error processing question {question.id}: {str(e)}")

    # 모든 질문을 병렬로 처리 (최대 10개 동시 처리)
    questions_list = list(questions)
    logger.info(f"Processing {len(questions_list)} questions in parallel...")

    # Reset timing stats before processing
    _reset_timing_stats()

    with ThreadPoolExecutor(max_workers=10) as executor:
        futures = [executor.submit(process_question, question, achievement_standards) for question in questions_list]
        # 모든 작업 완료 대기
        for future in as_completed(futures):
            future.result()

    # Log timing summary after all questions are processed
    _log_timing_summary()

    # 4. 통계량 계산 (SUBMITTED 상태의 PersonalAssignment만 대상)
    statistics = calculate_statistics(student_id, class_id)

    return statistics


def calculate_statistics(student_id, class_id):
    """
    특정 학생과 클래스의 SUBMITTED 상태 PersonalAssignment에 대한 통계량을 계산하는 함수

    Args:
        student_id: 학생 ID
        class_id: 클래스 ID

    Returns:
        dict: 통계량 정보
    """

    # SUBMITTED 상태의 PersonalAssignment에 해당하는 질문들과 답안들 조회
    # 성취기준이 유효하게 설정된 것만 (null, 빈 문자열, NOT_FOUND 제외)
    submitted_questions = (
        Question.objects.filter(
            personal_assignment__student_id=student_id,
            personal_assignment__assignment__course_class_id=class_id,
            personal_assignment__status=PersonalAssignment.Status.SUBMITTED,
            achievement_code__isnull=False,
        )
        .exclude(achievement_code="")
        .exclude(achievement_code=ACHIEVEMENT_CODE_NOT_FOUND)
        .select_related("personal_assignment")
    )

    # 해당 질문들에 대한 답안들 조회
    answers = Answer.objects.filter(question__in=submitted_questions, student_id=student_id).select_related("question")

    logger.info("=== 통계량 계산 ===")
    logger.info(f"SUBMITTED 상태 질문 수: {submitted_questions.count()}")
    logger.info(f"답안 수: {answers.count()}")

    # 전체 통계량 계산
    total_questions = answers.count()
    total_correct = answers.filter(state=Answer.State.CORRECT).count()
    overall_accuracy = (total_correct / total_questions * 100) if total_questions > 0 else 0

    logger.info(f"전체 문제 수: {total_questions}")
    logger.info(f"맞춘 문제 수: {total_correct}")
    logger.info(f"전체 정답률: {overall_accuracy:.1f}%")

    # 성취기준별 통계량 계산
    achievement_statistics = {}

    # CSV 파일에서 성취기준 정보 가져오기
    current_dir = os.path.dirname(os.path.abspath(__file__))
    csv_file_path = os.path.join(current_dir, "achievement_standards.csv")
    achievement_standards_dict = {}

    try:
        with open(csv_file_path, "r", encoding="utf-8") as file:
            reader = csv.DictReader(file)
            for row in reader:
                achievement_standards_dict[row["code"]] = row["content"]
    except UnicodeDecodeError:
        # cp949로 읽기 실패시 utf-8로 시도
        with open(csv_file_path, "r", encoding="cp949") as file:
            reader = csv.DictReader(file)
            for row in reader:
                achievement_standards_dict[row["code"]] = row["content"]

    for answer in answers:
        achievement_code = answer.question.achievement_code

        if achievement_code not in achievement_statistics:
            achievement_statistics[achievement_code] = {
                "total_questions": 0,
                "correct_questions": 0,
                "accuracy": 0.0,
                "content": achievement_standards_dict.get(achievement_code, "성취기준 내용을 찾을 수 없습니다."),
            }

        achievement_statistics[achievement_code]["total_questions"] += 1

        if answer.state == Answer.State.CORRECT:
            achievement_statistics[achievement_code]["correct_questions"] += 1

    # 각 성취기준별 정답률 계산
    for achievement_code, stats in achievement_statistics.items():
        if stats["total_questions"] > 0:
            stats["accuracy"] = round(stats["correct_questions"] / stats["total_questions"] * 100, 1)

        logger.info(f"성취기준 {achievement_code}:")
        logger.info(f"문제 수: {stats['total_questions']}")
        logger.info(f"맞춘 문제 수: {stats['correct_questions']}")
        logger.info(f"정답률: {stats['accuracy']}%")
        logger.debug(f"내용: {stats['content']}")

    return {
        "total_questions": total_questions,
        "total_correct": total_correct,
        "overall_accuracy": round(overall_accuracy, 1),
        "achievement_statistics": achievement_statistics,
    }


def find_best_achievement_code(
    question_content, answer, explanation, achievement_standards, use_model_filtering=True, top_k=20
):
    """
    GPT API를 사용하여 질문 내용에 가장 적합한 성취기준을 찾는 함수

    먼저 trained model을 사용하여 top-k 성취기준을 필터링한 후,
    GPT에게 그 중에서 가장 적합한 것을 선택하도록 합니다.

    Args:
        question_content: 질문 내용
        answer: 모범 답안
        explanation: 해설
        achievement_standards: 성취기준 리스트
        use_model_filtering: True이면 trained model로 먼저 필터링 (기본값: True)
        top_k: model filtering 시 상위 몇 개를 사용할지 (기본값: 20)

    Returns:
        가장 적합한 성취기준의 code 또는 None
    """
    question_start_time = time.time()
    model_time = 0.0
    gpt_time = 0.0

    # OpenAI API 키가 설정되어 있는지 확인
    api_key = getattr(settings, "OPENAI_API_KEY", None)
    if not api_key:
        logger.warning("OPENAI_API_KEY not found in settings. Skipping GPT analysis.")
        return None

    # Step 1: trained model로 top-k 성취기준 필터링
    filtered_standards = achievement_standards
    if use_model_filtering and achievement_standards:
        try:
            model_start_time = time.time()
            filtered_standards = filter_standards_by_model(
                question_content=question_content,
                achievement_standards=achievement_standards,
                top_k=top_k,
            )
            model_time = time.time() - model_start_time
            logger.info(
                f"[TIMING] Model inference: {model_time:.3f}s - "
                f"Filtered {len(achievement_standards)} → {len(filtered_standards)} standards"
            )
        except Exception as e:
            logger.warning(f"Model filtering failed, using all standards: {str(e)}")
            filtered_standards = achievement_standards

    # Step 2: GPT를 사용하여 최종 선택
    # 성취기준들을 문자열로 변환
    standards_text = "\n".join(
        [f"Code: {std['code']}\nContent: {std['content']}\nGrade: {std['grade']}\n" for std in filtered_standards]
    )

    # 문제 정보를 문자열로 구성 (answer와 explanation이 있으면 포함)
    question_info = f"Question: {question_content}"
    if answer:
        question_info += f"\nAnswer: {answer}"
    if explanation:
        question_info += f"\nExplanation: {explanation}"

    # GPT API 요청 구성
    headers = {"Authorization": f"Bearer {api_key}", "Content-Type": "application/json"}

    prompt = f"""
The following are educational achievement standards:

{standards_text}

Please determine which of the above standards is the most appropriate for the following question, answer, and explanation:

{question_info}

Only return the Code of the most appropriate standard. No additional explanation is needed.
Do not return anything other than the Code. For example, just return '2과03-01' if that is the best match.

"""

    data = {
        "model": "gpt-4o",
        "messages": [
            {
                "role": "system",
                "content": "당신은 교육과정 성취기준 분석 전문가입니다. 주어진 질문이 어떤 성취기준에 가장 적합한지 정확히 판단할 수 있습니다.",
            },
            {"role": "user", "content": prompt},
        ],
        "max_tokens": 50,
        "temperature": 0.1,
    }

    try:
        gpt_start_time = time.time()
        response = requests.post(
            "https://api.openai.com/v1/chat/completions", headers=headers, data=json.dumps(data), timeout=30
        )
        gpt_time = time.time() - gpt_start_time

        # Record timing stats
        total_time = time.time() - question_start_time
        _add_timing_stat(model_time, gpt_time, total_time)
        logger.info(f"[TIMING] GPT API: {gpt_time:.3f}s, 문제 총: {total_time:.3f}s")

        if response.status_code == 200:
            result = response.json()

            achievement_code = result["choices"][0]["message"]["content"].strip()

            # 응답이 유효한 코드인지 확인 (filtered standards 기준)
            valid_codes = [std["code"] for std in filtered_standards]
            if achievement_code in valid_codes:
                return achievement_code
            else:
                logger.warning(f"GPT returned invalid code '{achievement_code}'. Valid codes: {valid_codes[:5]}...")
                return None
        else:
            logger.error(f"GPT API request failed with status code: {response.status_code}")
            logger.error(f"Response: {response.text}")
            return None

    except requests.exceptions.RequestException as e:
        # Record timing even on failure
        gpt_time = time.time() - gpt_start_time if "gpt_start_time" in locals() else 0.0
        total_time = time.time() - question_start_time
        _add_timing_stat(model_time, gpt_time, total_time)
        logger.error(f"GPT API request failed: {str(e)}")
        return None
    except Exception as e:
        # Record timing even on failure
        total_time = time.time() - question_start_time
        _add_timing_stat(model_time, gpt_time, total_time)
        logger.error(f"Error processing GPT response: {str(e)}")
        return None


if __name__ == "__main__":
    result = parse_curriculum(student_id=2, class_id=1)
    print("\n=== 최종 결과 ===")
    print(f"반환된 통계량: {result}")
