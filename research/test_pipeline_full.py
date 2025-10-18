"""
Testing the full pipeline: quiz generation, feature extraction, inference, and tail question generation.
"""

import argparse
import time

from feature_extractor.extract_all_features import extract_all_features
from inference import run_inference
from question_generator.generate_question_few_shot import generate_quizzes
from tail_question_generator.generate_questions_routed import generate_tail_question


def test_pipeline_full(wav_path: str, model_name: str, learning_material: str, n_questions: int, xgbmodel_path: str):
    timings = {}

    # Step 1: Generate quizzes based on the learning material
    start = time.perf_counter()
    quizzes = generate_quizzes(learning_material, n_questions)
    timings["quiz_generation"] = time.perf_counter() - start

    print("\nGenerated Quizzes:")
    for i, quiz in enumerate(quizzes):
        print(f"Quiz {i + 1}:")
        print(f"Topic: {quiz.topic}")
        print(f"Question: {quiz.question}")
        print(f"Model Answer: {quiz.model_answer}")
        print(f"Explanation: {quiz.explanation}")
        print(f"Difficulty: {quiz.difficulty}")
        print()

    quiz = quizzes[0]

    # Step 2: Extract features from the WAV file
    start = time.perf_counter()
    features = extract_all_features(wav_path, model_name)
    timings["feature_extraction"] = time.perf_counter() - start

    print("Extracted Features:")
    for key, value in features.items():
        print(f"{key}: {value}")

    # Step 3: Run inference
    start = time.perf_counter()
    inference_results = run_inference(xgbmodel_path, features)
    timings["inference"] = time.perf_counter() - start

    print("\nInference Results:")
    print(inference_results)

    # Step 4: Generate tail quiz
    student_answer = features["script"]
    confidence_score = inference_results["pred_cont"]

    start = time.perf_counter()
    tail = generate_tail_question(
        question=quiz.question,
        model_answer=quiz.model_answer,
        student_answer=student_answer,
        eval_grade=confidence_score,
        recalled_time=0,
    )
    timings["tail_generation"] = time.perf_counter() - start

    print("\nTail Generation Result:")
    print(f"- Correct: {tail.get('is_correct')}")
    print(f"- Confidence: {tail.get('confidence')}")
    print(f"- Bucket: {tail.get('bucket')}")
    print(f"- Plan: {tail.get('plan')}")
    print(f"- RecalledTime -> {tail.get('recalled_time')}")

    # 꼬리질문 출력 (ASK인 경우에만 내용이 의미 있음)
    tq = tail.get("tail_question", {}) or {}
    if tail.get("plan") == "ASK":
        print("\nGenerated Tail Quiz:")
        print(f"Topic: {tq.get('topic', '')}")
        print(f"Q: {tq.get('question', '')}")
        print(f"A: {tq.get('model_answer', '')}")
        print(f"Hint: {tq.get('hint', '')}")
        print(f"Explanation: {tq.get('explanation', '')}")
        print(f"Difficulty: {tq.get('difficulty', '')}")
    else:
        print("\nNo tail question generated (plan was ONLY_CORRECT).")

    # Print timing summary
    print("\n=== Execution Time Summary ===")
    for step, sec in timings.items():
        print(f"{step:20s}: {sec:.3f} sec")
    response_time = 0
    for step, sec in timings.items():
        if step != "quiz_generation":
            response_time += sec
    print(f"{'Total response time':20s}: {response_time:.3f} sec")

    return tail


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Test the full pipeline.")
    parser.add_argument("--wav_path", type=str, required=True, help="Path to the input WAV file.")
    parser.add_argument(
        "--model_name",
        type=str,
        default="snunlp/KR-SBERT-V40K-klueNLI-augSTS",
        help="SBERT model name for semantic feature extraction.",
    )
    parser.add_argument(
        "--learning_material", type=str, required=True, help="Learning material pdf for quiz generation."
    )
    parser.add_argument("--n_questions", type=int, default=3, help="Number of questions to generate.")
    parser.add_argument(
        "--xgbmodel_path", type=str, default="model.joblib", help="Path to the trained inference model."
    )
    args = parser.parse_args()

    test_pipeline_full(args.wav_path, args.model_name, args.learning_material, args.n_questions, args.xgbmodel_path)
