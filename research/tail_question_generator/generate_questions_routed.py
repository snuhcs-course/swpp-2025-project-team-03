"""
Tail-question generator with planner-only correctness grading,
rule-based bucket routing, and few-shot actor prompts. (No ReAct/tools)
CLI: python generate_tail_questions.py --help
"""

import argparse
import json
import os
import time
from typing import Literal, Optional, Tuple, TypedDict

from dotenv import load_dotenv
from langchain_core.output_parsers import JsonOutputParser
from langchain_core.prompts import ChatPromptTemplate
from langchain_openai import ChatOpenAI
from langgraph.graph import END, StateGraph

# Environment & Models
load_dotenv()
if "OPENAI_API_KEY" not in os.environ:
    raise ValueError("OPENAI_API_KEY is not set")

LLM_MODEL = "gpt-4o-mini"
planner_llm = ChatOpenAI(
    model=LLM_MODEL,
    temperature=0,
    model_kwargs={"response_format": {"type": "json_object"}},
)
actor_llm = ChatOpenAI(
    model=LLM_MODEL,
    temperature=0.7,
    model_kwargs={"response_format": {"type": "json_object"}},
)
parser = JsonOutputParser()


# ---- Few-shot examples (per strategy) ----
EXAMPLES = {
    "A": [  # Correct + High Confidence (Deeper Question)
        {
            "input": {
                "question": "힘이 일정할 때, 질량이 커지면 가속도는 어떻게 변할까요?",
                "model_answer": "F=ma 공식에 따라, 힘이 일정하면 질량이 커질수록 가속도는 작아집니다.",
                "student_answer": "가속도는 작아져요.",
            },
            "output": {
                "topic": "뉴턴의 제2법칙 심화",
                "question": "만약 두 물체 A와 B가 있고, B의 질량이 A의 두 배라면, 같은 힘을 가했을 때 두 물체의 가속도 비율은 어떻게 될까요?",
                "model_answer": "B의 가속도는 A의 가속도의 1/2이 됩니다. 가속도는 질량에 반비례하기 때문입니다.",
                "explanation": "이 질문은 뉴턴의 제2법칙을 단순히 이해하는 것을 넘어, 질량과 가속도의 반비례 관계를 정량적으로 적용할 수 있는지 평가합니다.",
                "difficulty": "hard",
            },
        },
        {
            "input": {
                "question": "운동량 보존 법칙이 무엇인가요?",
                "model_answer": "외력이 작용하지 않는 계에서 전체 운동량은 보존됩니다.",
                "student_answer": "외력이 없으면 전체 운동량은 일정해요.",
            },
            "output": {
                "topic": "운동량 보존의 정량 적용",
                "question": "마찰이 거의 없는 얼음 위에서 두 사람이 서로 밀었더니 반대 방향으로 움직였습니다. 한 사람의 질량이 다른 사람의 두 배라면, 두 사람의 속도 크기 비는 어떻게 될까요?",
                "model_answer": "가벼운 사람이 두 배 빠르게 움직입니다(속도 비 2:1). 운동량 보존으로 m1 v1 = m2 v2 이므로 v는 질량에 반비례합니다.",
                "explanation": "질량-속도 반비례 관계를 실제 상황에 정량 적용하도록 확장합니다.",
                "difficulty": "hard",
            },
        },
        {
            "input": {
                "question": "스넬의 법칙을 말해보세요.",
                "model_answer": "n1 sinθ1 = n2 sinθ2 입니다.",
                "student_answer": "n1 sinθ1 = n2 sinθ2요.",
            },
            "output": {
                "topic": "굴절의 설계적 활용",
                "question": "수영장 바닥 타일이 실제보다 얕아 보이는 이유를 스넬의 법칙으로 설명하고, 물의 굴절률이 더 크면 상의 위치가 어떻게 변할지 말해보세요.",
                "model_answer": "공기→물 경계에서 굴절로 빛 경로가 꺾여 얕아 보입니다. 굴절률이 커질수록 굴절각이 작아져 얕아 보이는 정도가 커집니다.",
                "explanation": "공식 암기에서 일상 시지각 왜곡으로 확장합니다.",
                "difficulty": "hard",
            },
        },
        {
            "input": {
                "question": "전력은 어떻게 계산하나요?",
                "model_answer": "전력 P는 전압 V와 전류 I의 곱 P=VI입니다.",
                "student_answer": "전력은 전압 곱하기 전류요.",
            },
            "output": {
                "topic": "고속 충전의 물리",
                "question": "같은 배터리를 9V·2A 충전기와 5V·3A 충전기로 충전할 때 이상적으로 어느 쪽 전력이 더 큰가요? 이유도 설명하세요.",
                "model_answer": "9V·2A는 18W, 5V·3A는 15W로 9V·2A가 더 큽니다. 전력은 P=VI입니다.",
                "explanation": "수식을 기기 사양 비교로 적용합니다.",
                "difficulty": "hard",
            },
        },
        {
            "input": {
                "question": "자연선택이 무엇인지 설명해보세요.",
                "model_answer": "환경에 더 적응한 형질이 집단에 확산되는 과정입니다.",
                "student_answer": "적응한 개체가 살아남고 번식해요.",
            },
            "output": {
                "topic": "선택 압력의 변화",
                "question": "도시의 야간 조명이 강한 지역에서 어두운 깃과 밝은 깃 중 어떤 형질이 유리할지 포식 회피 관점에서 설명해보세요.",
                "model_answer": "밝은 환경에서 어두운 깃은 눈에 띄어 불리하고, 배경과 위장되는 밝은 깃이 유리할 수 있습니다.",
                "explanation": "환경 요인 변화에 따른 선택 압력을 추론합니다.",
                "difficulty": "hard",
            },
        },
        {
            "input": {
                "question": "판 구조론의 기본을 말해보세요.",
                "model_answer": "판이 움직이며 경계에서 지진·화산 활동이 활발합니다.",
                "student_answer": "판이 움직이고 경계에 사건이 많아요.",
            },
            "output": {
                "topic": "지진 위험 예측의 원리",
                "question": "보존적 전단 경계와 섭입대에서 지진의 깊이 분포가 다른 이유를 설명해보세요.",
                "model_answer": "전단 경계는 얕은 단층 파열로 천발지진이 많고, 섭입대는 판이 깊이 들어가며 넓은 깊이에서 지진이 납니다.",
                "explanation": "경계 유형과 응력 분포의 차이를 연결합니다.",
                "difficulty": "hard",
            },
        },
    ],
    "B": [  # Correct + Low Confidence (Reinforcing question)
        {
            "input": {
                "question": "달에서 물체의 무게가 지구보다 작게 느껴지는 이유는 무엇일까요?",
                "model_answer": "달의 질량이 지구보다 작아서, 중력도 지구보다 약하기 때문입니다.",
                "student_answer": "음 달의 중력이 약해서인 것 같아요.",
            },
            "output": {
                "topic": "중력과 질량의 관계",
                "question": "그렇다면, 질량이 매우 큰 목성에서는 같은 물체의 무게가 지구에서보다 클까요, 작을까요?",
                "model_answer": "목성에서는 무게가 더 큽니다. 목성의 질량이 지구보다 훨씬 크기 때문에 중력도 더 강합니다.",
                "explanation": "이 질문은 학생이 중력과 천체의 질량 사이의 관계를 명확히 이해하고 있는지 확인하여 자신감을 심어주기 위함입니다.",
                "difficulty": "medium",
            },
        },
        {
            "input": {
                "question": "물질의 상태 변화에서 응고는 무엇인가요?",
                "model_answer": "액체가 고체로 변하는 과정입니다.",
                "student_answer": "액체가 얼어서 고체 되는 걸요…?",
            },
            "output": {
                "topic": "상변화 재확인",
                "question": "물과 식용유를 같은 온도에서 냉동고에 넣으면 같은 온도에서 응고될까요?",
                "model_answer": "아니요. 물질마다 응고점이 달라 응고 온도와 방식이 다릅니다.",
                "explanation": "다른 물질 사례로 개념을 재확인합니다.",
                "difficulty": "medium",
            },
        },
        {
            "input": {
                "question": "마찰력은 어떤 힘인가요?",
                "model_answer": "상대 운동 또는 그 경향을 방해하는 힘입니다.",
                "student_answer": "미끄러짐을 막는 힘…?",
            },
            "output": {
                "topic": "마찰의 역할",
                "question": "사람이 앞으로 걸을 때 발에 작용하는 마찰력의 방향은 어디를 향하나요?",
                "model_answer": "발에는 앞으로의 정지 마찰력이 작용해 뒤로 미끄러짐을 막습니다.",
                "explanation": "일상 동작으로 개념을 안정화합니다.",
                "difficulty": "medium",
            },
        },
        {
            "input": {
                "question": "지구의 낮과 밤은 왜 생기나요?",
                "model_answer": "지구가 자전하기 때문입니다.",
                "student_answer": "지구가 돌아서…맞나요?",
            },
            "output": {
                "topic": "자전 효과 강화",
                "question": "낮과 밤의 길이가 거의 같은 시기는 언제인가요?",
                "model_answer": "춘분과 추분입니다.",
                "explanation": "기본 개념을 천문 현상과 연결합니다.",
                "difficulty": "medium",
            },
        },
        {
            "input": {
                "question": "유전은 무엇을 통해 전달되나요?",
                "model_answer": "유전자는 DNA에 담겨 부모에서 자식으로 전달됩니다.",
                "student_answer": "DNA로 전달돼요…아마도요.",
            },
            "output": {
                "topic": "표현형 다양성",
                "question": "형제가 닮았지만 키나 얼굴이 다른 이유를 유전과 환경으로 설명해보세요.",
                "model_answer": "유전적 요인과 환경의 상호작용 때문입니다.",
                "explanation": "유전 개념을 환경 요인과 결합합니다.",
                "difficulty": "medium",
            },
        },
        {
            "input": {
                "question": "에너지 보존 법칙은 무엇인가요?",
                "model_answer": "고립계 총에너지는 형태만 바뀌고 총량은 같습니다.",
                "student_answer": "에너지는 없어지지 않아요.",
            },
            "output": {
                "topic": "에너지 전환 사례",
                "question": "롤러코스터 최고점과 최저점에서 에너지 형태는 어떻게 달라지나요?",
                "model_answer": "최고점은 위치에너지↑, 내려오며 운동에너지로 전환, 최저점은 운동에너지↑.",
                "explanation": "친숙한 상황으로 개념을 강화합니다.",
                "difficulty": "medium",
            },
        },
    ],
    "C": [  # Incorrect + High Confidence (Correctig)
        {
            "input": {
                "question": "식물이 광합성을 하는 주된 이유는 무엇인가요?",
                "model_answer": "스스로 살아가는 데 필요한 양분(포도당)을 만들기 위해서입니다.",
                "student_answer": "산소를 만들어서 우리에게 주기 위해서요!",
            },
            "output": {
                "topic": "광합성의 목적",
                "question": "광합성의 결과로 산소가 나오는 것은 맞지만, 그것이 식물의 주된 목적일까요? 식물 자신에게 가장 먼저 필요한 것은 무엇일지 생각해보세요.",
                "model_answer": "식물 자신에게 가장 먼저 필요한 것은 살아가는 데 사용할 에너지원, 즉 양분(포도당)입니다. 산소는 그 과정에서 나오는 부산물입니다.",
                "explanation": "학생이 '광합성=산소 생성'이라는 단순한 결과에만 집중하는 오개념을 가지고 있을 수 있습니다. 이 질문은 광합성의 주된 목적이 식물 자신의 생존을 위한 양분 생산임을 명확히 하도록 유도합니다.",
                "difficulty": "medium",
            },
        },
        {
            "input": {
                "question": "정맥과 동맥의 차이는 무엇인가요?",
                "model_answer": "동맥은 심장에서 나가고 정맥은 심장으로 들어옵니다.",
                "student_answer": "정맥 피는 파랗고 동맥 피는 빨개요!",
            },
            "output": {
                "topic": "혈액의 실제 색과 오개념",
                "question": "정맥이 파랗게 보인다고 피가 파란 걸까요? 왜 파랗게 보일까요?",
                "model_answer": "피는 붉습니다. 피부·조직에서 빛이 산란·흡수되어 파랗게 보입니다.",
                "explanation": "색 지각과 생리학을 연결해 교정합니다.",
                "difficulty": "medium",
            },
        },
        {
            "input": {
                "question": "소리는 어떤 매질을 통해 전달되나요?",
                "model_answer": "기체·액체·고체 같은 매질을 통해 전달됩니다.",
                "student_answer": "진공에서도 소리 들려요! 영화에서 봤어요.",
            },
            "output": {
                "topic": "소리와 진공",
                "question": "우주(진공)에서 금속을 두드리면 동료가 들을 수 있을까요?",
                "model_answer": "아니요. 매질이 없어 음파가 전달되지 않습니다.",
                "explanation": "대중매체 유발 오개념을 교정합니다.",
                "difficulty": "medium",
            },
        },
        {
            "input": {
                "question": "별이 반짝여 보이는 이유는?",
                "model_answer": "대기 난류로 별빛 굴절·산란이 변동하기 때문입니다.",
                "student_answer": "별이 실제로 깜빡여서 그래요!",
            },
            "output": {
                "topic": "대기 왜곡",
                "question": "우주망원경으로 보면 별이 지상처럼 반짝일까요? 이유는요?",
                "model_answer": "대기 난류가 없어 반짝임이 크게 줄어듭니다.",
                "explanation": "현상 원인을 대기로 재정립합니다.",
                "difficulty": "medium",
            },
        },
        {
            "input": {
                "question": "고래는 어떤 동물인가요?",
                "model_answer": "포유류입니다.",
                "student_answer": "물에서 살아서 물고기죠!",
            },
            "output": {
                "topic": "분류 기준의 재정립",
                "question": "허파로 호흡하고 젖을 먹이면 물에 살아도 물고기일까요?",
                "model_answer": "그 특징은 포유류이므로 고래는 포유류입니다.",
                "explanation": "서식지 아닌 형질로 분류를 교정합니다.",
                "difficulty": "medium",
            },
        },
        {
            "input": {
                "question": "태양광은 흐린 날 전기를 못 만들죠?",
                "model_answer": "생산하지만 출력이 줄어듭니다.",
                "student_answer": "구름이면 0이에요!",
            },
            "output": {
                "topic": "출력 감소와 제로의 구분",
                "question": "흐린 날에도 출력이 0이 아닌 이유를 산란광 관점에서 말해보세요.",
                "model_answer": "산란된 확산광이 도달해 일부 전력을 만듭니다.",
                "explanation": "0/1 이분법 오개념을 수정합니다.",
                "difficulty": "medium",
            },
        },
    ],
    "D": [  # Incorrect + Low Confidence (easy)
        {
            "input": {
                "question": "소금물에서 소금과 물을 어떻게 분리할 수 있을까요?",
                "model_answer": "물을 끓여 증발시키면 소금만 남습니다. 이 과정을 증류라고 합니다.",
                "student_answer": "잘 모르겠어요... 필터로 거르면 안되나요?",
            },
            "output": {
                "topic": "혼합물의 분리",
                "question": "소금물은 소금 입자가 물에 완전히 녹아있는 상태예요. 이런 액체를 끓이면 어떻게 될까요? 물만 수증기로 날아가지 않을까요?",
                "model_answer": "물을 끓이면 수증기가 되어 날아가고, 끓는점이 훨씬 높은 소금은 그대로 남게 됩니다.",
                "explanation": "학생이 용해와 혼합물의 분리 개념을 어려워하고 있습니다. 더 간단한 현상인 '증발'에 집중하도록 유도하여 기본적인 분리 원리를 이해하도록 돕습니다.",
                "difficulty": "easy",
            },
        },
        {
            "input": {
                "question": "열이 높은 곳에서 낮은 곳으로 이동한다는 말의 의미는?",
                "model_answer": "온도 차이로 에너지가 이동한다는 뜻입니다.",
                "student_answer": "잘 모르겠어요…",
            },
            "output": {
                "topic": "전도 기본",
                "question": "뜨거운 머그컵을 오래 잡으면 손이 뜨거워지는 이유를 열 이동으로 말해보세요.",
                "model_answer": "컵에서 손으로 열이 전도되어 손 온도가 올라갑니다.",
                "explanation": "일상 사례로 기본 개념을 정착합니다.",
                "difficulty": "easy",
            },
        },
        {
            "input": {
                "question": "그림자는 왜 생기나요?",
                "model_answer": "빛이 직진하고 물체가 빛을 가리기 때문입니다.",
                "student_answer": "잘 모르겠어요.",
            },
            "output": {
                "topic": "빛의 직진",
                "question": "손전등에서 물체와 벽 거리를 바꾸면 그림자 크기는 어떻게 변하나요?",
                "model_answer": "화면에 가까우면 커지고 멀면 작아집니다.",
                "explanation": "기하 관계로 쉽게 익힙니다.",
                "difficulty": "easy",
            },
        },
        {
            "input": {
                "question": "증발은 어떤 현상인가요?",
                "model_answer": "액체 표면에서 입자가 기체로 나가는 현상입니다.",
                "student_answer": "잘… 모르겠어요.",
            },
            "output": {
                "topic": "증발과 건조",
                "question": "빨래가 바람 불 때 더 빨리 마르는 이유는 무엇인가요?",
                "model_answer": "바람이 수증기를 가져가 농도 차를 유지해 증발이 빨라집니다.",
                "explanation": "현상-원인을 단순 연결합니다.",
                "difficulty": "easy",
            },
        },
        {
            "input": {
                "question": "계절이 생기는 까닭은?",
                "model_answer": "자전축 기울기와 공전으로 태양 고도가 달라지기 때문입니다.",
                "student_answer": "태양에서 멀어져서…?",
            },
            "output": {
                "topic": "태양 고도와 일사",
                "question": "여름에 그림자가 짧아지는 이유를 태양 고도로 설명해보세요.",
                "model_answer": "태양 고도가 높아 빛이 더 수직으로 비칩니다.",
                "explanation": "관찰 지표로 오해를 교정합니다.",
                "difficulty": "easy",
            },
        },
        {
            "input": {
                "question": "부력은 왜 생기나요?",
                "model_answer": "유체의 압력 차로 위쪽 힘이 작용하기 때문입니다.",
                "student_answer": "가벼워서 떠요?",
            },
            "output": {
                "topic": "기본 부력 감각",
                "question": "공기보다 가벼운 기체 풍선이 올라가는 이유를 말해보세요.",
                "model_answer": "밀도가 낮아 부력이 무게보다 커집니다.",
                "explanation": "밀도-부력 핵심을 강조합니다.",
                "difficulty": "easy",
            },
        },
    ],
}


