import json
from typing import List

from django.conf import settings
from langchain_core.output_parsers.json import JsonOutputParser
from langchain_core.prompts import ChatPromptTemplate
from langchain_openai import ChatOpenAI
from pydantic import BaseModel, Field


# === Quiz Schema ===
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


def generate_base_quizzes(material_text: str, n: int = 3) -> List[Quiz]:
    """Generate N quizzes from summarized text."""
    llm = ChatOpenAI(model="gpt-5", temperature=0.7, api_key=settings.OPENAI_API_KEY)
    few_shot_str = json.dumps(FEW_SHOT_EXAMPLES, ensure_ascii=False, indent=2)
    prompt = multi_quiz_prompt.format(n=n, examples=few_shot_str, learning_material=material_text)

    response = llm.invoke(prompt).content
    parser = JsonOutputParser()

    try:
        parsed = parser.parse(response)
        return [Quiz(**item) for item in parsed.values()]
    except Exception as e:
        print("JSON parsing failed:", e)
        print("Raw model output:\n", response)
        raise
