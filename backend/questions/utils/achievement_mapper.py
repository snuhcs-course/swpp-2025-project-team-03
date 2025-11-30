import csv
import json
import logging
import os

import requests
from django.conf import settings

logger = logging.getLogger(__name__)


def load_achievement_standards():
    """
    CSV 파일에서 성취기준을 로드하는 함수

    Returns:
        list: 성취기준 딕셔너리 리스트
    """
    current_dir = os.path.dirname(os.path.abspath(__file__))
    # achievement_standards.csv는 reports/utils에 있으므로 상대 경로로 접근
    csv_file_path = os.path.join(current_dir, "..", "..", "reports", "utils", "achievement_standards.csv")
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
    except FileNotFoundError:
        logger.warning(f"Achievement standards CSV file not found at {csv_file_path}")
        return []

    return achievement_standards


def get_school_level_from_grade(grade):
    """
    학년 문자열에서 학교 단계를 추출하는 함수

    Args:
        grade: 학년 문자열 (예: "초등학교 1학년", "중학교 2학년")

    Returns:
        str: 학교 단계 ("초등학교", "중학교", "고등학교") 또는 None
    """
    if not grade:
        return None

    if "초" in grade:
        return "초등학교"
    elif "중" in grade:
        return "중학교"
    elif "고" in grade:
        return "고등학교"

    return None


def filter_relevant_standards(achievement_standards, subject_name, school_level):
    """
    과목명과 학교 단계에 맞는 성취기준을 필터링하는 함수

    Args:
        achievement_standards: 전체 성취기준 리스트
        subject_name: 과목명
        school_level: 학교 단계 ("초등학교", "중학교", "고등학교")

    Returns:
        list: 필터링된 성취기준 리스트
    """
    if not school_level:
        return []

    relevant_standards = [
        std for std in achievement_standards if std.get("subject") == subject_name and std.get("school") == school_level
    ]

    return relevant_standards


def filter_standards_by_roberta(summary_text, relevant_standards, top_k=20):
    """
    RoBERTa 모델을 사용하여 성취기준을 top-k개로 필터링하는 함수

    Args:
        summary_text: PDF 요약 텍스트
        relevant_standards: 과목/학교로 필터링된 성취기준 리스트
        top_k: 상위 몇 개를 선택할지 (기본값: 20)

    Returns:
        list: 필터링된 성취기준 리스트
    """
    try:
        from reports.utils.achievement_inference import filter_standards_by_model

        filtered_standards = filter_standards_by_model(
            question_content=summary_text,
            achievement_standards=relevant_standards,
            top_k=top_k,
        )

        logger.info(f"RoBERTa filtered {len(relevant_standards)} → {len(filtered_standards)} standards")

        return filtered_standards
    except Exception as e:
        logger.warning(f"RoBERTa model filtering failed, using all standards: {str(e)}")
        return relevant_standards


