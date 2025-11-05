import json
import re
import sys
import time
from typing import List

from django.conf import settings
from langchain_core.prompts import ChatPromptTemplate
from langchain_openai import ChatOpenAI
from pydantic import BaseModel, Field


# === Quiz Schema ===
class Quiz(BaseModel):
    question: str = Field(description="question in korean")
    model_answer: str = Field(description="answer in korean")
    explanation: str = Field(description="explanation of the key concept and educational purpose of the question")
    difficulty: str = Field(description="Difficulty level — choose one from: ['easy', 'medium', 'hard']")


# --- Few-shot Examples ---
FEW_SHOT_EXAMPLES = [
    {
        "question": "힘이 일정할 때, 질량이 커지면 가속도는 어떻게 변할까요?",
        "model_answer": "F = ma에 따라, 힘이 일정하면 질량이 커질수록 가속도는 작아집니다.",
        "explanation": "가속도는 힘에 비례하고 질량에 반비례하므로, 무거운 물체는 같은 힘에서 덜 움직입니다.",
        "difficulty": "easy",
    },
    {
        "question": "달에서 물체의 무게가 지구보다 작게 느껴지는 이유는 무엇일까요?",
        "model_answer": "달의 질량이 지구보다 작아서, 중력도 지구보다 약하기 때문입니다.",
        "explanation": "달의 질량이 작아 중력이 약하므로, 같은 물체라도 달에서는 더 가볍게 느껴집니다.",
        "difficulty": "medium",
    },
    {
        "question": "풍선을 냉동실에 넣으면 부피가 줄어드는 이유는 무엇인가요?",
        "model_answer": "온도가 낮아지면 기체 분자의 운동 에너지가 줄어들어 압력이 낮아지고 부피가 줄어듭니다.",
        "explanation": "보일-샤를 법칙에 따라 온도와 부피는 비례 관계에 있습니다.",
        "difficulty": "easy",
    },
    {
        "question": "만약 식물의 잎이 모두 잘려 나간다면, 증산 작용과 물의 이동에는 어떤 변화가 생길까요?",
        "model_answer": "잎이 없으면 기공이 사라져 증산이 거의 일어나지 않으며, 물기둥이 유지되지 못하고 끊어져 물의 이동이 원할하게 이뤄지지 못하게 됩니다.",
        "explanation": "증산 작용은 잎의 기공을 통해 물을 증발시키는 과정이므로 잎이 사라지면 증산 작용이 거의 일어나지 않으며, 증산 작용은 뿌리의 물 흡수를 유도하는 물기둥 유지력을 제공하기 때문에, 증산이 줄면 물의 이동이 거의 멈춥니다.",
        "difficulty": "hard",
    },
]


# --- Prompt Template ---
multi_quiz_prompt = ChatPromptTemplate.from_template(
    """
You are an expert educational quiz designer who creates high-quality review questions based on **learning materials**.

Your task is to generate **{n}** well-crafted quiz questions in Korean that align with the provided learning material.  
Each quiz must:
- Focus on a **different topic or concept** within the material.
- Have **clear educational intent**, assessing understanding or reasoning, not mere recall.
- Avoid questions that simply ask for the "importance" or "role" of a concept (e.g., "~가 중요한 이유는?", "~의 역할은?").
- Be written naturally and distinctly — **no repeated or similar phrasing**.
- Be suitable for elementary ~ middle school korean students or similar educational level.
- Do not simply ask "~가 왜 중요한가요?"!!
- Never ask overly trivial questions and always consider educational purpose for every question.
- Do NOT use any backslash-based notation.
- Write math in plain text only, e.g., "x > 4", "y = 2x + 1".

---

# Examples of Good Questions
Good questions encourage reasoning and conceptual understanding rather than memorization.

- 힘이 일정할 때, 질량이 커지면 가속도는 어떻게 변할까요?  
  → Tests understanding of Newton’s Second Law.

- 왜 같은 물체라도 달에서의 무게가 지구보다 작게 측정될까요?  
  → Invites reasoning about gravitational strength differences.

- 잎의 기공이 너무 많이 열리면 식물은 어떤 어려움을 겪을까요?  
  → Evaluates understanding of transpiration and water balance in plants.

---

# Examples of Poor Questions
Avoid vague, factual, or overly general questions.

- 세포 호흡을 통해 생성된 에너지는 어떻게 사용되나요? 
- 호흡과 배설은 어떤 관계가 있나요?
- 온도계는 무엇을 재는 도구인가요? 
- 왜 덧셈이 중요한가요?

These are too abstract or lack a clear conceptual target.

---

## More examples
{examples}

### Output Format (JSON)
Return your output **only** as a valid JSON array with the following structure:

[
  {{
    "question": "...",
    "model_answer": "...",
    "explanation": "...",
    "difficulty": "..."
  }},
  {{
    "question": "...",
    "model_answer": "...",
    "explanation": "...",
    "difficulty": "..."
  }},
  ...
]

# Learning Materials:
{learning_material}
"""
)