# Prompts : English instructions; model must output Korean-only where specified
PLANNER_PROMPT = ChatPromptTemplate.from_messages(
    [
        (
            "system",
            """You are a strict grader. Determine ONLY whether the student's answer is semantically consistent with the model answer.

Output one JSON object ONLY:
{{"is_correct": true|false}}

Rules:
- Compare meaning; ignore stylistic differences.
- No extra text or keys. Only the JSON above.""",
        ),
        ("user", "Question: {question}\nModelAnswer: {model_answer}\nStudent: {student_answer}"),
    ]
)

strategy_A = """
- Create a **deeper, more applied, or cross-concept question**.
- Extend the concept to a new situation.
- Encourage transfer of knowledge or synthesis across topics.
- Difficulty: **hard**
     """
strategy_B = """
- Create a **concept-reinforcing question**.
- Revisit the same concept with a different perspective or representation.
- Help the student confirm understanding and build confidence.
- Difficulty: **medium**
"""
strategy_C = """
- Create a **misconception-correcting question** that the student is likely to answer differently from before.
- Identify the likely misconception in the student's answer.
- Ask a contrasting or diagnostic question that exposes and corrects the misunderstanding.
- **Do not reuse** key phrases, n-grams, or templates from the original question, model answer, or student answer (except essential technical terms).
- Target **one pivot idea** that directly contradicts the misconception, and require a **prediction or explanation**, not a definition recall.
- Include an explanation that clarifies the correct concept.
- Difficulty: **medium**
"""
strategy_D = """
- Create a **scaffolding or foundational question** to build up from the student's current understanding.
- **Do not reuse** key words, n-grams, or templates from the original question, model answer (except essential technical terms).
- Use concrete examples or guided reasoning to help understanding.
- If the student's answer is very vague, assume that student does not understand the concept at all, and ask a very basic easier question about the core concept.
- Difficulty: **easy**
"""