def infer_relevant_achievement_codes_from_summary(summary_text, subject_name, grade, max_codes=5, top_k=20):
    """
    PDF summary를 기반으로 관련 성취기준 코드들을 한 번에 추론하는 함수

    RoBERTa 모델로 top-k 성취기준을 필터링한 후 GPT API로 최종 선택합니다.

    Args:
        summary_text: PDF 요약 텍스트
        subject_name: 과목명
        grade: 학년 문자열
        max_codes: 추론할 최대 성취기준 코드 개수 (기본값: 5)
        top_k: RoBERTa 모델 필터링 시 상위 몇 개를 선택할지 (기본값: 20)

    Returns:
        dict: 성취기준 정보
        {
            "codes": ["2과03-01", "2과03-02"],
            "details": {
                "2과03-01": "성취기준 내용 1",
                "2과03-02": "성취기준 내용 2"
            }
        }
    """
    # OpenAI API 키가 설정되어 있는지 확인
    api_key = getattr(settings, "OPENAI_API_KEY", None)
    if not api_key:
        logger.warning("OPENAI_API_KEY not found in settings. Skipping GPT analysis.")
        return {"codes": [], "details": {}}

    # 성취기준 CSV 파일 로드
    achievement_standards = load_achievement_standards()
    if not achievement_standards:
        return {"codes": [], "details": {}}

    # 학교 단계 추출
    school_level = get_school_level_from_grade(grade)
    if not school_level:
        logger.warning(f"Could not determine school level from grade '{grade}'")
        return {"codes": [], "details": {}}

    # 관련 성취기준 필터링 (과목/학교 기준)
    relevant_standards = filter_relevant_standards(achievement_standards, subject_name, school_level)
    if not relevant_standards:
        logger.warning(f"No achievement standards found for subject '{subject_name}' and school '{school_level}'")
        return {"codes": [], "details": {}}

    logger.info(f"Found {len(relevant_standards)} standards for {subject_name} ({school_level})")

    # Step 1: RoBERTa 모델로 top-k 성취기준 필터링
    filtered_standards = filter_standards_by_roberta(summary_text, relevant_standards, top_k=top_k)

    # Step 2: GPT를 사용하여 최종 선택
    # 성취기준들을 문자열로 변환
    standards_text = "\n".join(
        [f"Code: {std['code']}\nContent: {std['content']}\nGrade: {std['grade']}\n" for std in filtered_standards]
    )

    # GPT API 요청 구성
    headers = {"Authorization": f"Bearer {api_key}", "Content-Type": "application/json"}

    prompt = f"""
Below is a summary of the learning material:

{summary_text}

Please select up to {max_codes} achievement standards that are most relevant to the content of the learning material.

Here is the list of achievement standards for the subject ({subject_name}) and school level ({school_level}):

{standards_text}

#Rules
- From the list above, select and return only the achievement standard codes that are most directly relevant to the learning material, up to {max_codes} codes, in a JSON array format.
- Select only the codes that clearly align with the key concepts in the summary.
- Return only the codes with no additional explanation.
- If there are only 1~2 clearly relevant codes, return only those 1~2 codes.
- If none of the achievement standards clearly match the summary, return an empty array [].
- Do NOT include codes that are only vaguely or indirectly related.
Example: ["2과03-01", "2과03-02", "2과04-01"]
"""

    data = {
        "model": "gpt-4o",
        "messages": [
            {
                "role": "system",
                "content": "You are an expert in analyzing curriculum achievement standards. You can accurately determine which achievement standards are relevant to a given learning material.",
            },
            {"role": "user", "content": prompt},
        ],
        "temperature": 0.1,
    }

    try:
        response = requests.post(
            "https://api.openai.com/v1/chat/completions", headers=headers, data=json.dumps(data), timeout=30
        )

        if response.status_code == 200:
            result = response.json()
            response_text = result["choices"][0]["message"]["content"].strip()

            # JSON 파싱 시도
            try:
                # JSON 마크다운 코드 블록 제거
                cleaned_response = response_text.replace("```json", "").replace("```", "").strip()
                achievement_codes = json.loads(cleaned_response)

                # 리스트가 아닌 경우 리스트로 변환
                if not isinstance(achievement_codes, list):
                    achievement_codes = [achievement_codes]

                # 유효한 코드만 필터링 (filtered_standards 기준)
                valid_codes = [std["code"] for std in filtered_standards]
                filtered_codes = [code for code in achievement_codes if code in valid_codes]

                if filtered_codes:
                    logger.info(f"Successfully inferred {len(filtered_codes)} achievement codes: {filtered_codes}")
                    # 코드와 내용을 매핑
                    code_to_content = {std["code"]: std.get("content", "내용 없음") for std in filtered_standards}
                    details = {code: code_to_content[code] for code in filtered_codes[:max_codes]}
                    return {"codes": filtered_codes[:max_codes], "details": details}
                else:
                    logger.warning(f"No valid codes found in GPT response: {response_text}")
                    return {"codes": [], "details": {}}
            except json.JSONDecodeError:
                # JSON 파싱 실패 시 단일 코드로 시도
                achievement_code = response_text.strip().strip('"').strip("'")
                valid_codes = [std["code"] for std in filtered_standards]
                if achievement_code in valid_codes:
                    code_to_content = {std["code"]: std.get("content", "내용 없음") for std in filtered_standards}
                    return {
                        "codes": [achievement_code],
                        "details": {achievement_code: code_to_content[achievement_code]},
                    }
                else:
                    logger.warning(f"GPT returned invalid format: {response_text}")
                    return {"codes": [], "details": {}}
        else:
            logger.error(f"GPT API request failed with status code: {response.status_code}")
            logger.error(f"Response: {response.text}")
            return {"codes": [], "details": {}}

    except requests.exceptions.RequestException as e:
        logger.error(f"GPT API request failed: {str(e)}")
        return {"codes": [], "details": {}}
    except Exception as e:
        logger.error(f"Error processing GPT response: {str(e)}")
        return {"codes": [], "details": {}}
