import csv
import json
import logging
import os
import sys
import time
from pathlib import Path
from typing import Dict, List, Tuple

import requests

# Django 설정이 필요한 경우를 위해
try:
    import django
    from django.conf import settings

    if not settings.configured:
        # Django 설정 시도
        sys.path.insert(0, str(Path(__file__).resolve().parent.parent.parent))
        os.environ.setdefault("DJANGO_SETTINGS_MODULE", "backend.settings")
        django.setup()
except Exception as e:
    print(f"Django setup skipped: {e}")
    settings = None

# tiktoken은 선택적 (OpenAI 토큰 계산용)
try:
    import tiktoken

    TIKTOKEN_AVAILABLE = True
except ImportError:
    TIKTOKEN_AVAILABLE = False
    print("Warning: tiktoken not installed. Token counts will be estimated.")

import sys
from pathlib import Path

# 경로 설정: research 디렉토리에서 backend 모듈 import 가능하도록
sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from backend.reports.utils.achievement_inference import filter_standards_by_model

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def count_tokens(text: str, model: str = "gpt-4o") -> int:
    """OpenAI 토큰 수 계산"""
    if TIKTOKEN_AVAILABLE:
        try:
            encoding = tiktoken.encoding_for_model(model)
            return len(encoding.encode(text))
        except Exception:
            # fallback to cl100k_base
            encoding = tiktoken.get_encoding("cl100k_base")
            return len(encoding.encode(text))
    else:
        # 대략적인 추정: 한글 기준 약 2글자당 1토큰
        return len(text) // 2


def load_achievement_standards() -> List[Dict]:
    """CSV에서 성취기준 로드"""
    # CSV 파일은 backend/reports/utils/에 있음
    current_dir = Path(__file__).resolve().parent.parent / "backend" / "reports" / "utils"
    csv_file_path = current_dir / "achievement_standards.csv"

    achievement_standards = []

    encodings = ["cp949", "utf-8", "euc-kr"]
    for encoding in encodings:
        try:
            with open(csv_file_path, "r", encoding=encoding) as file:
                reader = csv.DictReader(file)
                for row in reader:
                    achievement_standards.append(row)
            break
        except UnicodeDecodeError:
            continue

    return achievement_standards


def build_prompt(
    question_content: str, answer: str, explanation: str, achievement_standards: List[Dict]
) -> Tuple[str, str]:
    """
    GPT 프롬프트 구성

    Returns:
        (system_prompt, user_prompt)
    """
    # 성취기준들을 문자열로 변환
    standards_text = "\n".join(
        [
            f"Code: {std['code']}\nContent: {std['content']}\nGrade: {std.get('grade', 'N/A')}\n"
            for std in achievement_standards
        ]
    )

    # 문제 정보를 문자열로 구성
    question_info = f"Question: {question_content}"
    if answer:
        question_info += f"\nAnswer: {answer}"
    if explanation:
        question_info += f"\nExplanation: {explanation}"

    system_prompt = "당신은 교육과정 성취기준 분석 전문가입니다. 주어진 질문이 어떤 성취기준에 가장 적합한지 정확히 판단할 수 있습니다."

    user_prompt = f"""
The following are educational achievement standards:

{standards_text}

Please determine which of the above standards is the most appropriate for the following question, answer, and explanation:

{question_info}

Only return the Code of the most appropriate standard. No additional explanation is needed.
Do not return anything other than the Code. For example, just return '2과03-01' if that is the best match.

"""

    return system_prompt, user_prompt


def call_gpt_api(system_prompt: str, user_prompt: str, api_key: str) -> Tuple[str, float]:
    """
    GPT API 호출 및 응답 시간 측정

    Returns:
        (response_text, response_time_seconds)
    """
    headers = {"Authorization": f"Bearer {api_key}", "Content-Type": "application/json"}

    data = {
        "model": "gpt-4o",
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_prompt},
        ],
        "max_tokens": 50,
        "temperature": 0.1,
    }

    start_time = time.time()
    response = requests.post(
        "https://api.openai.com/v1/chat/completions", headers=headers, data=json.dumps(data), timeout=60
    )
    elapsed_time = time.time() - start_time

    if response.status_code == 200:
        result = response.json()
        response_text = result["choices"][0]["message"]["content"].strip()
        return response_text, elapsed_time
    else:
        raise Exception(f"API call failed: {response.status_code} - {response.text}")


