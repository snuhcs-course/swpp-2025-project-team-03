"""
Few-shot Multi-Quiz Generator (balanced version)
"""

import argparse
import json
import os
from typing import List

from dotenv import load_dotenv
from langchain_core.output_parsers.json import JsonOutputParser
from langchain_core.prompts import ChatPromptTemplate
from langchain_openai import ChatOpenAI
from pydantic import BaseModel, Field

# Load API key
load_dotenv()
if "OPENAI_API_KEY" not in os.environ:
    raise ValueError("OPENAI_API_KEY is not set in environment variables.")

# Use lightweight model for speed (adjust as needed)
llm = ChatOpenAI(model="gpt-5", temperature=0.7)


# Quiz schema
class Quiz(BaseModel):
    topic: str = Field(description="related topic")
    question: str = Field(description="question in korean")
    model_answer: str = Field(description="answer in korean")
    explanation: str = Field(description="explanation of the key concept and educational purpose of the question")
    difficulty: str = Field(description="Difficulty level — choose one from: ['easy', 'medium', 'hard']")


# --- Few-shot Examples ---
FEW_SHOT_EXAMPLES = [
    {
        "topic": "뉴턴의 제2운동법칙",
        "question": "힘이 일정할 때, 질량이 커지면 가속도는 어떻게 변할까요?",
        "model_answer": "F = ma에 따라, 힘이 일정하면 질량이 커질수록 가속도는 작아집니다.",
        "explanation": "가속도는 힘에 비례하고 질량에 반비례하므로, 무거운 물체는 같은 힘에서 덜 움직입니다.",
        "difficulty": "easy",
    },
    {
        "topic": "중력의 영향",
        "question": "달에서 물체의 무게가 지구보다 작게 느껴지는 이유는 무엇일까요?",
        "model_answer": "달의 질량이 지구보다 작아서, 중력도 지구보다 약하기 때문입니다.",
        "explanation": "달의 질량이 작아 중력이 약하므로, 같은 물체라도 달에서는 더 가볍게 느껴집니다.",
        "difficulty": "medium",
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
- Be written naturally and distinctly — **no repeated or similar phrasing**.
- Be suitable for elementary ~ middle school korean students or similar educational level.

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
Return your output **only** as valid JSON with the following structure:

{{
  "0": {{
    "topic": "...",
    "question": "...",
    "model_answer": "...",
    "explanation": "...",
    "difficulty": "..."
  }},
  "1": {{
    "topic": "...",
    "question": "...",
    "model_answer": "...",
    "explanation": "...",
    "difficulty": "..."
  }},
...
}}

# Learning Materials:
{learning_material}
"""
)


def generate_quizzes(material: str, n: int) -> List[Quiz]:
    few_shot_str = json.dumps(FEW_SHOT_EXAMPLES, ensure_ascii=False, indent=2)
    prompt = multi_quiz_prompt.format(n=n, examples=few_shot_str, learning_material=material)
    response = llm.invoke(prompt).content
    parser = JsonOutputParser()
    try:
        data = parser.parse(response)
        quizzes = [Quiz(**item) for item in data.values()]
    except Exception as e:
        print("⚠️ JSON parsing failed, showing raw output:\n", response)
        raise e

    return quizzes


def main():
    parser = argparse.ArgumentParser(
        description="Generate N diverse quizzes from learning material (few-shot version)."
    )
    parser.add_argument("--n", type=int, default=3, help="Number of quizzes to generate.")
    parser.add_argument("--summary", type=str, help="Path to learning material text file.")
    args = parser.parse_args()

    if args.summary:
        with open(args.summary, "r", encoding="utf-8") as f:
            material = f.read()
    else:
        material = """
            사과는 왜 땅에 떨어질까요?

            중력은 물체의 질량(mass) 때문에 생기는 자연의 기본적인 힘이에요.
            모든 물체는 질량을 가지고 있고, 질량이 있는 물체끼리는 서로를 끌어당기죠.
            이 현상을 만유인력의 법칙(Law of Universal Gravitation) 이라고 합니다.

            아이작 뉴턴은 중력을 이렇게 설명했어요:
            두 물체 사이에는 서로를 끌어당기는 힘이 작용하며,
            그 힘의 크기는 두 물체의 질량에 비례하고,
            두 물체 사이의 거리의 제곱에 반비례한다.

            수식으로는 다음과 같아요:
            F = G × (m₁ × m₂) / r²
            여기서 F는 두 물체 사이의 중력의 크기 (단위: 뉴턴, N),
            G는 만유인력 상수 (약 6.67 × 10⁻¹¹ N·m²/kg²),
            m₁, m₂는 두 물체의 질량 (킬로그램, kg),
            r은 두 물체 사이의 거리 (미터, m)입니다.

            즉, 질량이 큰 물체일수록 중력이 강하고, 거리가 멀어질수록 중력이 약해집니다.
            지구의 질량이 아주 크기 때문에, 우리를 비롯한 모든 물체가 지구 중심 방향으로 끌려가죠.
            그래서 공을 던지면 결국 땅으로 떨어지고, 우리가 ‘무게’를 느끼는 것도 바로 이 중력 때문이에요.

            예를 들어, 달은 지구보다 질량이 작아서 중력이 약해요.
            그래서 같은 물체라도 달에서는 지구에서보다 약 6분의 1 정도의 무게만 느껴집니다.

            결국, 중력은 질량을 가진 모든 물체가 서로 끌어당기는 힘이며,
            이 힘 덕분에 행성은 태양 주위를 돌고, 달은 지구 주위를 도는 거예요.
        """

    quizzes = generate_quizzes(material, args.n)

    print(f"\n✅ Generated {len(quizzes)} quizzes:\n")
    for i, q in enumerate(quizzes, 1):
        print(f"[{i}] Topic: {q.topic}")
        print(f"Q: {q.question}")
        print(f"A: {q.model_answer}")
        print(f"Explanation: {q.explanation}")
        print(f"Difficulty: {q.difficulty}")
        print("-" * 70)


if __name__ == "__main__":
    main()
