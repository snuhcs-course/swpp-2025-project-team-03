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
    {
        "topic": "온도와 부피 변화",
        "question": "풍선을 냉동실에 넣으면 부피가 줄어드는 이유는 무엇인가요?",
        "model_answer": "온도가 낮아지면 기체 분자의 운동 에너지가 줄어들어 압력이 낮아지고 부피가 줄어듭니다.",
        "explanation": "보일-샤를 법칙에 따라 온도와 부피는 비례 관계에 있습니다.",
        "difficulty": "easy",
    },
    {
        "topic": "식물의 증산 작용",
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
    llm = ChatOpenAI(model="gpt-4o-mini", temperature=0.5, api_key=settings.OPENAI_API_KEY)
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
