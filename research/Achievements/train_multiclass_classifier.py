"""
Multi-Class Classification for Text-to-Achievement Standard Matching

This approach treats each achievement standard (code) as a separate class.
Given a text, the model predicts which achievement standard it belongs to.

Advantages over bi-encoder:
- Direct classification → Higher accuracy
- Single forward pass → Fast inference
- Better semantic understanding
- Can use advanced techniques (label smoothing, focal loss, etc.)

Optimized for L40S GPU.
"""

import argparse
import json
import random
from pathlib import Path
from typing import Dict, List

import numpy as np
import pandas as pd
import torch
import torch.nn as nn
import torch.nn.functional as F
from sklearn.metrics import accuracy_score, f1_score, precision_recall_fscore_support, top_k_accuracy_score
from sklearn.model_selection import train_test_split
from torch.utils.data import DataLoader, Dataset
from tqdm import tqdm
from transformers import AutoConfig, AutoModel, AutoTokenizer, get_cosine_schedule_with_warmup, set_seed
from util import detect_encoding


def set_train_random_seed(seed: int):
    """set seeds for reproducibility"""
    random.seed(seed)
    np.random.seed(seed)
    torch.manual_seed(seed)
    torch.cuda.manual_seed_all(seed)
    torch.backends.cudnn.deterministic = True
    torch.backends.cudnn.benchmark = False
    set_seed(seed)


class AchievementDataset(Dataset):
    """Dataset for achievement standard classification"""

    def __init__(self, texts: List[str], labels: List[int], tokenizer, max_length: int = 256):
        self.texts = texts
        self.labels = labels
        self.tokenizer = tokenizer
        self.max_length = max_length

    def __len__(self):
        return len(self.texts)

    def __getitem__(self, idx):
        text = str(self.texts[idx])
        label = self.labels[idx]

        encoding = self.tokenizer(
            text,
            max_length=self.max_length,
            padding="max_length",
            truncation=True,
            return_tensors="pt",
        )

        return {
            "input_ids": encoding["input_ids"].squeeze(0),
            "attention_mask": encoding["attention_mask"].squeeze(0),
            "labels": torch.tensor(label, dtype=torch.long),
        }


class AchievementClassifier(nn.Module):
    """
    Multi-class classifier for achievement standards.

    Architecture:
    - Pretrained Korean BERT/RoBERTa
    - Optional pooling layer
    - Dropout for regularization
    - Classification head
    """

    def __init__(
        self,
        model_name: str,
        num_classes: int,
        dropout: float = 0.1,
        pooling: str = "cls",
        hidden_size: int = None,
    ):
        super().__init__()

        self.config = AutoConfig.from_pretrained(model_name)
        self.encoder = AutoModel.from_pretrained(model_name)
        self.pooling = pooling

        hidden_dim = self.config.hidden_size

        # Optional intermediate layer for better capacity
        if hidden_size:
            self.intermediate = nn.Sequential(nn.Linear(hidden_dim, hidden_size), nn.Tanh(), nn.Dropout(dropout))
            hidden_dim = hidden_size
        else:
            self.intermediate = None

        self.dropout = nn.Dropout(dropout)
        self.classifier = nn.Linear(hidden_dim, num_classes)

    def forward(self, input_ids, attention_mask, labels=None):
        outputs = self.encoder(input_ids=input_ids, attention_mask=attention_mask)

        # Pooling
        if self.pooling == "cls":
            pooled = outputs.last_hidden_state[:, 0]  # CLS token
        elif self.pooling == "mean":
            # Mean pooling with attention mask
            token_embeddings = outputs.last_hidden_state
            attention_mask_expanded = attention_mask.unsqueeze(-1).expand(token_embeddings.size()).float()
            sum_embeddings = torch.sum(token_embeddings * attention_mask_expanded, 1)
            sum_mask = torch.clamp(attention_mask_expanded.sum(1), min=1e-9)
            pooled = sum_embeddings / sum_mask
        else:
            pooled = outputs.pooler_output

        # Intermediate layer if exists
        if self.intermediate:
            pooled = self.intermediate(pooled)

        pooled = self.dropout(pooled)
        logits = self.classifier(pooled)

        loss = None
        if labels is not None:
            loss_fct = nn.CrossEntropyLoss()
            loss = loss_fct(logits, labels)

        return {"loss": loss, "logits": logits}