ACTOR_PROMPT = ChatPromptTemplate.from_messages(
    [
        (
            "system",
            """You are an educational follow-up question generator. Produce ONE concise Korean question.

Strategy: 
{strategy}

Return ONLY this JSON:
{{
  "response": {{
    "topic": "<short topic name>",
    "question": "<the follow-up question>",
    "model_answer": "<ideal answer>",
    "explanation": "<why this question matters and what concept it tests>",
    "difficulty": "easy | medium | hard"
  }}
}}

Output Requirements:
- question ≤ 50 Korean words
- explanation ≤ 30 Korean words
- All fields MUST be in Korean. No extra text.
- Do NOT use any backslash-based notation.
- Write math in plain text only, e.g., "x > 4", "y = 2x + 1".
- Ensure the question is conceptually aligned with the original topic.
- Include a clear model answer and concise explanation.
- The question should help the student **progress** from their current understanding state.

# Few-shot examples (multiple):
# {example}""",
        ),
        ("user", "Original Question: {question}\nModel Answer: {model_answer}\nStudent Answer: {student_answer}\n}}"),
    ]
)


# Rule-based policy
def decide_bucket_confidence(is_correct: bool, eval_grade: float, high_thr: float = 4) -> Tuple[str, str, str, dict]:
    """Return (bucket, confidence, strategy, example) from correctness & eval_grade."""
    confidence = "high" if float(eval_grade) >= float(high_thr) else "low"
    if is_correct and confidence == "high":
        bucket = "A"
        strategy = strategy_A
        example = EXAMPLES["A"]
    elif is_correct and confidence == "low":
        bucket = "B"
        strategy = strategy_B
        example = EXAMPLES["B"]
    elif (not is_correct) and confidence == "high":
        bucket = "C"
        strategy = strategy_C
        example = EXAMPLES["C"]
    else:
        bucket = "D"
        strategy = strategy_D
        example = EXAMPLES["D"]
    return bucket, confidence, strategy, example


