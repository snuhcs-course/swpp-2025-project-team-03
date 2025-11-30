"""
Achievement Standard Inference Module

Uses a trained multi-class classifier to predict top-k achievement standards
for a given question text. This is used to filter down candidates before
sending to GPT for final selection.
"""

import json
import logging
import threading
from pathlib import Path
from typing import Dict, List, Optional, Tuple

import torch
import torch.nn as nn
import torch.nn.functional as F
from transformers import AutoConfig, AutoModel, AutoTokenizer

logger = logging.getLogger(__name__)

# Global cache for model and tokenizer to avoid reloading
_model_cache: Dict[str, any] = {}
_model_lock = threading.Lock()  # Thread-safe lock for model loading


class AchievementClassifier(nn.Module):
    """
    Multi-class classifier for achievement standards.
    Same architecture as the training script.
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

        if hidden_size:
            self.intermediate = nn.Sequential(nn.Linear(hidden_dim, hidden_size), nn.Tanh(), nn.Dropout(dropout))
            hidden_dim = hidden_size
        else:
            self.intermediate = None

        self.dropout = nn.Dropout(dropout)
        self.classifier = nn.Linear(hidden_dim, num_classes)

    def forward(self, input_ids, attention_mask, labels=None):
        outputs = self.encoder(input_ids=input_ids, attention_mask=attention_mask)

        if self.pooling == "cls":
            pooled = outputs.last_hidden_state[:, 0]
        elif self.pooling == "mean":
            token_embeddings = outputs.last_hidden_state
            attention_mask_expanded = attention_mask.unsqueeze(-1).expand(token_embeddings.size()).float()
            sum_embeddings = torch.sum(token_embeddings * attention_mask_expanded, 1)
            sum_mask = torch.clamp(attention_mask_expanded.sum(1), min=1e-9)
            pooled = sum_embeddings / sum_mask
        else:
            pooled = outputs.pooler_output

        if self.intermediate:
            pooled = self.intermediate(pooled)

        pooled = self.dropout(pooled)
        logits = self.classifier(pooled)

        loss = None
        if labels is not None:
            loss_fct = nn.CrossEntropyLoss()
            loss = loss_fct(logits, labels)

        return {"loss": loss, "logits": logits}


def get_model_dir() -> Path:
    """
    Get the path to the trained model directory.
    Default location: backend/reports/utils/models/
    """
    # Default: same directory as this file + models/
    # backend/reports/utils/achievement_inference.py -> backend/reports/utils/models/
    current_dir = Path(__file__).resolve().parent
    default_model_dir = current_dir / "models"

    return default_model_dir


def load_model(model_dir: Path, device: torch.device) -> Tuple[nn.Module, any, dict, dict]:
    """
    Load the trained model, tokenizer, config, and mappings.
    Uses caching to avoid reloading on every call.
    Thread-safe with lock.
    """
    cache_key = str(model_dir)

    # Check cache first (without lock for performance)
    if cache_key in _model_cache:
        cached = _model_cache[cache_key]
        return cached["model"], cached["tokenizer"], cached["config"], cached["mappings"]

    # Acquire lock for thread-safe loading
    with _model_lock:
        # Double-check after acquiring lock (another thread might have loaded it)
        if cache_key in _model_cache:
            cached = _model_cache[cache_key]
            return cached["model"], cached["tokenizer"], cached["config"], cached["mappings"]

        model_dir = Path(model_dir)

        # Try to load config
        config_path = model_dir / "config.json"
        if not config_path.exists():
            config_path = model_dir.parent / "config.json"

        if config_path.exists():
            with open(config_path, "r", encoding="utf-8") as f:
                config = json.load(f)
        else:
            # Use default values silently (no warning spam)
            config = {
                "base_model": "klue/roberta-large",
                "max_length": 256,
                "dropout": 0.1,
                "pooling": "cls",
            }

        # Load mappings
        mappings_path = model_dir / "label_mappings.json"
        if not mappings_path.exists():
            mappings_path = model_dir.parent / "label_mappings.json"

        if not mappings_path.exists():
            raise FileNotFoundError(f"label_mappings.json not found in {model_dir} or {model_dir.parent}")

        with open(mappings_path, "r", encoding="utf-8") as f:
            mappings = json.load(f)

        if "num_classes" not in config:
            config["num_classes"] = mappings.get("num_classes", len(mappings.get("code_to_idx", {})))

        # Load tokenizer
        tokenizer = AutoTokenizer.from_pretrained(model_dir)

        # Infer base_model if not in config
        if "base_model" not in config or not config["base_model"]:
            tokenizer_config_path = model_dir / "tokenizer_config.json"
            if tokenizer_config_path.exists():
                with open(tokenizer_config_path, "r", encoding="utf-8") as f:
                    tokenizer_config = json.load(f)
                    if "model_type" in tokenizer_config:
                        model_type = tokenizer_config["model_type"]
                        if model_type == "roberta":
                            config["base_model"] = "klue/roberta-large"
                        elif model_type == "bert":
                            config["base_model"] = "klue/bert-base"
                        else:
                            config["base_model"] = f"klue/{model_type}-base"
                    else:
                        config["base_model"] = "klue/roberta-large"
            else:
                config["base_model"] = "klue/roberta-large"

        # Load model
        model = AchievementClassifier(
            model_name=config["base_model"],
            num_classes=config["num_classes"],
            dropout=config.get("dropout", 0.1),
            pooling=config.get("pooling", "cls"),
        )

        model_pt_path = model_dir / "model.pt"
        # Use weights_only=False for compatibility with different PyTorch versions
        state_dict = torch.load(model_pt_path, map_location=device, weights_only=False)
        model.load_state_dict(state_dict)
        model.to(device)
        model.eval()

        # Cache the loaded model
        _model_cache[cache_key] = {
            "model": model,
            "tokenizer": tokenizer,
            "config": config,
            "mappings": mappings,
        }

        logger.info(f"Loaded achievement model from {model_dir} with {config['num_classes']} classes")

        return model, tokenizer, config, mappings


def predict_top_k(
    text: str,
    top_k: int = 30,
    model_dir: Optional[Path] = None,
    device: str = "cuda",
    filter_codes: Optional[set] = None,
) -> List[Dict]:
    """
    Predict top-k achievement standards for a given text.

    Args:
        text: Input question text
        top_k: Number of top predictions to return (default: 30)
        model_dir: Path to model directory (optional, uses default if not provided)
        device: Device to use ("cuda" or "cpu")
        filter_codes: Optional set of achievement codes to filter by.
                      If provided, only returns top-k from these codes.

    Returns:
        List of dictionaries with 'code', 'content', and 'probability' keys,
        sorted by probability (highest first)
    """
    if model_dir is None:
        model_dir = get_model_dir()

    # Determine device
    if device == "cuda" and not torch.cuda.is_available():
        device = "cpu"
    device = torch.device(device)

    try:
        model, tokenizer, config, mappings = load_model(model_dir, device)
    except FileNotFoundError as e:
        logger.error(f"Model files not found: {e}")
        return []
    except Exception as e:
        logger.error(f"Error loading model: {e}")
        return []

    # Get mappings
    idx_to_code = {int(k): v for k, v in mappings["idx_to_code"].items()}
    code_to_content = mappings.get("code_to_content", {})

    # Tokenize
    encodings = tokenizer(
        text,
        max_length=config.get("max_length", 256),
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

    # If filter_codes is provided, filter by those codes only
    if filter_codes is not None:
        # Get all predictions, filter by codes, then take top-k
        all_probs = probs[0].cpu().numpy()

        # Build list of (code, content, probability) for filtered codes only
        filtered_results = []
        for idx, prob in enumerate(all_probs):
            code = idx_to_code.get(idx, f"unknown_{idx}")
            if code in filter_codes:
                content = code_to_content.get(code, "")
                filtered_results.append(
                    {
                        "code": code,
                        "content": content,
                        "probability": float(prob),
                    }
                )

        # Sort by probability (highest first) and take top-k
        filtered_results.sort(key=lambda x: x["probability"], reverse=True)
        return filtered_results[:top_k]

    # Original behavior: get top-k from all classes
    num_classes = probs.size(1)
    effective_top_k = min(top_k, num_classes)

    topk_probs, topk_indices = torch.topk(probs[0], k=effective_top_k)

    # Build results
    results = []
    for prob, idx in zip(topk_probs.cpu().numpy(), topk_indices.cpu().numpy()):
        code = idx_to_code.get(int(idx), f"unknown_{idx}")
        content = code_to_content.get(code, "")
        results.append(
            {
                "code": code,
                "content": content,
                "probability": float(prob),
            }
        )

    return results


def filter_standards_by_model(
    question_content: str,
    achievement_standards: List[Dict],
    top_k: int = 20,
    min_results: int = 3,
) -> List[Dict]:
    """
    Use the trained model to filter achievement standards down to top-k candidates.

    This function:
    1. Extracts codes from the provided achievement_standards (subject/school filtered)
    2. Gets top-k predictions from the model, only considering those codes
    3. Returns the filtered standards sorted by model probability

    Args:
        question_content: The question text to classify
        achievement_standards: List of achievement standard dicts from CSV
                               (already filtered by subject/school)
        top_k: Number of top predictions to return (default: 20)
        min_results: Minimum number of filtered results required (default: 3).
                     If fewer results, falls back to all standards.

    Returns:
        Filtered list of achievement standards (subset of input), sorted by model probability
    """
    if not achievement_standards:
        logger.warning("No achievement standards provided")
        return []

    # Extract codes from the provided standards (already filtered by subject/school)
    available_codes = {std["code"] for std in achievement_standards}

    # Get model predictions filtered by available codes
    # This ensures we only consider standards for the current subject/school
    predictions = predict_top_k(
        question_content,
        top_k=top_k,
        filter_codes=available_codes,
    )

    if not predictions:
        logger.warning("Model prediction failed, using all standards as fallback")
        return achievement_standards

    # Get predicted codes (already filtered and sorted by probability)
    predicted_codes = [pred["code"] for pred in predictions]

    # Build filtered standards in the order of model probability
    code_to_standard = {std["code"]: std for std in achievement_standards}
    filtered_standards = [code_to_standard[code] for code in predicted_codes if code in code_to_standard]

    # Check if we have enough results
    if len(filtered_standards) < min_results:
        logger.warning(
            f"Filtered results ({len(filtered_standards)}) below minimum ({min_results}). "
            f"Using all standards as fallback."
        )
        return achievement_standards

    logger.info(
        f"Filtered {len(achievement_standards)} standards down to {len(filtered_standards)} "
        f"using model predictions (top-{top_k} from subject/school filtered codes)"
    )

    return filtered_standards


def get_top_k_with_content(
    question_content: str,
    top_k: int = 30,
) -> List[Dict]:
    """
    Get top-k achievement standards with their content from the model.

    This is useful when you don't have a pre-filtered list and want to
    directly use model predictions.

    Args:
        question_content: The question text to classify
        top_k: Number of top predictions to return

    Returns:
        List of dicts with 'code', 'content', 'probability' keys
    """
    return predict_top_k(question_content, top_k=top_k)