class FocalLoss(nn.Module):
    """
    Focal Loss for handling class imbalance.
    Focuses more on hard examples.
    """

    def __init__(self, alpha=1.0, gamma=2.0, reduction="mean"):
        super().__init__()
        self.alpha = alpha
        self.gamma = gamma
        self.reduction = reduction

    def forward(self, inputs, targets):
        ce_loss = F.cross_entropy(inputs, targets, reduction="none")
        pt = torch.exp(-ce_loss)
        focal_loss = self.alpha * (1 - pt) ** self.gamma * ce_loss

        if self.reduction == "mean":
            return focal_loss.mean()
        elif self.reduction == "sum":
            return focal_loss.sum()
        else:
            return focal_loss


class LabelSmoothingCrossEntropy(nn.Module):
    """
    Label Smoothing for better generalization.
    """

    def __init__(self, epsilon: float = 0.1):
        super().__init__()
        self.epsilon = epsilon

    def forward(self, outputs, targets):
        n_classes = outputs.size(-1)
        log_probs = F.log_softmax(outputs, dim=-1)

        # One-hot with label smoothing
        targets_one_hot = torch.zeros_like(log_probs).scatter_(1, targets.unsqueeze(1), 1)
        targets_one_hot = targets_one_hot * (1 - self.epsilon) + self.epsilon / n_classes

        loss = (-targets_one_hot * log_probs).sum(dim=-1).mean()
        return loss


def prepare_data(csv_path: str, encoding: str = None, max_samples_per_class: int = None):
    """
    Prepare training data from CSV.

    Returns:
        texts: List of text samples
        labels: List of label indices
        code_to_idx: Mapping from code to index
        idx_to_code: Mapping from index to code
        code_to_content: Mapping from code to content description
    """
    if not encoding:
        encoding = detect_encoding(csv_path)

    df = pd.read_csv(csv_path, encoding=encoding)

    # Get text columns
    text_cols = [c for c in df.columns if c.startswith("text_")]

    # Build label mapping
    unique_codes = df["code"].unique()
    code_to_idx = {code: idx for idx, code in enumerate(sorted(unique_codes))}
    idx_to_code = {idx: code for code, idx in code_to_idx.items()}

    # Build code to content mapping
    code_to_content = df.groupby("code")["content"].first().to_dict()

    # Collect all samples
    texts = []
    labels = []

    for _, row in df.iterrows():
        code = row["code"]
        label_idx = code_to_idx[code]

        # Collect all texts for this row
        row_texts = [str(row[col]).strip() for col in text_cols if pd.notna(row[col]) and str(row[col]).strip() != ""]

        if max_samples_per_class:
            row_texts = row_texts[:max_samples_per_class]

        for text in row_texts:
            texts.append(text)
            labels.append(label_idx)

    return texts, labels, code_to_idx, idx_to_code, code_to_content


def evaluate_model(
    model: nn.Module,
    dataloader: DataLoader,
    device: torch.device,
    idx_to_code: Dict[int, str],
) -> Dict:
    """Comprehensive evaluation"""

    model.eval()

    all_preds = []
    all_labels = []
    all_probs = []

    with torch.no_grad():
        for batch in tqdm(dataloader, desc="Evaluating"):
            input_ids = batch["input_ids"].to(device)
            attention_mask = batch["attention_mask"].to(device)
            labels = batch["labels"].to(device)

            outputs = model(input_ids=input_ids, attention_mask=attention_mask)
            logits = outputs["logits"]

            probs = F.softmax(logits, dim=-1)
            preds = torch.argmax(logits, dim=-1)

            all_preds.extend(preds.cpu().numpy())
            all_labels.extend(labels.cpu().numpy())
            all_probs.extend(probs.cpu().numpy())

    all_preds = np.array(all_preds)
    all_labels = np.array(all_labels)
    all_probs = np.array(all_probs)

    # Calculate metrics
    accuracy = accuracy_score(all_labels, all_preds)

    # Top-k accuracies
    # Only calculate top-k accuracy if k <= number of classes to avoid errors
    top_k_accs = {}
    num_classes = len(idx_to_code)
    for k in [1, 3, 5, 10, 20]:
        if k <= num_classes:
            top_k_acc = top_k_accuracy_score(all_labels, all_probs, k=k, labels=list(range(num_classes)))
            top_k_accs[f"top_{k}_acc"] = top_k_acc

    # Precision, Recall, F1
    precision, recall, f1, _ = precision_recall_fscore_support(
        all_labels, all_preds, average="weighted", zero_division=0
    )

    # Macro F1 for balanced view
    macro_f1 = f1_score(all_labels, all_preds, average="macro", zero_division=0)

    metrics = {
        "accuracy": accuracy,
        "precision": precision,
        "recall": recall,
        "f1_weighted": f1,
        "f1_macro": macro_f1,
        **top_k_accs,
    }

    return metrics