def generate_base_quizzes(material_text: str, n: int = 3) -> List[Quiz]:
    """Generate N quizzes from summarized text."""

    # Python 인터프리터가 종료 중인지 확인
    if getattr(sys, "_exiting", False):
        raise RuntimeError("Cannot generate quizzes: Python interpreter is shutting down")

    # 재시도 로직
    max_retries = 3
    retry_delay = 1.0  # 초

    for attempt in range(max_retries):
        try:
            llm = ChatOpenAI(
                model="gpt-4o-mini",
                temperature=0.5,
                api_key=settings.OPENAI_API_KEY,
                timeout=60.0,  # 60초 타임아웃 설정
            )
            few_shot_str = json.dumps(FEW_SHOT_EXAMPLES, ensure_ascii=False, indent=2)
            prompt = multi_quiz_prompt.format(n=n, examples=few_shot_str, learning_material=material_text)

            # 동기적으로 처리
            response = llm.invoke(prompt).content

            try:
                # JSON 파싱 전에 LaTeX 백슬래시 이스케이프 문제 해결
                # \( 와 \) 를 ( 와 ) 로 변환하여 유효한 JSON으로 만듦
                cleaned_response = re.sub(r"\\([()])", r"\1", response)

                # JSON 마크다운 코드 블록 제거 (```json ... ```)
                cleaned_response = re.sub(r"```json\s*", "", cleaned_response, flags=re.IGNORECASE)
                cleaned_response = re.sub(r"```\s*$", "", cleaned_response, flags=re.MULTILINE)
                cleaned_response = cleaned_response.strip()

                # 직접 JSON 파싱
                parsed = json.loads(cleaned_response)

                # 배열 형식이 우선, 하지만 숫자 키 객체도 지원 (하위 호환성)
                if isinstance(parsed, list):
                    # 배열인 경우 (표준 형식)
                    result = [Quiz(**item) for item in parsed]
                elif isinstance(parsed, dict):
                    # 숫자 키 객체인 경우 (하위 호환성)
                    items = []
                    for key in sorted(parsed.keys(), key=lambda k: int(k) if k.isdigit() else 999):
                        items.append(parsed[key])
                    result = [Quiz(**item) for item in items]
                else:
                    raise ValueError(f"Unexpected JSON structure: {type(parsed)}")

                return result
            except json.JSONDecodeError as e:
                # JSON 파싱 오류 시 실제 응답을 로깅
                import logging

                logger = logging.getLogger(__name__)
                logger.error(f"JSON 파싱 실패. 원본 응답:\n{response}")
                logger.error(f"정리된 응답:\n{cleaned_response}")
                raise ValueError(f"Invalid json output: {cleaned_response[:500]}") from e
            except Exception as e:
                # 다른 오류도 로깅
                import logging

                logger = logging.getLogger(__name__)
                logger.error(f"문제 생성 중 오류: {e}")
                logger.error(f"원본 응답:\n{response}")
                raise

        except RuntimeError as e:
            error_msg = str(e).lower()
            if "interpreter shutdown" in error_msg or "cannot schedule" in error_msg:
                if attempt < max_retries - 1:
                    time.sleep(retry_delay)
                    retry_delay *= 2  # 지수 백오프
                    continue
                else:
                    raise RuntimeError(f"Quiz generation interrupted after {max_retries} attempts: {e}")
            raise
        except Exception as e:
            error_msg = str(e).lower()
            # 일시적인 네트워크 오류인 경우 재시도
            if (
                "timeout" in error_msg or "connection" in error_msg or "network" in error_msg
            ) and attempt < max_retries - 1:
                time.sleep(retry_delay)
                retry_delay *= 2
                continue
            else:
                raise

    # 이 지점에 도달하면 안 됨
    raise RuntimeError("Failed to generate quizzes after all retries")
