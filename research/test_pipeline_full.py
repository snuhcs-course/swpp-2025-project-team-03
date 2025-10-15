"""
Testing the full pipeline: quiz generation, feature extraction, inference, and tail question generation.
"""

import argparse
import time

from feature_extractor.extract_all_features import extract_all_features
from inference import run_inference
from question_generator.generate_question_few_shot import generate_quizzes
from tail_question_generator.generate_question_few_shot import build_tail_quiz_single_call


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

    # Step 2: Extract features
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
    tail_quiz = build_tail_quiz_single_call(
        question=quiz.question,
        model_answer=quiz.model_answer,
        student_answer=student_answer,
        eval_grade=confidence_score,
    )
    timings["tail_generation"] = time.perf_counter() - start

    print("\nGenerated Tail Quiz:")
    print(f"Topic: {tail_quiz.get('topic', '')}")
    print(f"Q: {tail_quiz.get('question', '')}")
    print(f"A: {tail_quiz.get('model_answer', '')}")
    print(f"Explanation: {tail_quiz.get('explanation', '')}")
    print(f"Difficulty: {tail_quiz.get('difficulty', '')}")

    # Print timing summary
    print("\n=== Execution Time Summary ===")
    for step, sec in timings.items():
        print(f"{step:20s}: {sec:.3f} sec")
    total_time = sum(timings.values())
    print(f"{'total':20s}: {total_time:.3f} sec")

    return tail_quiz


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