def analyze_prompt_size(system_prompt: str, user_prompt: str, label: str) -> Dict:
    """프롬프트 크기 분석"""
    full_prompt = system_prompt + user_prompt

    system_tokens = count_tokens(system_prompt)
    user_tokens = count_tokens(user_prompt)
    total_tokens = count_tokens(full_prompt)

    result = {
        "label": label,
        "system_prompt_chars": len(system_prompt),
        "user_prompt_chars": len(user_prompt),
        "total_chars": len(full_prompt),
        "system_tokens": system_tokens,
        "user_tokens": user_tokens,
        "total_tokens": total_tokens,
        "standards_count": user_prompt.count("Code:"),
    }

    return result


def run_comparison_test(
    question_content: str = None,
    answer: str = None,
    explanation: str = None,
    subject: str = "수학",
    school_level: str = "중학교",
    top_k_values: List[int] = None,
    call_api: bool = False,
    api_key: str = None,
) -> Dict:
    """
    필터링 전/후 프롬프트 크기 및 API 응답 시간 비교 테스트

    Args:
        question_content: 테스트할 질문 내용 (없으면 샘플 사용)
        answer: 모범 답안
        explanation: 해설
        subject: 과목명 (기본값: 수학)
        school_level: 학교 단계 (기본값: 중학교)
        top_k_values: 테스트할 top_k 값들 (기본값: [5, 10, 20, 30, 50])
        call_api: 실제 API 호출 여부 (기본값: False - 프롬프트 크기만 비교)
        api_key: OpenAI API 키 (call_api=True일 때 필요)

    Returns:
        비교 결과 딕셔너리
    """
    if top_k_values is None:
        top_k_values = [5, 10, 20, 30, 50]

    # 샘플 질문 (없으면 기본값 사용)
    if question_content is None:
        question_content = "일차방정식 3x + 5 = 14를 풀이하시오."
        answer = "x = 3"
        explanation = "3x + 5 = 14에서 양변에서 5를 빼면 3x = 9가 되고, 양변을 3으로 나누면 x = 3입니다."

    # 성취기준 로드
    all_standards = load_achievement_standards()
    print(f"\n📚 전체 성취기준 수: {len(all_standards)}")

    # 과목과 학교 단계로 필터링
    relevant_standards = [
        std for std in all_standards if std.get("subject") == subject and std.get("school") == school_level
    ]
    print(f"📖 {subject}/{school_level} 관련 성취기준: {len(relevant_standards)}개")

    if not relevant_standards:
        print(f"⚠️ {subject}/{school_level}에 해당하는 성취기준이 없습니다.")
        print("   사용 가능한 과목/학교 조합:")
        combinations = set((std.get("subject"), std.get("school")) for std in all_standards)
        for subj, sch in sorted(combinations):
            print(f"   - {subj} / {sch}")
        return {}

    results = {
        "question": question_content,
        "answer": answer,
        "explanation": explanation,
        "subject": subject,
        "school_level": school_level,
        "total_relevant_standards": len(relevant_standards),
        "comparisons": [],
    }

    # API 키 확인
    if call_api:
        if api_key is None:
            api_key = getattr(settings, "OPENAI_API_KEY", None) if settings else None
            if api_key is None:
                api_key = os.environ.get("OPENAI_API_KEY")
        if not api_key:
            print("⚠️ API 키가 없어 프롬프트 크기만 비교합니다.")
            call_api = False

    print("\n" + "=" * 80)
    print("📊 프롬프트 크기 비교 테스트")
    print("=" * 80)
    print(f"\n📝 테스트 질문: {question_content[:50]}...")

    # 1. 필터링 없이 전체 성취기준 사용
    print("\n" + "-" * 60)
    print("🔷 [비교 기준] 필터링 없음 (전체 성취기준 사용)")
    print("-" * 60)

    system_prompt, user_prompt = build_prompt(question_content, answer, explanation, relevant_standards)
    baseline_analysis = analyze_prompt_size(system_prompt, user_prompt, "No filtering (baseline)")

    print(f"   성취기준 수: {baseline_analysis['standards_count']}개")
    print(f"   프롬프트 문자 수: {baseline_analysis['total_chars']:,}")
    print(f"   프롬프트 토큰 수: {baseline_analysis['total_tokens']:,}")

    baseline_api_result = None
    baseline_api_time = None

    if call_api:
        try:
            response, elapsed = call_gpt_api(system_prompt, user_prompt, api_key)
            baseline_api_result = response
            baseline_api_time = elapsed
            print(f"   API 응답 시간: {elapsed:.3f}초")
            print(f"   API 응답: {response}")
        except Exception as e:
            print(f"   ❌ API 호출 실패: {e}")

    baseline_analysis["api_response"] = baseline_api_result
    baseline_analysis["api_time"] = baseline_api_time
    results["baseline"] = baseline_analysis

    # 2. 각 top_k 값으로 필터링
    for top_k in top_k_values:
        print("\n" + "-" * 60)
        print(f"🔶 [top_k={top_k}] RoBERTa 모델로 필터링")
        print("-" * 60)

        try:
            # 모델 추론 시간 측정
            model_start = time.time()
            filtered_standards = filter_standards_by_model(
                question_content=question_content,
                achievement_standards=relevant_standards,
                top_k=top_k,
            )
            model_time = time.time() - model_start

            print(f"   RoBERTa 모델 추론 시간: {model_time:.3f}초")
            print(f"   필터링된 성취기준 수: {len(filtered_standards)}개")

            # 프롬프트 생성
            system_prompt, user_prompt = build_prompt(question_content, answer, explanation, filtered_standards)
            analysis = analyze_prompt_size(system_prompt, user_prompt, f"top_k={top_k}")
            analysis["model_inference_time"] = model_time

            print(f"   프롬프트 문자 수: {analysis['total_chars']:,}")
            print(f"   프롬프트 토큰 수: {analysis['total_tokens']:,}")

            # 감소율 계산
            char_reduction = (1 - analysis["total_chars"] / baseline_analysis["total_chars"]) * 100
            token_reduction = (1 - analysis["total_tokens"] / baseline_analysis["total_tokens"]) * 100

            print(f"   📉 문자 수 감소율: {char_reduction:.1f}%")
            print(f"   📉 토큰 수 감소율: {token_reduction:.1f}%")

            analysis["char_reduction_percent"] = round(char_reduction, 1)
            analysis["token_reduction_percent"] = round(token_reduction, 1)

            # API 호출 (옵션)
            if call_api:
                try:
                    response, elapsed = call_gpt_api(system_prompt, user_prompt, api_key)
                    analysis["api_response"] = response
                    analysis["api_time"] = elapsed

                    if baseline_api_time:
                        time_reduction = (1 - elapsed / baseline_api_time) * 100
                        print(f"   API 응답 시간: {elapsed:.3f}초 (감소율: {time_reduction:.1f}%)")
                        analysis["api_time_reduction_percent"] = round(time_reduction, 1)
                    else:
                        print(f"   API 응답 시간: {elapsed:.3f}초")

                    print(f"   API 응답: {response}")
                except Exception as e:
                    print(f"   ❌ API 호출 실패: {e}")
                    analysis["api_response"] = None
                    analysis["api_time"] = None

            results["comparisons"].append(analysis)

        except Exception as e:
            print(f"   ❌ 필터링 실패: {e}")
            import traceback

            traceback.print_exc()

    # 요약 출력
    print("\n" + "=" * 80)
    print("📈 요약 비교표")
    print("=" * 80)

    print(f"\n{'설정':<20} | {'성취기준 수':>12} | {'토큰 수':>10} | {'감소율':>8} | {'API 시간':>10}")
    print("-" * 80)

    print(
        f"{'필터링 없음':<20} | {baseline_analysis['standards_count']:>12}개 | "
        f"{baseline_analysis['total_tokens']:>10,} | {'(기준)':>8} | "
        f"{baseline_analysis.get('api_time', 'N/A'):>10}"
    )

    for comp in results["comparisons"]:
        api_time_str = f"{comp['api_time']:.3f}s" if comp.get("api_time") else "N/A"
        print(
            f"{comp['label']:<20} | {comp['standards_count']:>12}개 | "
            f"{comp['total_tokens']:>10,} | {comp.get('token_reduction_percent', 0):>7.1f}% | "
            f"{api_time_str:>10}"
        )

    print("\n" + "=" * 80)

    return results


