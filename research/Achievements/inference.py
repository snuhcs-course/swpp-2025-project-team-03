"""
Inference function for single text classification.
Returns top-k achievement standards with random shuffle.
"""

import json
import random
from typing import Dict

import pandas as pd
import torch
from predict_multiclass import load_model, predict_batch
from util import detect_encoding

# Set random seed for shuffle
RANDOM_SEED = 42
random.seed(RANDOM_SEED)


def infer_top_k(
    text: str,
    top_k: int,
    train_csv: str,
    model_dir: str,
    device: str = "cuda",
    random: bool = True,
) -> Dict:
    """
    Predict top-k achievement standards for a given text.

    Args:
        text: Input text string
        top_k: Number of top predictions to return
        train_csv: Path to training CSV file (for code-content mapping, used as fallback)
        model_dir: Path to trained model directory
        device: Device to use (default: "cuda")
        random: If True, shuffle results. If False, keep probability order (highest first)

    Returns:
        Dictionary with text, k, and top-k results (shuffled if random=True, sorted by prob if False)
    """
    # Set device
    device = torch.device(device if torch.cuda.is_available() else "cpu")

    # Load model
    model, tokenizer, config, mappings = load_model(model_dir, device)

    # Load train_csv to get available achievement standards
    encoding = detect_encoding(train_csv)
    df = pd.read_csv(train_csv, encoding=encoding)
    train_codes = set(df["code"].astype(str).unique())

    # Get code-content mapping from train_csv
    code_to_content = df.groupby("code")["content"].first().to_dict()

    # Get idx_to_code from mappings
    idx_to_code = {int(k): v for k, v in mappings["idx_to_code"].items()}

    # Predict - get predictions for ALL classes first
    # We need to get all probabilities, not just top-k
    results = predict_batch(
        model,
        tokenizer,
        [text],
        device,
        max_length=config["max_length"],
        batch_size=1,
        top_k=config["num_classes"],  # Get all predictions
        num_classes=config["num_classes"],
    )

    # Extract all predictions with probabilities, filter by train_csv codes
    all_predictions = []
    for item in results[0]["top_k"]:
        code = idx_to_code[item["class_idx"]]

        # Only include codes that exist in train_csv
        if code in train_codes:
            content = code_to_content.get(code, "N/A")
            all_predictions.append({"code": code, "content": content, "probability": item["probability"]})

    # Sort by probability (highest first)
    all_predictions.sort(key=lambda x: x["probability"], reverse=True)

    # Take top-k
    top_k_list = all_predictions[:top_k]

    # Shuffle or keep sorted by probability
    if random:
        # Random shuffle with fixed seed
        random.shuffle(top_k_list)
    # else: keep sorted by probability (already sorted)

    # Remove probability from final output
    for item in top_k_list:
        item.pop("probability", None)

    # Build result dictionary
    result = {"text": text, "k": top_k, "top-k": top_k_list}

    return result


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description="Infer top-k achievement standards for a text")
    parser.add_argument("--text", type=str, required=True, help="Input text")
    parser.add_argument("--top_k", type=int, required=True, help="Number of top predictions")
    parser.add_argument("--train_csv", type=str, required=True, help="Training CSV path")
    parser.add_argument("--model_dir", type=str, required=True, help="Trained model directory")
    parser.add_argument("--device", type=str, default="cuda", help="Device (cuda/cpu)")
    parser.add_argument(
        "--random",
        action="store_true",
        default=True,
        help="Shuffle results (default: True). Use --no-random to keep probability order",
    )
    parser.add_argument(
        "--no-random",
        action="store_false",
        dest="random",
        help="Keep results sorted by probability (highest first)",
    )

    args = parser.parse_args()

    result = infer_top_k(
        text=args.text,
        top_k=args.top_k,
        train_csv=args.train_csv,
        model_dir=args.model_dir,
        device=args.device,
        random=args.random,
    )

    # Print as JSON
    print(json.dumps(result, indent=2, ensure_ascii=False))
