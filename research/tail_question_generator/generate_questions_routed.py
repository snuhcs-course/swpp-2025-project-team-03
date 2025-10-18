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

LLM_MODEL = "gpt-5-nano"
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


# 1. Correct + High Confidence (Deeper Question)
EXAMPLE_A = {
    "input": {
        "question": "힘이 일정할 때, 질량이 커지면 가속도는 어떻게 변할까요?",
        "model_answer": "F=ma 공식에 따라, 힘이 일정하면 질량이 커질수록 가속도는 작아집니다.",
        "hint": "F=ma 공식을 생각해보세요.",
        "student_answer": "가속도는 작아져요.",
        "eval_grade": 6.5,
    },
    "output": {
        "topic": "뉴턴의 제2법칙 심화",
        "question": "만약 두 물체 A와 B가 있고, B의 질량이 A의 두 배라면, 같은 힘을 가했을 때 두 물체의 가속도 비율은 어떻게 될까요?",
        "model_answer": "B의 가속도는 A의 가속도의 1/2이 됩니다. 가속도는 질량에 반비례하기 때문입니다.",
        "hint": "가속도와 질량의 관계를 비율로 생각해보세요.",
        "explanation": "이 질문은 뉴턴의 제2법칙을 단순히 이해하는 것을 넘어, 질량과 가속도의 반비례 관계를 정량적으로 적용할 수 있는지 평가합니다.",
        "difficulty": "hard",
    },
}

# 2. Correct + Low Confidence (Reinforcing Question)
EXAMPLE_B = {
    "input": {
        "question": "달에서 물체의 무게가 지구보다 작게 느껴지는 이유는 무엇일까요?",
        "model_answer": "달의 질량이 지구보다 작아서, 중력도 지구보다 약하기 때문입니다.",
        "hint": "중력은 천체의 질량에 영향을 받습니다.",
        "student_answer": "음 달의 중력이 약해서인 것 같아요.",
        "eval_grade": 2.5,
    },
    "output": {
        "topic": "중력과 질량의 관계",
        "question": "그렇다면, 질량이 매우 큰 목성에서는 같은 물체의 무게가 지구에서보다 클까요, 작을까요?",
        "model_answer": "목성에서는 무게가 더 큽니다. 목성의 질량이 지구보다 훨씬 크기 때문에 중력도 더 강합니다.",
        "hint": "천체의 질량이 클수록 중력도 강해집니다.",
        "explanation": "이 질문은 학생이 중력과 천체의 질량 사이의 관계를 명확히 이해하고 있는지 확인하여 자신감을 심어주기 위함입니다.",
        "difficulty": "medium",
    },
}
# 3. Incorrect + High Confidence (Misconception-correcting Question)
EXAMPLE_C = {
    "input": {
        "question": "식물이 광합성을 하는 주된 이유는 무엇인가요?",
        "model_answer": "스스로 살아가는 데 필요한 양분(포도당)을 만들기 위해서입니다.",
        "hint": "식물도 에너지가 필요해요.",
        "student_answer": "산소를 만들어서 우리에게 주기 위해서요!",
        "eval_grade": 6.0,
    },
    "output": {
        "topic": "광합성의 목적",
        "question": "광합성의 결과로 산소가 나오는 것은 맞지만, 그것이 식물의 주된 목적일까요? 식물 자신에게 가장 먼저 필요한 것은 무엇일지 생각해보세요.",
        "model_answer": "식물 자신에게 가장 먼저 필요한 것은 살아가는 데 사용할 에너지원, 즉 양분(포도당)입니다. 산소는 그 과정에서 나오는 부산물입니다.",
        "hint": "모든 생물은 생존을 위해 에너지가 필요합니다.",
        "explanation": "학생이 '광합성=산소 생성'이라는 단순한 결과에만 집중하는 오개념을 가지고 있을 수 있습니다. 이 질문은 광합성의 주된 목적이 식물 자신의 생존을 위한 양분 생산임을 명확히 하도록 유도합니다.",
        "difficulty": "medium",
    },
}
# 4. Incorrect + Low Confidence (Scaffolding Question)
EXAMPLE_D = {
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

strategy_A = """deeper or applied question (difficulty="hard")"""
strategy_B = """reinforce same concept (difficulty="medium")"""
strategy_C = """correct misconception with contrasting question + short explanation (difficulty="medium" or "easy")"""
strategy_D = """foundational, simplified question (difficulty="easy")"""

ACTOR_PROMPT = ChatPromptTemplate.from_messages(
    [
        (
            "system",
            """You are an educational follow-up question generator. Produce ONE concise Korean question.

Strategy: 
- {strategy}
- Keep the topic consistent but make the question distinct.

Return ONLY this JSON:
{{"response":{{"topic":"","question":"","model_answer":"","hint":"","explanation":"","difficulty":""}}}}

Constraints:
- question ≤ 50 Korean words
- explanation ≤ 30 Korean words
- All fields MUST be in Korean. No extra text.

Example:{example}""",
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
        example = EXAMPLE_A
    elif is_correct and confidence == "low":
        bucket = "B"
        strategy = strategy_B
        example = EXAMPLE_B
    elif (not is_correct) and confidence == "high":
        bucket = "C"
        strategy = strategy_C
        example = EXAMPLE_C
    else:
        bucket = "D"
        strategy = strategy_D
        example = EXAMPLE_D
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
        "hint": "",
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
    ap.add_argument(
        "--question", "-q", type=str, default="달에서 물체의 무게가 지구보다 작게 느껴지는 이유는 무엇일까요?"
    )
    ap.add_argument(
        "--model-answer", "-m", type=str, default="달의 질량이 지구보다 작아서, 중력도 지구보다 약하기 때문입니다."
    )
    ap.add_argument("--student-answer", "-s", type=str, default="달의 질량이  지구보다 적어서요.")
    ap.add_argument("--confidence", "-c", type=float, default=6, help="eval_grade")
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
