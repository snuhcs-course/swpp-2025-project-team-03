import csv
import json
import os
from concurrent.futures import ThreadPoolExecutor, as_completed

import requests
from django.conf import settings
from questions.models import Question
from submissions.models import Answer, PersonalAssignment


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
    questions = Question.objects.filter(
        personal_assignment__student_id=student_id,
        personal_assignment__assignment__course_class_id=class_id,
        achievement_code__isnull=True,  # achievement_code가 null인 것만
    ).select_related("personal_assignment__assignment__subject", "personal_assignment__assignment")

    print(f"Found {questions.count()} questions without achievement_code for student {student_id} in class {class_id}")

    # 2. CSV 파일 읽기
    current_dir = os.path.dirname(os.path.abspath(__file__))
    csv_file_path = os.path.join(current_dir, "achievement_standards.csv")
    achievement_standards = []

    try:
        with open(csv_file_path, "r", encoding="utf-8") as file:
            reader = csv.DictReader(file)
            for row in reader:
                achievement_standards.append(row)
    except UnicodeDecodeError:
        # cp949로 읽기 실패시 utf-8로 시도
        with open(csv_file_path, "r", encoding="cp949") as file:
            reader = csv.DictReader(file)
            for row in reader:
                achievement_standards.append(row)

    print(f"Loaded {len(achievement_standards)} achievement standards from CSV")

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
                print(f"Warning: Could not determine school level from grade '{grade}' for question {question.id}")
                return

            # 해당 과목과 학교 단계에 맞는 성취기준 필터링
            relevant_standards = [
                std for std in achievement_standards if std["subject"] == subject_name and std["school"] == school_level
            ]

            if not relevant_standards:
                print(
                    f"Warning: No achievement standards found for subject '{subject_name}' and school '{school_level}' for question {question.id}"
                )
                return

            print(f"\nProcessing Question {question.id}:")
            print(f"Subject: {subject_name}, School: {school_level}")
            print(f"Question content: {question.content}")
            print(f"Found {len(relevant_standards)} relevant achievement standards")

            # GPT API를 통해 가장 적합한 성취기준 찾기
            best_achievement_code = find_best_achievement_code(question.content, relevant_standards)

            if best_achievement_code:
                # achievement_code 업데이트
                question.achievement_code = best_achievement_code
                question.save()
                print(f"Updated achievement_code to: {best_achievement_code}")
            else:
                print(f"Could not determine best achievement code for question {question.id}")

        except Exception as e:
            print(f"Error processing question {question.id}: {str(e)}")

    # 모든 질문을 병렬로 처리 (최대 10개 동시 처리)
    questions_list = list(questions)
    print(f"\nProcessing {len(questions_list)} questions in parallel...")

    with ThreadPoolExecutor(max_workers=10) as executor:
        futures = [executor.submit(process_question, question, achievement_standards) for question in questions_list]
        # 모든 작업 완료 대기
        for future in as_completed(futures):
            future.result()

    # 4. 통계량 계산 (GRADED 상태의 PersonalAssignment만 대상)
    statistics = calculate_statistics(student_id, class_id)

    return statistics


def calculate_statistics(student_id, class_id):
    """
    특정 학생과 클래스의 GRADED 상태 PersonalAssignment에 대한 통계량을 계산하는 함수

    Args:
        student_id: 학생 ID
        class_id: 클래스 ID

    Returns:
        dict: 통계량 정보
    """

    # GRADED 상태의 PersonalAssignment에 해당하는 질문들과 답안들 조회
    graded_questions = Question.objects.filter(
        personal_assignment__student_id=student_id,
        personal_assignment__assignment__course_class_id=class_id,
        personal_assignment__status=PersonalAssignment.Status.GRADED,
        achievement_code__isnull=False,  # 성취기준이 설정된 것만
    ).select_related("personal_assignment")

    # 해당 질문들에 대한 답안들 조회
    answers = Answer.objects.filter(question__in=graded_questions, student_id=student_id).select_related("question")

    print("\n=== 통계량 계산 ===")
    print(f"GRADED 상태 질문 수: {graded_questions.count()}")
    print(f"답안 수: {answers.count()}")

    # 전체 통계량 계산
    total_questions = answers.count()
    total_correct = answers.filter(state=Answer.State.CORRECT).count()
    overall_accuracy = (total_correct / total_questions * 100) if total_questions > 0 else 0

    print(f"전체 문제 수: {total_questions}")
    print(f"맞춘 문제 수: {total_correct}")
    print(f"전체 정답률: {overall_accuracy:.1f}%")

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

        print(f"\n성취기준 {achievement_code}:")
        print(f"  문제 수: {stats['total_questions']}")
        print(f"  맞춘 문제 수: {stats['correct_questions']}")
        print(f"  정답률: {stats['accuracy']}%")
        print(f"  내용: {stats['content']}")

    return {
        "total_questions": total_questions,
        "total_correct": total_correct,
        "overall_accuracy": round(overall_accuracy, 1),
        "achievement_statistics": achievement_statistics,
    }


def find_best_achievement_code(question_content, achievement_standards):
    """
    GPT API를 사용하여 질문 내용에 가장 적합한 성취기준을 찾는 함수

    Args:
        question_content: 질문 내용
        achievement_standards: 성취기준 리스트

    Returns:
        가장 적합한 성취기준의 code 또는 None
    """

    # OpenAI API 키가 설정되어 있는지 확인
    api_key = getattr(settings, "OPENAI_API_KEY", None)
    if not api_key:
        print("Warning: OPENAI_API_KEY not found in settings. Skipping GPT analysis.")
        return None

    # 성취기준들을 문자열로 변환
    standards_text = "\n".join(
        [f"Code: {std['code']}\nContent: {std['content']}\nGrade: {std['grade']}\n" for std in achievement_standards]
    )

    # GPT API 요청 구성
    headers = {"Authorization": f"Bearer {api_key}", "Content-Type": "application/json"}

    prompt = f"""
The following are educational achievement standards:

{standards_text}

Please determine which of the above standards is the most appropriate for the following question:

Question: {question_content}

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
        response = requests.post(
            "https://api.openai.com/v1/chat/completions", headers=headers, data=json.dumps(data), timeout=30
        )

        if response.status_code == 200:
            result = response.json()

            # TODO: 여기서 단원명도 추출하도록 구현 예정
            achievement_code = result["choices"][0]["message"]["content"].strip()

            # 응답이 유효한 코드인지 확인
            valid_codes = [std["code"] for std in achievement_standards]
            if achievement_code in valid_codes:
                return achievement_code
            else:
                print(f"Warning: GPT returned invalid code '{achievement_code}'. Valid codes: {valid_codes[:5]}...")
                return None
        else:
            print(f"GPT API request failed with status code: {response.status_code}")
            print(f"Response: {response.text}")
            return None

    except requests.exceptions.RequestException as e:
        print(f"GPT API request failed: {str(e)}")
        return None
    except Exception as e:
        print(f"Error processing GPT response: {str(e)}")
        return None


if __name__ == "__main__":
    result = parse_curriculum(student_id=2, class_id=1)
    print("\n=== 최종 결과 ===")
    print(f"반환된 통계량: {result}")