def quick_test():
    """빠른 테스트 (API 호출 없이 프롬프트 크기만 비교)"""
    return run_comparison_test(call_api=False)


def full_test(api_key: str = None):
    """전체 테스트 (API 호출 포함)"""
    return run_comparison_test(call_api=True, api_key=api_key)


# 과목별 샘플 질문 (각 과목에 적합한 질문)
SAMPLE_QUESTIONS = {
    ("수학", "초등학교"): {
        "question": "두 자리 수 덧셈 23 + 45를 계산하시오.",
        "answer": "68",
        "explanation": "23과 45를 더하면 68이 됩니다. 일의 자리 3+5=8, 십의 자리 2+4=6으로 68입니다.",
    },
    ("수학", "중학교"): {
        "question": "일차방정식 3x + 5 = 14를 풀이하시오.",
        "answer": "x = 3",
        "explanation": "3x + 5 = 14에서 양변에서 5를 빼면 3x = 9가 되고, 양변을 3으로 나누면 x = 3입니다.",
    },
    ("수학", "고등학교"): {
        "question": "이차방정식 x² - 5x + 6 = 0의 해를 구하시오.",
        "answer": "x = 2 또는 x = 3",
        "explanation": "x² - 5x + 6 = (x-2)(x-3) = 0이므로 x = 2 또는 x = 3입니다.",
    },
    ("과학", "초등학교"): {
        "question": "물의 세 가지 상태를 말하시오.",
        "answer": "고체(얼음), 액체(물), 기체(수증기)",
        "explanation": "물은 온도에 따라 고체인 얼음, 액체인 물, 기체인 수증기로 존재합니다.",
    },
    ("과학", "중학교"): {
        "question": "광합성에서 필요한 물질과 생성되는 물질을 설명하시오.",
        "answer": "물과 이산화탄소가 필요하고 포도당과 산소가 생성됩니다.",
        "explanation": "식물은 빛 에너지를 이용하여 물과 이산화탄소를 포도당과 산소로 바꿉니다.",
    },
    ("과학", "고등학교"): {
        "question": "뉴턴의 운동 제2법칙을 설명하시오.",
        "answer": "F = ma (힘 = 질량 × 가속도)",
        "explanation": "물체에 작용하는 알짜힘은 물체의 질량과 가속도의 곱과 같습니다.",
    },
    ("국어", "초등학교"): {
        "question": "다음 문장에서 주어를 찾으시오: '강아지가 공원에서 뛰어놉니다.'",
        "answer": "강아지가",
        "explanation": "주어는 문장에서 동작이나 상태의 주체가 되는 말로, 이 문장에서는 '강아지가'입니다.",
    },
    ("국어", "중학교"): {
        "question": "은유와 직유의 차이점을 설명하시오.",
        "answer": "직유는 '~처럼, ~같이'를 사용하고, 은유는 직접 빗대어 표현합니다.",
        "explanation": "직유는 '그녀는 꽃처럼 아름답다'처럼 표현하고, 은유는 '그녀는 꽃이다'처럼 표현합니다.",
    },
    ("국어", "고등학교"): {
        "question": "고전 소설의 특징을 세 가지 이상 설명하시오.",
        "answer": "전지적 작가 시점, 행복한 결말, 권선징악, 비현실적 요소 등",
        "explanation": "고전 소설은 대체로 전지적 작가 시점으로 서술되며, 권선징악적 주제와 행복한 결말을 가집니다.",
    },
    ("사회", "초등학교"): {
        "question": "우리 지역의 공공기관을 세 가지 말해보시오.",
        "answer": "주민센터, 소방서, 경찰서",
        "explanation": "공공기관은 주민들을 위해 서비스를 제공하는 곳으로, 주민센터, 소방서, 경찰서 등이 있습니다.",
    },
    ("사회", "중학교"): {
        "question": "민주주의의 기본 원리를 설명하시오.",
        "answer": "국민 주권, 권력 분립, 법치주의, 다수결 원칙",
        "explanation": "민주주의는 국민이 주권을 가지고, 권력을 분립시키며, 법에 따라 통치하는 정치 체제입니다.",
    },
    ("사회", "고등학교"): {
        "question": "시장 경제의 장단점을 설명하시오.",
        "answer": "장점: 효율성, 혁신 촉진 / 단점: 불평등, 시장 실패 가능성",
        "explanation": "시장 경제는 자원 배분의 효율성을 높이지만, 소득 불평등과 외부 효과 등의 문제가 발생할 수 있습니다.",
    },
    ("영어", "초등학교"): {
        "question": "다음 단어의 뜻을 쓰시오: apple, book, cat",
        "answer": "apple: 사과, book: 책, cat: 고양이",
        "explanation": "기본적인 영어 단어의 의미를 알고 있어야 합니다.",
    },
    ("영어", "중학교"): {
        "question": "현재완료 시제의 용법을 설명하시오.",
        "answer": "완료, 경험, 계속, 결과를 나타냅니다.",
        "explanation": "현재완료(have + 과거분사)는 과거에 시작되어 현재까지 영향을 미치는 상황을 표현합니다.",
    },
    ("영어", "고등학교"): {
        "question": "관계대명사 what과 that의 차이를 설명하시오.",
        "answer": "what은 선행사를 포함하고, that은 선행사가 필요합니다.",
        "explanation": "what = the thing which로 선행사를 포함하며, that은 별도의 선행사를 필요로 합니다.",
    },
}


