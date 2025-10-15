import argparse
import json
import os

from dotenv import load_dotenv
from langchain_core.output_parsers.json import JsonOutputParser
from langchain_core.prompts import ChatPromptTemplate
from langchain_openai import ChatOpenAI

# Load API key
load_dotenv()
if "OPENAI_API_KEY" not in os.environ:
    raise ValueError("OPENAI_API_KEY is not set")


LLM_MODEL = "gpt-5-nano"
llm = ChatOpenAI(
    model=LLM_MODEL,
    temperature=0,
    model_kwargs={"response_format": {"type": "json_object"}},
)
# --- Few-shot Examples ---
FEW_SHOT_EXAMPLES = [
    # 1. Correct + High Confidence (Deeper Question)
    {
        "input": {
            "question": "힘이 일정할 때, 질량이 커지면 가속도는 어떻게 변할까요?",
            "model_answer": "F=ma 공식에 따라, 힘이 일정하면 질량이 커질수록 가속도는 작아집니다.",
            "hint": "F=ma 공식을 생각해보세요.",
            "student_answer": "가속도는 작아져요.",
            "eval_grade": 4.5,
        },
        "output": {
            "topic": "뉴턴의 제2법칙 심화",
            "question": "만약 두 물체 A와 B가 있고, B의 질량이 A의 두 배라면, 같은 힘을 가했을 때 두 물체의 가속도 비율은 어떻게 될까요?",
            "model_answer": "B의 가속도는 A의 가속도의 1/2이 됩니다. 가속도는 질량에 반비례하기 때문입니다.",
            "hint": "가속도와 질량의 관계를 비율로 생각해보세요.",
            "explanation": "이 질문은 뉴턴의 제2법칙을 단순히 이해하는 것을 넘어, 질량과 가속도의 반비례 관계를 정량적으로 적용할 수 있는지 평가합니다.",
            "difficulty": "hard",
        },
    },
    # 2. Correct + Low Confidence (Reinforcing Question)
    {
        "input": {
            "question": "달에서 물체의 무게가 지구보다 작게 느껴지는 이유는 무엇일까요?",
            "model_answer": "달의 질량이 지구보다 작아서, 중력도 지구보다 약하기 때문입니다.",
            "hint": "중력은 천체의 질량에 영향을 받습니다.",
            "student_answer": "음 달의 중력이 약해서인 것 같아요.",
            "eval_grade": 1.5,
        },
        "output": {
            "topic": "중력과 질량의 관계",
            "question": "그렇다면, 질량이 매우 큰 목성에서는 같은 물체의 무게가 지구에서보다 클까요, 작을까요?",
            "model_answer": "목성에서는 무게가 더 큽니다. 목성의 질량이 지구보다 훨씬 크기 때문에 중력도 더 강합니다.",
            "hint": "천체의 질량이 클수록 중력도 강해집니다.",
            "explanation": "이 질문은 학생이 중력과 천체의 질량 사이의 관계를 명확히 이해하고 있는지 확인하여 자신감을 심어주기 위함입니다.",
            "difficulty": "medium",
        },
    },
    # 3. Incorrect + High Confidence (Misconception-correcting Question)
    {
        "input": {
            "question": "식물이 광합성을 하는 주된 이유는 무엇인가요?",
            "model_answer": "스스로 살아가는 데 필요한 양분(포도당)을 만들기 위해서입니다.",
            "hint": "식물도 에너지가 필요해요.",
            "student_answer": "산소를 만들어서 우리에게 주기 위해서요!",
            "eval_grade": 4.0,
        },
        "output": {
            "topic": "광합성의 목적",
            "question": "광합성의 결과로 산소가 나오는 것은 맞지만, 그것이 식물의 주된 목적일까요? 식물 자신에게 가장 먼저 필요한 것은 무엇일지 생각해보세요.",
            "model_answer": "식물 자신에게 가장 먼저 필요한 것은 살아가는 데 사용할 에너지원, 즉 양분(포도당)입니다. 산소는 그 과정에서 나오는 부산물입니다.",
            "hint": "모든 생물은 생존을 위해 에너지가 필요합니다.",
            "explanation": "학생이 '광합성=산소 생성'이라는 단순한 결과에만 집중하는 오개념을 가지고 있을 수 있습니다. 이 질문은 광합성의 주된 목적이 식물 자신의 생존을 위한 양분 생산임을 명확히 하도록 유도합니다.",
            "difficulty": "medium",
        },
    },
    # 4. Incorrect + Low Confidence (Scaffolding Question)
    {
        "input": {
            "question": "소금물에서 소금과 물을 어떻게 분리할 수 있을까요?",
            "model_answer": "물을 끓여 증발시키면 소금만 남습니다. 이 과정을 증류라고 합니다.",
            "hint": "물과 소금의 끓는점이 다른 것을 이용해요.",
            "student_answer": "잘 모르겠어요... 필터로 거르면 안되나요?",
            "eval_grade": 0.5,
        },
        "output": {
            "topic": "혼합물의 분리",
            "question": "소금물은 소금 입자가 물에 완전히 녹아있는 상태예요. 이런 액체를 끓이면 어떻게 될까요? 물만 수증기로 날아가지 않을까요?",
            "model_answer": "물을 끓이면 수증기가 되어 날아가고, 끓는점이 훨씬 높은 소금은 그대로 남게 됩니다.",
            "hint": "라면을 끓일 때 물이 졸아들면 더 짜지는 것을 생각해보세요.",
            "explanation": "학생이 용해와 혼합물의 분리 개념을 어려워하고 있습니다. 더 간단한 현상인 '증발'에 집중하도록 유도하여 기본적인 분리 원리를 이해하도록 돕습니다.",
            "difficulty": "easy",
        },
    },
]


