"""
Inference script for trained multi-class achievement standard classifier.
"""

import argparse
import json
from pathlib import Path
from typing import Dict, List

import pandas as pd
import torch
import torch.nn.functional as F
from train_multiclass_classifier import AchievementClassifier
from transformers import AutoTokenizer


def load_model(model_dir: str, device: torch.device):
    """Load trained model and mappings"""

    model_dir = Path(model_dir)

    # Try to load config, use defaults if not found
    config_path = model_dir / "config.json"
    if not config_path.exists():
        # Try parent directory
        config_path = model_dir.parent / "config.json"

    if config_path.exists():
        with open(config_path, "r", encoding="utf-8") as f:
            config = json.load(f)
    else:
        # Use defaults if config not found
        print("Warning: config.json not found. Using default values.")
        config = {
            "base_model": "klue/roberta-large",
            "max_length": 256,
            "dropout": 0.1,
            "pooling": "cls",
        }

    # Load mappings (required)
    mappings_path = model_dir.parent / "label_mappings.json"
    if not mappings_path.exists():
        mappings_path = model_dir / "label_mappings.json"

    if not mappings_path.exists():
        raise FileNotFoundError(f"label_mappings.json not found in {model_dir} or {model_dir.parent}")

    with open(mappings_path, "r", encoding="utf-8") as f:
        mappings = json.load(f)

    # Get num_classes from mappings if not in config
    if "num_classes" not in config:
        config["num_classes"] = mappings.get("num_classes", len(mappings.get("code_to_idx", {})))

    # Load tokenizer
    tokenizer = AutoTokenizer.from_pretrained(model_dir)

    # Try to infer base_model from tokenizer config if not in config
    if "base_model" not in config or not config["base_model"]:
        # Try to read from tokenizer_config.json
        tokenizer_config_path = model_dir / "tokenizer_config.json"
        if tokenizer_config_path.exists():
            with open(tokenizer_config_path, "r", encoding="utf-8") as f:
                tokenizer_config = json.load(f)
                # Try common fields
                if "model_type" in tokenizer_config:
                    model_type = tokenizer_config["model_type"]
                    if model_type == "roberta":
                        config["base_model"] = "klue/roberta-large"
                    elif model_type == "bert":
                        config["base_model"] = "klue/bert-base"
                    else:
                        config["base_model"] = f"klue/{model_type}-base"
                else:
                    config["base_model"] = "klue/roberta-large"  # Default
        else:
            config["base_model"] = "klue/roberta-large"  # Default

    # Load model
    model = AchievementClassifier(
        model_name=config["base_model"],
        num_classes=config["num_classes"],
        dropout=config.get("dropout", 0.1),
        pooling=config.get("pooling", "cls"),
    )

    model.load_state_dict(torch.load(model_dir / "model.pt", map_location=device))
    model.to(device)
    model.eval()

    return model, tokenizer, config, mappings


def predict_batch(
    model: AchievementClassifier,
    tokenizer,
    texts: List[str],
    device: torch.device,
    max_length: int = 256,
    batch_size: int = 32,
    top_k: int = 5,
    num_classes: int = None,
) -> List[Dict]:
    """
    Predict achievement standards for a batch of texts.

    Returns:
        List of predictions, each containing top-k results with codes, contents, and probabilities.
    """

    all_results = []

    for i in range(0, len(texts), batch_size):
        batch_texts = texts[i : i + batch_size]

        # Tokenize
        encodings = tokenizer(
            batch_texts,
            max_length=max_length,
            padding=True,
            truncation=True,
            return_tensors="pt",
        )

        input_ids = encodings["input_ids"].to(device)
        attention_mask = encodings["attention_mask"].to(device)

        # Predict
        with torch.no_grad():
            outputs = model(input_ids=input_ids, attention_mask=attention_mask)
            logits = outputs["logits"]
            probs = F.softmax(logits, dim=-1)

        # Get top-k predictions for each sample
        # Limit top_k to number of classes to avoid errors
        num_classes_actual = num_classes if num_classes is not None else probs.size(1)
        effective_top_k = min(top_k, num_classes_actual)

        topk_probs, topk_indices = torch.topk(probs, k=effective_top_k, dim=-1)

        for prob_row, idx_row in zip(topk_probs, topk_indices):
            result = {"top_k": []}

            for prob, idx in zip(prob_row.cpu().numpy(), idx_row.cpu().numpy()):
                result["top_k"].append(
                    {
                        "rank": len(result["top_k"]) + 1,
                        "class_idx": int(idx),
                        "probability": float(prob),
                    }
                )

            all_results.append(result)

    return all_results


