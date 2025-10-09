"""
Quiz Generation ReAct Agent using LangGraph

This script implements a multi-step agent to generate a review quiz question based on educational material.
The agent follows a planner -> executor -> replanner loop to iteratively refine the quiz.

Architecture:
- Planner: Creates an initial step-by-step plan to generate a quiz.
- Executor: Executes each step of the plan. It's a ReAct agent that can use tools.
- Replanner: Evaluates the result of the execution, and either finalizes the quiz or creates a new plan for refinement.

State Management:
- The process is managed by a state graph (`langgraph`).
- The state includes the input material, the current plan, executed steps, and the final quiz response.

"""

import os
from typing import List, Optional, Tuple, TypedDict

from dotenv import load_dotenv
from langchain.agents import AgentExecutor, create_openai_tools_agent
from langchain.tools import tool
from langchain_core.output_parsers.json import JsonOutputParser
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_openai import ChatOpenAI
from langgraph.graph import END, StateGraph
from pydantic import BaseModel, Field

# --- Environment Setup ---
# Load environment variables from .env file
load_dotenv()

# Ensure the OpenAI API key is set
if "OPENAI_API_KEY" not in os.environ:
    raise ValueError("OPENAI_API_KEY environment variable not set.")

# --- Data Structures ---


class Quiz(BaseModel):
    """Represents a single quiz question."""

    topic: str = Field(description="Topic or learning goal of the quiz.")
    question: str = Field(description="The quiz question.")
    model_answer: str = Field(description="The ideal answer.")
    explanation: str = Field(description="Concise rationale explaining the concept behind the answer.")
    difficulty: str = Field(description="One of ['easy', 'medium', 'hard'].")


class AgentState(TypedDict):
    """Represents the state of the agent graph."""

    input: str
    plan: List[str]
    past_steps: List[Tuple[str, str]]
    response: Optional[Quiz]


# --- Prompts ---

planner_prompt = ChatPromptTemplate.from_messages(
    [
        (
            "system",
            """You are an AI educational planner creating a review quiz from class material.
For the given learning content, design a clear step-by-step plan that leads to generating a high-quality review quiz.

Each step should be concrete, logically ordered, and executable by another agent.
Avoid redundancy and ensure that each step uses all available information.
Think in English, but do not think too much on planning.

The plan should always follow this broad structure:
1. Summarize key learning concepts from the material.
2. Generate an initial quiz question in Korean, model answer, and explanation based on the summary.
3. Evaluate the clarity, correctness, and educational value of the generated quiz.
4. Refine the question, answer, and explanation to improve quality and align with the learning objective.
5. Finalize the quiz and assign a difficulty rating.

Output your plan as a numbered list of steps.""",
        ),
        ("user", "{messages}"),
    ]
)

react_system_prompt = """You are an AI quiz author and reasoning agent.
You are executing a single step of a larger plan to create a quiz question.
Note that question should be written in Korean.
You have access to the following tools to gather information:
- `web_search`: search the internet for factual support.
- `vector_search`: retrieve passages from the class material.

Follow this reasoning–acting process:
(1) Understand the goal of the current step.
(2) Think about what information you need and decide which tool (if any) to use.
(3) Execute the tool to get the information.
(4) Synthesize the information to accomplish the step's goal.
(5) Provide a summary of what you accomplished as the output.

Your final output for this step should be a concise text summary of your result."""

executor_prompt = ChatPromptTemplate.from_messages(
    [
        ("system", react_system_prompt),
        ("user", "{messages}"),
        MessagesPlaceholder(variable_name="agent_scratchpad"),
    ]
)

replanner_prompt = ChatPromptTemplate.from_template(
    """
You are an AI educational evaluator agent.
Your goal is to refine or finalize the generation of a **single quiz question in Korean**.

# Context
Learning Objective:
{input}

Original Plan:
{plan}

Executed Steps (with results):
{past_steps}

# Instructions
1. Carefully evaluate the quality of the results from the executed steps.
   - Assess if the outcomes directly align with the learning objective.
   - Check for clarity, conceptual depth, and educational value in the generated content.
   - Ensure the model answer and explanation are correct, complete, and pedagogically sound.
   - Evaluate whether the difficulty label ('easy', 'medium', 'hard') is appropriate, based on these descriptions:
     - easy: Simple factual recall.
     - medium: Requires understanding of concepts from the material.
     - hard: Requires complex reasoning and synthesis of multiple concepts.

2. If the current result is incomplete or low-quality:
   - Propose a **new, concise plan** to fix the issues. Focus on improving reasoning, clarity, and alignment.

3. If the result is a high-quality quiz that meets the educational goal:
   - Produce a **Response** object containing the completed quiz.

# Output Format
You must return a single JSON object with an "action" key. The value of "action" must be **one** of the following:

Example (New Plan):
{{
  "action": {{
    "steps": [
      "Revise the question to better test conceptual understanding.",
      "Improve explanation clarity and correctness."
    ]
  }}
}}

Example (Final Response):
{{
  "action": {{
    "response": {{
      "topic": "Newton's Second Law",
      "question": "힘이 일정할 때, 질량이 커지면 가속도는 어떻게 변하나요?",
      "model_answer": "F = ma에 따라, 힘이 일정하다면 질량이 커지면 가속도는 감소합니다.",
      "explanation": "뉴턴의 제 2법칙에 따라 힘이 일정할 때 질량과 가속도가 반비례 관계임을 이해할 수 있다.",
      "difficulty": "medium"
    }}
  }}
}}
"""
)