def decide_plan(bucket: str, recalled_time: int) -> Literal["ASK", "ONLY_CORRECT"]:
    """Route by recalled_time & bucket."""
    if recalled_time == 0:
        return "ASK"  # always ask on first recall
    if recalled_time in (1, 2):
        return "ONLY_CORRECT" if bucket == "A" else "ASK"
    return "ONLY_CORRECT"  # >=3 → return correctness only


# Graph State
class ReplanState(TypedDict):
    question: str
    model_answer: str
    student_answer: str
    eval_grade: float
    recalled_time: int
    high_thr: float
    # planner output
    is_correct: Optional[bool]
    strategy: Optional[str]
    example: Optional[dict]
    # derived
    bucket: Optional[str]
    confidence: Optional[str]
    plan: Optional[str]
    # final result including tail question
    result: Optional[dict]


# Nodes
def planner_node(state: ReplanState) -> ReplanState:
    """Return only is_correct from LLM; everything else is rule-based."""
    msg = PLANNER_PROMPT.format_messages(
        question=state["question"],
        model_answer=state["model_answer"],
        student_answer=state["student_answer"],
    )
    out = planner_llm.invoke(msg).content
    data = parser.parse(out)  # {"is_correct": true|false}
    is_correct = bool(data["is_correct"])
    return {**state, "is_correct": is_correct}