def predict_texts(
    model_dir: str,
    texts: List[str],
    device: str = "cuda",
    top_k: int = 5,
    batch_size: int = 32,
) -> List[Dict]:
    """Main prediction function"""

    device = torch.device(device if torch.cuda.is_available() else "cpu")

    # Load model
    print(f"Loading model from: {model_dir}")
    model, tokenizer, config, mappings = load_model(model_dir, device)

    idx_to_code = {int(k): v for k, v in mappings["idx_to_code"].items()}
    code_to_content = mappings["code_to_content"]

    print(f"Model loaded. {config['num_classes']} classes.")
    print(f"Predicting for {len(texts)} texts...")

    # Predict
    results = predict_batch(
        model,
        tokenizer,
        texts,
        device,
        max_length=config["max_length"],
        batch_size=batch_size,
        top_k=top_k,
        num_classes=config["num_classes"],
    )

    # Add code and content to results
    for result in results:
        for item in result["top_k"]:
            class_idx = item["class_idx"]
            code = idx_to_code[class_idx]
            item["code"] = code
            item["content"] = code_to_content[code]

    return results


def evaluate_predictions(
    predictions: List[Dict],
    ground_truth_codes: List[str],
    top_k_values: List[int] = [1, 3, 5, 10, 20],
) -> Dict[str, float]:
    """
    Evaluate predictions against ground truth.

    Args:
        predictions: List of prediction results (each with 'top_k' list)
        ground_truth_codes: List of ground truth codes
        top_k_values: List of k values for top-k accuracy

    Returns:
        Dictionary with accuracy metrics for each top-k
    """

    if len(predictions) != len(ground_truth_codes):
        raise ValueError(
            f"Predictions ({len(predictions)}) and ground truth ({len(ground_truth_codes)}) length mismatch"
        )

    # Calculate top-k accuracies
    top_k_accs = {k: 0.0 for k in top_k_values}
    correct_counts = {k: 0 for k in top_k_values}

    for pred, gt_code in zip(predictions, ground_truth_codes):
        # Get predicted codes from top-k
        pred_codes = [item["code"] for item in pred["top_k"]]

        # Check if ground truth is in top-k
        for k in top_k_values:
            if k <= len(pred_codes):
                if gt_code in pred_codes[:k]:
                    correct_counts[k] += 1

    # Calculate accuracies
    total = len(predictions)
    for k in top_k_values:
        top_k_accs[k] = correct_counts[k] / total if total > 0 else 0.0

    return top_k_accs, correct_counts


