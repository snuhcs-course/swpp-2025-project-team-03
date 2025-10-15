"""
Testing the full pipeline: quiz generation, feature extraction, inference, and tail question generation.
"""

import argparse

from feature_extractor.extract_all_features import extract_all_features
from inference import run_inference
from question_generator.generate_question_few_shot import generate_quizzes
from tail_question_generator.generate_question_few_shot import build_tail_quiz_single_call


def test_pipeline_full(wav_path: str, model_name: str, learning_material: str, n_questions: int, xgbmodel_path: str):
    # Step 1: Generate quizzes based on the learning material
    quizzes = generate_quizzes(learning_material, n_questions)
    print("\nGenerated Quizzes:")
    for i, quiz in enumerate(quizzes):
        print(f"Quiz {i + 1}:")
        print(f"Topic: {quiz.topic}")
        print(f"Question: {quiz.question}")
        print(f"Model Answer: {quiz.model_answer}")
        print(f"Explanation: {quiz.explanation}")
        print(f"Difficulty: {quiz.difficulty}")
        print()

    quiz = quizzes[0]  # Take the first quiz
    # Step 2: Extract features from the audio file(assuming the audio corresponds to the first quiz)
    features = extract_all_features(wav_path, model_name)

    print("Extracted Features:")
    for key, value in features.items():
        print(f"{key}: {value}")

    # Step 3: Run inference to get the confidence score

    inference_results = run_inference(xgbmodel_path, features)

    print("\nInference Results:")
    print(inference_results)

    # Step 4: Generate a follow-up question based on the student's answer and confidence score
    student_answer = features["script"]  # Using the transcribed script as the student's answer
    confidence_score = inference_results["pred_cont"]
    tail_quiz = build_tail_quiz_single_call(
        question=quiz.question,
        model_answer=quiz.model_answer,
        student_answer=student_answer,
        eval_grade=confidence_score,
    )
    print("\nGenerated Tail Quiz:")
    print(f"Topic: {tail_quiz.get('topic', '')}")
    print(f"Q: {tail_quiz.get('question', '')}")
    print(f"A: {tail_quiz.get('model_answer', '')}")
    print(f"Explanation: {tail_quiz.get('explanation', '')}")
    print(f"Difficulty: {tail_quiz.get('difficulty', '')}")

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