def derive_and_route_node(state: ReplanState) -> ReplanState:
    """Pure Python: derive bucket/confidence/plan, attach to state."""
    bucket, confidence, strategy, example = decide_bucket_confidence(
        is_correct=bool(state["is_correct"]),
        eval_grade=float(state["eval_grade"]),
        high_thr=float(state["high_thr"]),
    )
    plan = decide_plan(bucket=bucket, recalled_time=int(state["recalled_time"]))
    return {**state, "bucket": bucket, "confidence": confidence, "strategy": strategy, "example": example, "plan": plan}


def actor_node(state: ReplanState) -> ReplanState:
    """Generate the tail question; increment recalled_time in result."""
    next_rt = int(state["recalled_time"]) + 1
    msg = ACTOR_PROMPT.format_messages(
        question=state["question"],
        model_answer=state["model_answer"],
        student_answer=state["student_answer"],
        strategy=state["strategy"],
        example=json.dumps(state["example"], ensure_ascii=False, indent=2),
    )
    out = actor_llm.invoke(msg).content
    response = parser.parse(out)["response"]
    # final result
    result = {
        "plan": "ASK",
        "correctness": "correct" if state["is_correct"] else "incorrect",
        "bucket": state["bucket"],
        "confidence": state["confidence"],
        "recalled_time": next_rt,
        "response": response,
    }
    return {**state, "result": result}