def predict_from_csv(
    model_dir: str,
    input_csv: str,
    output_csv: str,
    text_column: str = None,
    encoding: str = "utf-8",
    top_k: int = 5,
    batch_size: int = 32,
    evaluate: bool = True,
):
    """
    Predict from CSV file.

    If text_column is not specified, automatically finds all text_* columns
    and processes all texts from those columns.

    If 'code' column exists in CSV, evaluates predictions and prints accuracy metrics.
    """

    # Read CSV
    df = pd.read_csv(input_csv, encoding=encoding)

    # Find text columns
    if text_column:
        # Use specified column
        if text_column not in df.columns:
            raise ValueError(f"Column '{text_column}' not found in CSV")
        text_columns = [text_column]
    else:
        # Auto-detect text_* columns
        text_columns = [c for c in df.columns if c.startswith("text_")]
        if not text_columns:
            raise ValueError("No text_* columns found. Please specify --text_column")
        print(f"Found {len(text_columns)} text columns: {text_columns[:5]}{'...' if len(text_columns) > 5 else ''}")

    # Check if evaluation is possible (has 'code' column)
    has_ground_truth = "code" in df.columns
    if has_ground_truth and evaluate:
        print("Ground truth 'code' column found. Evaluation will be performed.")
    elif evaluate:
        print("No 'code' column found. Skipping evaluation.")

    # Collect all texts from all text columns
    texts = []
    text_to_row_idx = []  # Map each text to its original row index
    ground_truth_codes = []  # Ground truth codes for evaluation

    for idx, row in df.iterrows():
        # Get ground truth code for this row
        gt_code = str(row["code"]).strip() if has_ground_truth else None

        for col in text_columns:
            text = str(row[col]).strip()
            if pd.notna(row[col]) and text != "" and text != "nan":
                texts.append(text)
                text_to_row_idx.append(idx)
                if has_ground_truth:
                    ground_truth_codes.append(gt_code)

    print(f"Total texts to predict: {len(texts)}")

    if len(texts) == 0:
        raise ValueError("No valid texts found in CSV")

    # Load model to get config for num_classes
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    _, _, config, _ = load_model(model_dir, device)

    # Predict
    results = predict_texts(model_dir, texts, top_k=top_k, batch_size=batch_size)

    # Evaluate if ground truth is available
    evaluation_results = None
    if has_ground_truth and evaluate and len(ground_truth_codes) > 0:
        print("\n" + "=" * 80)
        print("EVALUATION RESULTS")
        print("=" * 80)

        # Calculate top-k accuracies
        # Limit top_k_values to available classes and requested top_k
        num_classes = config.get("num_classes", len(set(ground_truth_codes)))
        max_available_k = min(top_k, num_classes)

        top_k_values = [k for k in [1, 3, 5, 10, 20, 30, 40, 50] if k <= max_available_k]
        top_k_accs, correct_counts = evaluate_predictions(results, ground_truth_codes, top_k_values)

        print(f"\nTotal samples: {len(results)}")
        print("\nTop-K Accuracy:")
        print("-" * 50)
        for k in top_k_values:
            acc = top_k_accs[k]
            correct = correct_counts[k]
            print(f"  Top-{k:2d}: {acc * 100:6.2f}% ({correct:5d}/{len(results)})")
        print("-" * 50)
        print("=" * 80)

        # Calculate MRR (Mean Reciprocal Rank)
        mrr = 0.0
        for pred, gt_code in zip(results, ground_truth_codes):
            pred_codes = [item["code"] for item in pred["top_k"]]
            if gt_code in pred_codes:
                rank = pred_codes.index(gt_code) + 1
                mrr += 1.0 / rank
            else:
                mrr += 0.0  # Not found in top-k
        mrr = mrr / len(results) if len(results) > 0 else 0.0

        # Prepare evaluation results for JSON
        input_path = Path(input_csv)

        # Extract folder name (e.g., "valid_80" from "dataset/valid_80/과학.csv")
        folder_parts = input_path.parts
        folder = None
        for i, part in enumerate(folder_parts):
            if "valid" in part or "train" in part:
                folder = part
                break
        if folder is None:
            folder = input_path.parent.name

        # Extract subject from filename (e.g., "과학.csv" -> "과학")
        subject = input_path.stem

        # Count unique codes (standards)
        unique_codes = df["code"].nunique() if "code" in df.columns else len(set(ground_truth_codes))

        # Count max samples per row (number of text_* columns)
        max_samples_per_row = len(text_columns)

        # Build evaluation results in the same format as results.json
        # Round to 4 decimal places for all float values
        evaluation_results = {
            "folder": folder,
            "model_name": str(model_dir),
            "subject": subject,
            "num_standards": unique_codes,
            "max_samples_per_row": max_samples_per_row,
            "total_samples": len(results),
            "top1_acc": round(float(top_k_accs.get(1, 0.0)), 4),
            "top3_acc": round(float(top_k_accs.get(3, 0.0)), 4),
            "top10_acc": round(float(top_k_accs.get(10, 0.0)), 4),
            "top20_acc": round(float(top_k_accs.get(20, 0.0)), 4),
        }

        # Add top30~50_acc if available
        if 30 in top_k_accs:
            evaluation_results["top30_acc"] = round(float(top_k_accs[30]), 4)
        if 40 in top_k_accs:
            evaluation_results["top40_acc"] = round(float(top_k_accs[40]), 4)
        if 50 in top_k_accs:
            evaluation_results["top50_acc"] = round(float(top_k_accs[50]), 4)

        evaluation_results["mrr"] = round(float(mrr), 4)

        # Save to JSON file
        output_dir = "./output" / "classification"
        output_dir.mkdir(parents=True, exist_ok=True)
        results_json_path = output_dir / "results.json"

        # Load existing results if file exists
        existing_results = []
        if results_json_path.exists():
            try:
                with open(results_json_path, "r", encoding="utf-8") as f:
                    content = f.read().strip()
                    if content:  # Only parse if file is not empty
                        existing_results = json.loads(content)
            except (json.JSONDecodeError, ValueError) as e:
                print(f"Warning: Could not parse existing results.json: {e}")
                print("Starting with empty results list.")
                existing_results = []

        # Append new result
        existing_results.append(evaluation_results)

        # Save updated results
        with open(results_json_path, "w", encoding="utf-8") as f:
            json.dump(existing_results, f, indent=2, ensure_ascii=False)

        print(f"\nEvaluation results saved to: {results_json_path}")

    # Create a new dataframe for predictions
    # Each row corresponds to one text prediction
    pred_df = pd.DataFrame(
        {
            "row_idx": text_to_row_idx,
            "text": texts,
        }
    )

    # Add ground truth if available
    if has_ground_truth:
        pred_df["ground_truth_code"] = ground_truth_codes

    # Add predictions
    # Limit to actual number of predictions available (may be less than top_k if top_k > num_classes)
    num_classes = config.get("num_classes", 1000)
    max_predictions = min(top_k, num_classes, 20)  # Support up to top-20 for CSV
    for i in range(max_predictions):
        pred_df[f"pred_code_{i + 1}"] = [r["top_k"][i]["code"] if i < len(r["top_k"]) else "" for r in results]
        pred_df[f"pred_content_{i + 1}"] = [r["top_k"][i]["content"] if i < len(r["top_k"]) else "" for r in results]
        pred_df[f"pred_prob_{i + 1}"] = [r["top_k"][i]["probability"] if i < len(r["top_k"]) else 0.0 for r in results]

    # Add correct flag for top-1 if ground truth available
    if has_ground_truth:
        pred_df["top1_correct"] = pred_df["pred_code_1"] == pred_df["ground_truth_code"]

    # Save predictions only if output_csv is provided
    if output_csv:
        pred_df.to_csv(output_csv, index=False, encoding="utf-8")
        print(f"\nPredictions saved to: {output_csv}")
        print(f"Total predictions: {len(pred_df)}")
    else:
        print("\nOutput CSV not specified. Predictions not saved to file.")

    return pred_df


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Predict achievement standards using trained classifier")

    parser.add_argument("--model_dir", type=str, required=True, help="Path to trained model directory")
    parser.add_argument("--input_csv", type=str, help="Input CSV file")
    parser.add_argument(
        "--output_csv",
        type=str,
        default=None,
        help="Output CSV file (does not save as csv format if not provided)",
    )
    parser.add_argument(
        "--text_column",
        type=str,
        default=None,
        help="Column name containing text (auto-detect text_* columns if not specified)",
    )
    parser.add_argument("--text", type=str, help="Single text to predict")
    parser.add_argument("--encoding", type=str, default="utf-8")
    parser.add_argument("--top_k", type=int, default=40, help="Number of top predictions to return")
    parser.add_argument("--batch_size", type=int, default=32)

    args = parser.parse_args()

    if args.text:
        # Single text prediction
        results = predict_texts(args.model_dir, [args.text], top_k=args.top_k, batch_size=1)

        print("\n" + "=" * 80)
        print("PREDICTION RESULTS")
        print("=" * 80)
        print(f"\nInput text: {args.text}\n")

        for item in results[0]["top_k"]:
            print(f"Rank {item['rank']}:")
            print(f"  Code: {item['code']}")
            print(f"  Probability: {item['probability']:.4f}")
            print(f"  Content: {item['content']}")
            print()

    elif args.input_csv:
        # Batch prediction from CSV
        # Don't auto-generate output path - only save if explicitly provided
        predict_from_csv(
            args.model_dir,
            args.input_csv,
            args.output_csv,  # Can be None - will skip CSV saving
            args.text_column,
            args.encoding,
            args.top_k,
            args.batch_size,
            evaluate=True,  # Always evaluate if ground truth is available
        )
    else:
        parser.print_help()