# Note: The `vision_summary_prompt` and `final_report_prompt` from the user's request
# have been omitted. The vision summary logic would be part of a tool implementation,
# and the final report's function is effectively handled by the replanner's ability
# to output a final `Quiz` object.

# --- Tools for the Executor Agent ---


@tool
def web_search(query: str) -> str:
    """Searches the internet for factual support. Use for general knowledge or fact-checking."""
    # This is a placeholder. A real implementation would use a search API (e.g., Tavily, Google Search).
    print(f"--- Executing Web Search: {query} ---")
    return f"Placeholder search results for '{query}'."


@tool
def vector_search(query: str) -> str:
    """Retrieves relevant passages from the provided class material (vectorized)."""
    # This is a placeholder. A real implementation would use a vector store
    # created from the user's input material (e.g., PDF, text).
    print(f"--- Executing Vector Search: {query} ---")
    return f"Placeholder vector search results for '{query}' from the class material."


# --- Agent and Graph Nodes ---

# LLM and Tools setup
llm = ChatOpenAI(model="gpt-4-turbo", temperature=0, streaming=True)
tools = [web_search, vector_search]


# 1. Planner Node: Creates the initial plan.
def planner_node(state: AgentState) -> dict:
    print("--- Planning ---")
    prompt = planner_prompt.format_messages(messages=state["input"])
    plan_str = llm.invoke(prompt).content
    # Parse the numbered list into a list of strings
    plan = [
        step.strip().split(". ", 1)[1] for step in plan_str.split("\n") if step.strip() and step.strip()[0].isdigit()
    ]
    print(f"Initial Plan: {plan}")
    return {"plan": plan}


# 2. Executor Node: A ReAct agent that executes the plan, step by step.
executor_agent_runnable = create_openai_tools_agent(llm, tools, executor_prompt)
executor_agent = AgentExecutor(agent=executor_agent_runnable, tools=tools, verbose=True)


def executor_node(state: AgentState) -> dict:
    print("--- Executing Plan ---")
    past_steps = state.get("past_steps", [])
    plan = state["plan"]

    # Execute all steps in the current plan
    for i, step in enumerate(plan):
        print(f"\n--- Step {i + 1}: {step} ---")
        messages = f"Learning Objective: {state['input']}\n\nPrevious Steps: {past_steps}\n\nCurrent Step: {step}"
        result = executor_agent.invoke({"messages": messages})
        past_steps.append((step, result["output"]))

    return {"past_steps": past_steps}


# 3. Replanner Node: Evaluates the execution and decides to loop or finish.
class ReplannerAction(BaseModel):
    steps: List[str] = Field(default_factory=list)
    response: Optional[Quiz] = None


class ReplannerDecision(BaseModel):
    action: ReplannerAction


replanner_parser = JsonOutputParser(pydantic_object=ReplannerDecision)
replanner_chain = replanner_prompt | llm | replanner_parser


def replanner_node(state: AgentState) -> dict:
    print("--- Replanning / Evaluating ---")
    if not state["past_steps"]:
        raise ValueError("No steps executed yet. Cannot replan.")

    output = replanner_chain.invoke({"input": state["input"], "plan": state["plan"], "past_steps": state["past_steps"]})

    # The output from JsonOutputParser is a dict, so use dictionary access
    action = output.get("action", {})
    response_data = action.get("response")
    steps = action.get("steps")

    if response_data:
        print("--- Evaluation Complete: Final quiz generated. ---")
        # Convert dict to Quiz pydantic model for type consistency in the state
        final_quiz = Quiz(**response_data)
        return {"response": final_quiz}
    elif steps:
        print(f"--- Evaluation Complete: Replanning with new steps: {steps} ---")
        # Reset past steps for the new plan
        return {"plan": steps, "past_steps": []}
    else:
        # If the replanner fails to produce a valid action, end the process.
        print("--- Evaluation Failed: Replanner did not provide a valid next action. ---")
        return {"response": None}


# 4. Conditional Edge: Determines the next step after replanning.
def should_replan(state: AgentState) -> str:
    if state.get("response"):
        return "end"
    else:
        return "execute"


# --- Graph Definition ---

workflow = StateGraph(AgentState)

workflow.add_node("planner", planner_node)
workflow.add_node("executor", executor_node)
workflow.add_node("replanner", replanner_node)

workflow.set_entry_point("planner")
workflow.add_edge("planner", "executor")
workflow.add_edge("executor", "replanner")
workflow.add_conditional_edges(
    "replanner",
    should_replan,
    {
        "end": END,
        "execute": "executor",  # Loop back to the executor with the new plan
    },
)

# Compile the graph
graph = workflow.compile()

# --- Main Execution ---

if __name__ == "__main__":
    # Example usage:
    learning_material = """
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

    inputs = {"input": learning_material}

    print("Starting quiz generation process...")
    final_state = graph.invoke(inputs)

    if final_state and final_state.get("response"):
        final_quiz = final_state["response"]
        print("\n--- FINAL QUIZ ---")
        print(f"Topic: {final_quiz.topic}")
        print(f"Question: {final_quiz.question}")
        print(f"Answer: {final_quiz.model_answer}")
        print(f"Explanation: {final_quiz.explanation}")
        print(f"Difficulty: {final_quiz.difficulty}")
    else:
        print("\n--- Process finished without a final quiz. ---")
        print("Final State:", final_state)