def only_correct_node(state: ReplanState) -> ReplanState:
    """Return only correctness (and counters); no generation."""
    next_rt = int(state["recalled_time"]) + 1
    result = {
        "plan": "ONLY_CORRECT",
        "correctness": "correct" if state["is_correct"] else "incorrect",
        "bucket": state["bucket"],
        "confidence": state["confidence"],
        "recalled_time": next_rt,
        "response": None,
    }
    return {**state, "result": result}


# Conditional routing
def route_after_derive(state: ReplanState) -> Literal["ASK", "ONLY_CORRECT"]:
    return state["plan"] or "ASK"


# Build graph
graph = StateGraph(ReplanState)
graph.add_node("planner", planner_node)
graph.add_node("derive", derive_and_route_node)
graph.add_node("actor", actor_node)
graph.add_node("only_correct", only_correct_node)

graph.set_entry_point("planner")
graph.add_edge("planner", "derive")
graph.add_conditional_edges(
    "derive",
    route_after_derive,
    {
        "ASK": "actor",
        "ONLY_CORRECT": "only_correct",
    },
)
graph.add_edge("actor", END)
graph.add_edge("only_correct", END)

app = graph.compile()


# returns final output (return empty tail question if not generated)
def generate_tail_question(question, model_answer, student_answer, eval_grade, recalled_time, high_thr=4):
    init: ReplanState = {
        "question": question,
        "model_answer": model_answer,
        "student_answer": student_answer,
        "eval_grade": float(eval_grade),
        "recalled_time": int(recalled_time),
        "high_thr": float(high_thr),
        "is_correct": None,
        "bucket": None,
        "confidence": None,
        "plan": None,
        "result": None,
    }

    out = app.invoke(init)
    res = out.get("result", {})
    empty_tail = {
        "topic": "",
        "question": "",
        "model_answer": "",
        "explanation": "",
        "difficulty": "",
    }
    payload = {
        "is_correct": bool(out.get("is_correct", False)),
        "confidence": out.get("confidence"),
        "bucket": out.get("bucket"),
        "plan": res.get("plan"),
        "recalled_time": res.get("recalled_time"),
        "tail_question": res.get("response") or empty_tail,
    }
    return payload


def main():
    ap = argparse.ArgumentParser(description="Planner-only correctness; rule-based routing; always output correctness.")
    ap.add_argument("--question", "-q", type=str, default="호흡 운동에서 가슴이 부풀어 오르는 이유는 무엇인가요?")
    ap.add_argument(
        "--model-answer", "-m", type=str, default="횡격막과 늑간근이 수축하여 흉강의 부피가 증가하기 때문입니다."
    )
    ap.add_argument("--student-answer", "-s", type=str, default="음음.")
    ap.add_argument("--confidence", "-c", type=float, default=2, help="eval_grade")
    ap.add_argument("--recalled-time", "-r", type=int, default=0)
    ap.add_argument("--high-thr", type=float, default=4, help="confidence high threshold")
    args = ap.parse_args()

    t0_total = time.perf_counter()
    payload = generate_tail_question(
        question=args.question,
        model_answer=args.model_answer,
        student_answer=args.student_answer,
        eval_grade=args.confidence,
        recalled_time=args.recalled_time,
        high_thr=args.high_thr,
    )
    total_s = time.perf_counter() - t0_total
    print(f"\nTotal time: {total_s:.2f}s")

    print(json.dumps(payload, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