few_shot_prompt = ChatPromptTemplate.from_messages(
    [
        (
            "system",
            """You are an adaptive tutoring agent that generates a **single follow-up (tail) quiz question**
to improve learning outcomes based on the student's previous answer and confidence level.

# Core Goal
Use the student's performance data (correctness + confidence) to design the next question
that best supports conceptual understanding, correction of misconceptions, or deeper reasoning.
     
# Task Overview
You will:
1. Analyze whether the student's answer is correct or incorrect.
2. Interpret the student's confidence level (`eval_grade` between 0.0 and 5.0).
3. Decide which of the four follow-up strategies fits best.
4. Generate a single new question in korean that supports conceptual improvement.(You can utilize the original question, model answer, and hint if needed.)

# Case Guidelines (Correctness × Confidence)
Follow these strategies strictly:

1. Correct + High Confidence (≥ 2.5)
   → Create a **deeper, more applied, or cross-concept question**.
     - Extend the concept to a new situation.
     - Encourage transfer of knowledge or synthesis across topics.
     - Difficulty: **hard** or **medium**

2. Correct + Low Confidence (< 2.5)
   → Create a **concept-reinforcing question**.
     - Revisit the same concept with a different perspective or representation.
     - Help the student confirm understanding and build confidence.
     - Difficulty: **medium**

3. Incorrect + High Confidence (≥ 2.5)
   → Create a **misconception-correcting question**.
     - Identify the likely misconception in the student's answer.
     - Ask a contrasting or diagnostic question that exposes and corrects the misunderstanding.
     - Include an explanation that clarifies the correct concept.
     - Difficulty: **medium** or **easy**

4. Incorrect + Low Confidence (< 2.5)
   → Create a **scaffolding or foundational question**.
     - Simplify the context and focus on the essential concept.
     - Use concrete examples or guided reasoning to help recovery.
     - Difficulty: **easy**

# Output Requirements
- Ensure the question is conceptually aligned with the original topic.
- Include a clear model answer and concise explanation.
- Explicitly label difficulty as "easy", "medium", or "hard".
- The question should help the student **progress** from their current understanding state.
- The question should be shorter than 30 words in korean, and the explanation should be shorter than 50 words in korean.

# Output Format
Return the result strictly as a JSON structure:

{{
  "response": {{
    "topic": "<short topic name>",
    "question": "<the follow-up question>",
    "model_answer": "<ideal answer>",
    "hint": "<hint for the question>",
    "explanation": "<why this question matters and what concept it tests>",
    "difficulty": "easy | medium | hard"
  }}
}}

# Rules
- No Markdown, lists, or commentary outside the JSON.
- Output must be factual, pedagogically sound, and self-contained.
- Respond in Korean.

---
## Examples
{examples}
---
""",
        ),
        (
            "user",
            """Original Question: {question}
Model Answer: {model_answer}
Student Answer: {student_answer}
Confidence Score: {eval_grade}
""",
        ),
    ]
)


def build_tail_quiz_single_call(question, model_answer, student_answer, eval_grade):
    few_shot_str = json.dumps(FEW_SHOT_EXAMPLES, ensure_ascii=False, indent=2)

    msg = few_shot_prompt.format_messages(
        question=question,
        model_answer=model_answer,
        student_answer=student_answer,
        eval_grade=str(eval_grade),
        examples=few_shot_str,
    )
    response = llm.invoke(msg).content
    parser = JsonOutputParser()
    try:
        data = parser.parse(response)
        tail_quiz = data["response"]
    except Exception as e:
        print("⚠️ JSON parsing failed, showing raw output:\n", response)
        raise e
    return tail_quiz  # dict


def main():
    parser = argparse.ArgumentParser(
        description="Generate a single follow-up (tail) quiz question based on student's previous answer and confidence level."
    )
    parser.add_argument("--question", "-q", type=str, default="왜 물체는 지구에서 땅으로 떨어지는가?", help="원문 질문")
    parser.add_argument(
        "--model-answer",
        "-m",
        type=str,
        default="지구의 중력이 물체를 중심 방향으로 끌어당기기 때문이다.",
        help="모범답안",
    )
    parser.add_argument("--student-answer", "-s", type=str, default="무거워서 더 빨리 떨어진다.", help="학생답안")
    parser.add_argument("--confidence", "-c", type=float, default=1.9, help="자신감 점수(0.0~5.0)")
    parser.add_argument("--json", action="store_true", help="결과를 JSON 원본으로 출력")

    args = parser.parse_args()

    result = build_tail_quiz_single_call(
        question=args.question,
        model_answer=args.model_answer,
        student_answer=args.student_answer,
        eval_grade=args.confidence,
    )  # dict: {"topic": ..., "question": ..., "model_answer": ..., "explanation": ..., "difficulty": ...}

    # 출력
    print("\n✅ Generated 1 tail quiz:\n")
    if args.json:
        print(json.dumps(result, ensure_ascii=False, indent=2))
    else:
        print(f"Topic:       {result.get('topic', '')}")
        print(f"Q:           {result.get('question', '')}")
        print(f"A:           {result.get('model_answer', '')}")
        print(f"Explanation: {result.get('explanation', '')}")
        print(f"Difficulty:  {result.get('difficulty', '')}")


if __name__ == "__main__":
    main()