def train_classifier(
    input_csv: str,
    base_model: str = "klue/roberta-large",
    output_dir: str = None,
    encoding: str = None,
    test_size: float = 0.2,
    max_samples_per_class: int = None,
    max_length: int = 256,
    batch_size: int = 32,
    epochs: int = 10,
    lr: float = 2e-5,
    warmup_ratio: float = 0.1,
    weight_decay: float = 0.01,
    dropout: float = 0.1,
    pooling: str = "cls",
    loss_type: str = "ce",  # ce, focal, label_smoothing
    label_smoothing: float = 0.1,
    focal_alpha: float = 1.0,
    focal_gamma: float = 2.0,
    early_stopping_patience: int = 3,
    mixed_precision: bool = True,
    gradient_accumulation_steps: int = 1,
):
    """
    Train multi-class classifier for achievement standard prediction.
    """

    # Setup
    if output_dir is None:
        output_dir = "./model" / "classification"
    output_dir = Path(output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")

    print("=" * 80)
    print("MULTI-CLASS ACHIEVEMENT STANDARD CLASSIFIER")
    print("=" * 80)

    # Prepare data
    print(f"\nLoading data from: {input_csv}")
    texts, labels, code_to_idx, idx_to_code, code_to_content = prepare_data(input_csv, encoding, max_samples_per_class)

    num_classes = len(code_to_idx)
    print(f"   Total samples: {len(texts)}")
    print(f"   Number of classes: {num_classes}")
    print(f"   Samples per class (avg): {len(texts) / num_classes:.1f}")

    # Split data
    train_texts, test_texts, train_labels, test_labels = train_test_split(
        texts, labels, test_size=test_size, random_state=42, stratify=labels
    )

    print(f"   Train samples: {len(train_texts)}")
    print(f"   Test samples: {len(test_texts)}")

    # Save mappings
    mappings = {
        "code_to_idx": code_to_idx,
        "idx_to_code": idx_to_code,
        "code_to_content": code_to_content,
        "num_classes": num_classes,
    }

    with open(output_dir / "label_mappings.json", "w", encoding="utf-8") as f:
        json.dump(mappings, f, indent=2, ensure_ascii=False)

    # Load tokenizer and create datasets
    print(f"\nLoading model: {base_model}")
    tokenizer = AutoTokenizer.from_pretrained(base_model)

    train_dataset = AchievementDataset(train_texts, train_labels, tokenizer, max_length)
    test_dataset = AchievementDataset(test_texts, test_labels, tokenizer, max_length)

    train_loader = DataLoader(
        train_dataset,
        batch_size=batch_size,
        shuffle=True,
        num_workers=4,
        pin_memory=True,
    )

    test_loader = DataLoader(
        test_dataset,
        batch_size=batch_size * 2,
        shuffle=False,
        num_workers=4,
        pin_memory=True,
    )

    # Create model
    model = AchievementClassifier(model_name=base_model, num_classes=num_classes, dropout=dropout, pooling=pooling)
    model.to(device)

    print(f"   Device: {device}")
    if torch.cuda.is_available():
        print(f"   GPU: {torch.cuda.get_device_name(0)}")
        print(f"   GPU Memory: {torch.cuda.get_device_properties(0).total_memory / 1e9:.1f} GB")

    # Setup loss function
    print(f"\nLoss function: {loss_type}")
    if loss_type == "focal":
        criterion = FocalLoss(alpha=focal_alpha, gamma=focal_gamma)
        print(f"   Focal Loss - alpha={focal_alpha}, gamma={focal_gamma}")
    elif loss_type == "label_smoothing":
        criterion = LabelSmoothingCrossEntropy(epsilon=label_smoothing)
        print(f"   Label Smoothing - epsilon={label_smoothing}")
    else:
        criterion = nn.CrossEntropyLoss()
        print("   Standard Cross-Entropy")

    # Optimizer and scheduler
    optimizer = torch.optim.AdamW(model.parameters(), lr=lr, weight_decay=weight_decay)

    total_steps = len(train_loader) * epochs // gradient_accumulation_steps
    warmup_steps = int(total_steps * warmup_ratio)

    scheduler = get_cosine_schedule_with_warmup(
        optimizer, num_warmup_steps=warmup_steps, num_training_steps=total_steps
    )

    # Mixed precision
    scaler = torch.cuda.amp.GradScaler() if mixed_precision else None

    print("\n   Training Configuration:")
    print(f"   Epochs: {epochs}")
    print(f"   Batch size: {batch_size}")
    print(f"   Gradient accumulation: {gradient_accumulation_steps}")
    print(f"   Effective batch size: {batch_size * gradient_accumulation_steps}")
    print(f"   Learning rate: {lr}")
    print(f"   Warmup steps: {warmup_steps} / {total_steps}")
    print(f"   Weight decay: {weight_decay}")
    print(f"   Dropout: {dropout}")
    print(f"   Pooling: {pooling}")
    print(f"   Mixed precision: {mixed_precision}")

    # Training loop
    best_f1 = 0.0
    best_accuracy = 0.0
    patience_counter = 0
    training_history = []

    print("\n" + "=" * 80)
    print("STARTING TRAINING")
    print("=" * 80)

    for epoch in range(epochs):
        print(f"\n{'=' * 80}")
        print(f"Epoch {epoch + 1}/{epochs}")
        print(f"{'=' * 80}")

        # Training
        model.train()
        train_loss = 0.0
        train_preds = []
        train_labels_list = []

        optimizer.zero_grad()

        progress_bar = tqdm(train_loader, desc="Training")

        for step, batch in enumerate(progress_bar):
            input_ids = batch["input_ids"].to(device)
            attention_mask = batch["attention_mask"].to(device)
            labels = batch["labels"].to(device)

            # Forward pass with mixed precision
            if mixed_precision:
                with torch.cuda.amp.autocast():
                    outputs = model(input_ids=input_ids, attention_mask=attention_mask)
                    loss = criterion(outputs["logits"], labels)
                    loss = loss / gradient_accumulation_steps

                scaler.scale(loss).backward()

                if (step + 1) % gradient_accumulation_steps == 0:
                    scaler.unscale_(optimizer)
                    torch.nn.utils.clip_grad_norm_(model.parameters(), max_norm=1.0)
                    scaler.step(optimizer)
                    scaler.update()
                    scheduler.step()
                    optimizer.zero_grad()
            else:
                outputs = model(input_ids=input_ids, attention_mask=attention_mask)
                loss = criterion(outputs["logits"], labels)
                loss = loss / gradient_accumulation_steps
                loss.backward()

                if (step + 1) % gradient_accumulation_steps == 0:
                    torch.nn.utils.clip_grad_norm_(model.parameters(), max_norm=1.0)
                    optimizer.step()
                    scheduler.step()
                    optimizer.zero_grad()

            train_loss += loss.item() * gradient_accumulation_steps

            preds = torch.argmax(outputs["logits"], dim=-1)
            train_preds.extend(preds.cpu().numpy())
            train_labels_list.extend(labels.cpu().numpy())

            progress_bar.set_postfix({"loss": loss.item() * gradient_accumulation_steps})

        avg_train_loss = train_loss / len(train_loader)
        train_acc = accuracy_score(train_labels_list, train_preds)

        print(f"\n   Train Loss: {avg_train_loss:.4f}")
        print(f"   Train Accuracy: {train_acc:.4f}")

        # Evaluation
        print("\nEvaluating...")
        metrics = evaluate_model(model, test_loader, device, idx_to_code)

        print("\n" + "=" * 80)
        print("EVALUATION RESULTS")
        print("=" * 80)
        print("\n   Main Metrics:")
        print(f"   Accuracy:        {metrics['accuracy']:.4f}")
        print(f"   F1 (weighted):   {metrics['f1_weighted']:.4f}")
        print(f"   F1 (macro):      {metrics['f1_macro']:.4f}")
        print(f"   Precision:       {metrics['precision']:.4f}")
        print(f"   Recall:          {metrics['recall']:.4f}")

        print("\n   Top-K Accuracies:")
        for k in [1, 3, 5, 10, 20]:
            key = f"top_{k}_acc"
            if key in metrics:
                print(f"   Top-{k:2d}:  {metrics[key]:.4f}")

        print("=" * 80)

        # Save history
        epoch_history = {
            "epoch": epoch + 1,
            "train_loss": avg_train_loss,
            "train_accuracy": train_acc,
            **metrics,
        }
        training_history.append(epoch_history)

        # Save best model
        current_f1 = metrics["f1_weighted"]
        current_acc = metrics["accuracy"]

        is_best = False
        if current_f1 > best_f1 or (current_f1 == best_f1 and current_acc > best_accuracy):
            is_best = True
            best_f1 = current_f1
            best_accuracy = current_acc

        if is_best:
            patience_counter = 0
            best_model_path = output_dir / "best_model"
            best_model_path.mkdir(exist_ok=True)

            torch.save(model.state_dict(), best_model_path / "model.pt")
            tokenizer.save_pretrained(best_model_path)

            print("\nNEW BEST MODEL!")
            print(f"   F1: {best_f1:.4f} | Accuracy: {best_accuracy:.4f}")
            print(f"   Saved to: {best_model_path}")
        else:
            patience_counter += 1
            print(f"\nNo improvement. Patience: {patience_counter}/{early_stopping_patience}")

        # Save checkpoint
        checkpoint_path = output_dir / f"checkpoint_epoch_{epoch + 1}"
        checkpoint_path.mkdir(exist_ok=True)
        torch.save(model.state_dict(), checkpoint_path / "model.pt")

        # Early stopping
        if patience_counter >= early_stopping_patience:
            print(f"\n{'=' * 80}")
            print(f"Early stopping triggered at epoch {epoch + 1}")
            print(f"   Best F1: {best_f1:.4f}")
            print(f"   Best Accuracy: {best_accuracy:.4f}")
            print(f"{'=' * 80}")
            break

    # Save training history
    history_path = output_dir / "training_history.json"
    with open(history_path, "w", encoding="utf-8") as f:
        json.dump(training_history, f, indent=2, ensure_ascii=False)

    # Save config
    config = {
        "base_model": base_model,
        "num_classes": num_classes,
        "max_length": max_length,
        "dropout": dropout,
        "pooling": pooling,
        "loss_type": loss_type,
        "best_f1": best_f1,
        "best_accuracy": best_accuracy,
    }

    config_path = output_dir / "config.json"
    with open(config_path, "w", encoding="utf-8") as f:
        json.dump(config, f, indent=2, ensure_ascii=False)

    print("\n" + "=" * 80)
    print("TRAINING COMPLETE!")
    print("=" * 80)
    print(f"   Best F1 Score: {best_f1:.4f}")
    print(f"   Best Accuracy: {best_accuracy:.4f}")
    print(f"   Model saved to: {output_dir / 'best_model'}")
    print(f"   History saved to: {history_path}")
    print("=" * 80)

    return model, training_history


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Train Multi-Class Classifier for Achievement Standard Prediction")

    # Data arguments
    parser.add_argument("--input_csv", type=str, required=True, help="Input CSV file")
    parser.add_argument("--encoding", type=str, default=None, help="CSV encoding")
    parser.add_argument("--test_size", type=float, default=0.2, help="Test split ratio")
    parser.add_argument("--max_samples_per_class", type=int, default=None)
    parser.add_argument("--max_length", type=int, default=256, help="Max sequence length")

    # Model arguments
    parser.add_argument("--base_model", type=str, default="klue/roberta-large")
    parser.add_argument("--output_dir", type=str, default="model/")
    parser.add_argument("--dropout", type=float, default=0.1)
    parser.add_argument("--pooling", type=str, default="cls", choices=["cls", "mean"])

    # Training arguments
    parser.add_argument("--batch_size", type=int, default=32)
    parser.add_argument("--gradient_accumulation_steps", type=int, default=1)
    parser.add_argument("--epochs", type=int, default=10)
    parser.add_argument("--lr", type=float, default=2e-5)
    parser.add_argument("--warmup_ratio", type=float, default=0.1)
    parser.add_argument("--weight_decay", type=float, default=0.01)
    parser.add_argument("--early_stopping_patience", type=int, default=3)
    parser.add_argument("--mixed_precision", action="store_true", default=True)
    parser.add_argument("--no_mixed_precision", action="store_true")

    # Loss arguments
    parser.add_argument(
        "--loss_type",
        type=str,
        default="ce",
        choices=["ce", "focal", "label_smoothing"],
    )
    parser.add_argument("--label_smoothing", type=float, default=0.1)
    parser.add_argument("--focal_alpha", type=float, default=1.0)
    parser.add_argument("--focal_gamma", type=float, default=2.0)

    args = parser.parse_args()

    if args.no_mixed_precision:
        args.mixed_precision = False

    # Set seed
    set_train_random_seed(42)

    # Train
    train_classifier(
        input_csv=args.input_csv,
        base_model=args.base_model,
        output_dir=args.output_dir,
        encoding=args.encoding,
        test_size=args.test_size,
        max_samples_per_class=args.max_samples_per_class,
        max_length=args.max_length,
        batch_size=args.batch_size,
        epochs=args.epochs,
        lr=args.lr,
        warmup_ratio=args.warmup_ratio,
        weight_decay=args.weight_decay,
        dropout=args.dropout,
        pooling=args.pooling,
        loss_type=args.loss_type,
        label_smoothing=args.label_smoothing,
        focal_alpha=args.focal_alpha,
        focal_gamma=args.focal_gamma,
        early_stopping_patience=args.early_stopping_patience,
        mixed_precision=args.mixed_precision,
        gradient_accumulation_steps=args.gradient_accumulation_steps,
    )