def get_sample_question(subject: str, school_level: str) -> Dict:
    """과목/학교에 맞는 샘플 질문 가져오기"""
    key = (subject, school_level)
    if key in SAMPLE_QUESTIONS:
        return SAMPLE_QUESTIONS[key]

    # 기본 질문
    return {
        "question": f"{subject} 관련 문제입니다.",
        "answer": "정답입니다.",
        "explanation": "이것은 샘플 해설입니다.",
    }


def run_all_combinations_test(
    top_k_values: List[int] = None,
    call_api: bool = False,
    api_key: str = None,
    verbose: bool = False,
) -> Dict:
    """
    모든 과목/학교 조합에 대해 테스트를 실행하고 평균을 계산합니다.

    Args:
        top_k_values: 테스트할 top_k 값들 (기본값: [5, 10, 20, 30, 50])
        call_api: 실제 API 호출 여부 (기본값: False)
        api_key: OpenAI API 키
        verbose: 각 조합별 상세 출력 여부 (기본값: False)

    Returns:
        전체 결과 및 평균 통계 딕셔너리
    """
    if top_k_values is None:
        top_k_values = [5, 10, 20, 30, 50]

    # 성취기준 로드
    all_standards = load_achievement_standards()

    # 모든 과목/학교 조합 찾기
    combinations = set()
    for std in all_standards:
        subject = std.get("subject")
        school = std.get("school")
        if (
            subject
            and subject in ["국어", "수학", "영어", "과학", "사회"]
            and school in ["초등학교", "중학교", "고등학교교"]
        ):
            combinations.add((subject, school))

    combinations = sorted(combinations)

    print("\n" + "=" * 100)
    print("🎯 전체 과목/학교 조합 테스트")
    print("=" * 100)
    print(f"\n📚 전체 성취기준 수: {len(all_standards)}")
    print(f"📂 과목/학교 조합 수: {len(combinations)}개")
    print(f"🔧 테스트할 top_k 값: {top_k_values}")

    if call_api:
        if api_key is None:
            api_key = getattr(settings, "OPENAI_API_KEY", None) if settings else None
            if api_key is None:
                api_key = os.environ.get("OPENAI_API_KEY")
        if not api_key:
            print("⚠️ API 키가 없어 프롬프트 크기만 비교합니다.")
            call_api = False

    # 조합별 결과 수집
    all_results = []

    # top_k별 집계용 딕셔너리
    topk_aggregates = {
        k: {
            "token_reductions": [],
            "char_reductions": [],
            "model_times": [],
            "api_times": [],
            "api_time_reductions": [],
        }
        for k in top_k_values
    }

    # baseline 집계
    baseline_tokens_list = []
    baseline_chars_list = []
    baseline_api_times = []

    print("\n" + "-" * 100)
    print("📋 조합별 테스트 진행 중...")
    print("-" * 100)

    for idx, (subject, school_level) in enumerate(combinations, 1):
        # 해당 조합의 성취기준 필터링
        relevant_standards = [
            std for std in all_standards if std.get("subject") == subject and std.get("school") == school_level
        ]

        if len(relevant_standards) < 3:
            print(
                f"⏭️ [{idx}/{len(combinations)}] {subject}/{school_level}: 성취기준이 {len(relevant_standards)}개로 스킵"
            )
            continue

        # 샘플 질문 가져오기
        sample = get_sample_question(subject, school_level)
        question_content = sample["question"]
        answer = sample["answer"]
        explanation = sample["explanation"]

        print(f"\n🔍 [{idx}/{len(combinations)}] {subject}/{school_level} (성취기준 {len(relevant_standards)}개)")

        if verbose:
            print(f"   질문: {question_content[:40]}...")

        result = {
            "subject": subject,
            "school_level": school_level,
            "total_standards": len(relevant_standards),
            "question": question_content,
            "comparisons": [],
        }

        # Baseline (필터링 없음)
        system_prompt, user_prompt = build_prompt(question_content, answer, explanation, relevant_standards)
        baseline_analysis = analyze_prompt_size(system_prompt, user_prompt, "baseline")

        baseline_tokens_list.append(baseline_analysis["total_tokens"])
        baseline_chars_list.append(baseline_analysis["total_chars"])

        if verbose:
            print(f"   ├─ Baseline: {baseline_analysis['total_tokens']:,} 토큰")

        baseline_api_time = None
        if call_api:
            try:
                _, elapsed = call_gpt_api(system_prompt, user_prompt, api_key)
                baseline_api_time = elapsed
                baseline_api_times.append(elapsed)
                if verbose:
                    print(f"   │   API 시간: {elapsed:.3f}s")
            except Exception as e:
                if verbose:
                    print(f"   │   API 실패: {e}")

        result["baseline"] = baseline_analysis
        result["baseline"]["api_time"] = baseline_api_time

        # 각 top_k에 대해 테스트
        for top_k in top_k_values:
            try:
                # 모델 필터링
                model_start = time.time()
                filtered_standards = filter_standards_by_model(
                    question_content=question_content,
                    achievement_standards=relevant_standards,
                    top_k=top_k,
                )
                model_time = time.time() - model_start

                # 프롬프트 분석
                system_prompt, user_prompt = build_prompt(question_content, answer, explanation, filtered_standards)
                analysis = analyze_prompt_size(system_prompt, user_prompt, f"top_k={top_k}")
                analysis["model_inference_time"] = model_time

                # 감소율 계산
                token_reduction = (1 - analysis["total_tokens"] / baseline_analysis["total_tokens"]) * 100
                char_reduction = (1 - analysis["total_chars"] / baseline_analysis["total_chars"]) * 100

                analysis["token_reduction_percent"] = round(token_reduction, 1)
                analysis["char_reduction_percent"] = round(char_reduction, 1)

                # 집계에 추가
                topk_aggregates[top_k]["token_reductions"].append(token_reduction)
                topk_aggregates[top_k]["char_reductions"].append(char_reduction)
                topk_aggregates[top_k]["model_times"].append(model_time)

                if verbose:
                    print(f"   ├─ top_k={top_k}: {analysis['total_tokens']:,} 토큰 (↓{token_reduction:.1f}%)")

                # API 호출
                if call_api and baseline_api_time:
                    try:
                        _, elapsed = call_gpt_api(system_prompt, user_prompt, api_key)
                        analysis["api_time"] = elapsed
                        api_time_reduction = (1 - elapsed / baseline_api_time) * 100
                        analysis["api_time_reduction_percent"] = round(api_time_reduction, 1)
                        topk_aggregates[top_k]["api_times"].append(elapsed)
                        topk_aggregates[top_k]["api_time_reductions"].append(api_time_reduction)
                        if verbose:
                            print(f"   │   API 시간: {elapsed:.3f}s (↓{api_time_reduction:.1f}%)")
                    except Exception:
                        pass

                result["comparisons"].append(analysis)

            except Exception as e:
                if verbose:
                    print(f"   ├─ top_k={top_k}: 실패 - {e}")

        all_results.append(result)

    # 통계 계산 헬퍼 함수
    def calc_stats(values: List[float]) -> Dict:
        """평균, 최소, 최대 계산"""
        if not values:
            return {"avg": None, "min": None, "max": None}
        return {
            "avg": sum(values) / len(values),
            "min": min(values),
            "max": max(values),
        }

    # 평균/최소/최대 계산 및 출력
    print("\n" + "=" * 120)
    print("📊 전체 통계 (평균 / 최소 / 최대)")
    print("=" * 120)

    n_tested = len(all_results)
    print(f"\n✅ 테스트된 과목/학교 조합: {n_tested}개")

    if baseline_tokens_list:
        baseline_token_stats = calc_stats(baseline_tokens_list)
        baseline_char_stats = calc_stats(baseline_chars_list)
        print("\n📈 Baseline (필터링 없음):")
        print(
            f"   - 토큰 수: 평균 {baseline_token_stats['avg']:,.0f} / "
            f"최소 {baseline_token_stats['min']:,.0f} / 최대 {baseline_token_stats['max']:,.0f}"
        )
        print(
            f"   - 문자 수: 평균 {baseline_char_stats['avg']:,.0f} / "
            f"최소 {baseline_char_stats['min']:,.0f} / 최대 {baseline_char_stats['max']:,.0f}"
        )
        if baseline_api_times:
            baseline_api_stats = calc_stats(baseline_api_times)
            print(
                f"   - API 시간: 평균 {baseline_api_stats['avg']:.3f}s / "
                f"최소 {baseline_api_stats['min']:.3f}s / 최대 {baseline_api_stats['max']:.3f}s"
            )

    print("\n📉 top_k별 토큰 감소율 통계:")
    print("-" * 120)
    print(
        f"{'top_k':>8} | {'평균':>10} | {'최소':>10} | {'최대':>10} | "
        f"{'모델시간(평균)':>14} | {'모델시간(최소)':>14} | {'모델시간(최대)':>14}"
    )
    print("-" * 120)

    summary_stats = {}

    for top_k in top_k_values:
        agg = topk_aggregates[top_k]

        if agg["token_reductions"]:
            token_stats = calc_stats(agg["token_reductions"])
            char_stats = calc_stats(agg["char_reductions"])
            model_stats = calc_stats(agg["model_times"])

            print(
                f"{top_k:>8} | {token_stats['avg']:>9.1f}% | {token_stats['min']:>9.1f}% | {token_stats['max']:>9.1f}% | "
                f"{model_stats['avg']:>13.3f}s | {model_stats['min']:>13.3f}s | {model_stats['max']:>13.3f}s"
            )

            # API 통계
            api_stats = calc_stats(agg["api_times"]) if agg["api_times"] else {"avg": None, "min": None, "max": None}
            api_reduction_stats = (
                calc_stats(agg["api_time_reductions"])
                if agg["api_time_reductions"]
                else {"avg": None, "min": None, "max": None}
            )

            summary_stats[top_k] = {
                "token_reduction": {
                    "avg": round(token_stats["avg"], 1),
                    "min": round(token_stats["min"], 1),
                    "max": round(token_stats["max"], 1),
                },
                "char_reduction": {
                    "avg": round(char_stats["avg"], 1),
                    "min": round(char_stats["min"], 1),
                    "max": round(char_stats["max"], 1),
                },
                "model_time": {
                    "avg": round(model_stats["avg"], 3),
                    "min": round(model_stats["min"], 3),
                    "max": round(model_stats["max"], 3),
                },
                "api_time": api_stats,
                "api_reduction": api_reduction_stats,
                "n_samples": len(agg["token_reductions"]),
            }
        else:
            print(f"{top_k:>8} | {'N/A':>10} | {'N/A':>10} | {'N/A':>10} | {'N/A':>14} | {'N/A':>14} | {'N/A':>14}")

    print("-" * 120)

    # API 시간 통계 (API 호출이 있는 경우)
    has_api_stats = any(agg["api_times"] for agg in topk_aggregates.values())
    if has_api_stats:
        print("\n⏱️ top_k별 API 응답 시간 통계:")
        print("-" * 100)
        print(
            f"{'top_k':>8} | {'API시간(평균)':>14} | {'API시간(최소)':>14} | {'API시간(최대)':>14} | "
            f"{'감소율(평균)':>12} | {'감소율(최소)':>12} | {'감소율(최대)':>12}"
        )
        print("-" * 100)

        for top_k in top_k_values:
            agg = topk_aggregates[top_k]
            if agg["api_times"]:
                api_stats = calc_stats(agg["api_times"])
                reduction_stats = calc_stats(agg["api_time_reductions"])
                print(
                    f"{top_k:>8} | {api_stats['avg']:>13.3f}s | {api_stats['min']:>13.3f}s | {api_stats['max']:>13.3f}s | "
                    f"{reduction_stats['avg']:>11.1f}% | {reduction_stats['min']:>11.1f}% | {reduction_stats['max']:>11.1f}%"
                )
            else:
                print(f"{top_k:>8} | {'N/A':>14} | {'N/A':>14} | {'N/A':>14} | {'N/A':>12} | {'N/A':>12} | {'N/A':>12}")
        print("-" * 100)

    # top_k=20 하이라이트
    if 20 in summary_stats:
        stats_20 = summary_stats[20]
        print("\n🎯 top_k=20 (현재 설정) 상세:")
        print("   📊 토큰 감소율:")
        print(f"      - 평균: {stats_20['token_reduction']['avg']:.1f}%")
        print(f"      - 최소: {stats_20['token_reduction']['min']:.1f}% (가장 적게 감소)")
        print(f"      - 최대: {stats_20['token_reduction']['max']:.1f}% (가장 많이 감소)")
        print("   📊 문자 감소율:")
        print(f"      - 평균: {stats_20['char_reduction']['avg']:.1f}%")
        print(f"      - 최소: {stats_20['char_reduction']['min']:.1f}%")
        print(f"      - 최대: {stats_20['char_reduction']['max']:.1f}%")
        print("   ⏱️ 모델 추론 시간:")
        print(f"      - 평균: {stats_20['model_time']['avg']:.3f}s")
        print(f"      - 최소: {stats_20['model_time']['min']:.3f}s")
        print(f"      - 최대: {stats_20['model_time']['max']:.3f}s")
        if stats_20["api_time"]["avg"]:
            print("   🌐 API 응답 시간:")
            print(f"      - 평균: {stats_20['api_time']['avg']:.3f}s")
            print(f"      - 최소: {stats_20['api_time']['min']:.3f}s")
            print(f"      - 최대: {stats_20['api_time']['max']:.3f}s")
        if stats_20["api_reduction"]["avg"]:
            print("   📉 API 시간 감소율:")
            print(f"      - 평균: {stats_20['api_reduction']['avg']:.1f}%")
            print(f"      - 최소: {stats_20['api_reduction']['min']:.1f}%")
            print(f"      - 최대: {stats_20['api_reduction']['max']:.1f}%")

    print("\n" + "=" * 120)

    return {
        "n_combinations_tested": n_tested,
        "combinations": [(r["subject"], r["school_level"]) for r in all_results],
        "baseline": {
            "tokens": calc_stats(baseline_tokens_list),
            "chars": calc_stats(baseline_chars_list),
            "api_time": calc_stats(baseline_api_times) if baseline_api_times else None,
        },
        "summary_by_topk": summary_stats,
        "detailed_results": all_results,
    }


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description="프롬프트 크기 비교 테스트")
    parser.add_argument("--api", action="store_true", help="실제 API 호출 포함")
    parser.add_argument("--api-key", type=str, help="OpenAI API 키")
    parser.add_argument("--subject", type=str, help="테스트할 과목 (미지정 시 전체 테스트)")
    parser.add_argument("--school", type=str, help="학교 단계 (미지정 시 전체 테스트)")
    parser.add_argument("--question", type=str, help="테스트할 질문")
    parser.add_argument("--top-k", type=str, default="5,10,20,30,50", help="테스트할 top_k 값들 (쉼표로 구분)")
    parser.add_argument("--all", action="store_true", help="모든 과목/학교 조합 테스트")
    parser.add_argument("--verbose", "-v", action="store_true", help="상세 출력")

    args = parser.parse_args()

    top_k_values = [int(x.strip()) for x in args.top_k.split(",")]

    if args.all or (args.subject is None and args.school is None):
        # 전체 조합 테스트
        run_all_combinations_test(
            top_k_values=top_k_values,
            call_api=args.api,
            api_key=args.api_key,
            verbose=args.verbose,
        )
    else:
        # 단일 과목/학교 테스트
        run_comparison_test(
            question_content=args.question,
            subject=args.subject or "수학",
            school_level=args.school or "중학교",
            top_k_values=top_k_values,
            call_api=args.api,
            api_key=args.api_key,
        )
