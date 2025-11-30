"""
Test coverage for achievement_inference.py

This test file covers the uncovered lines in achievement_inference.py
"""

import json
import tempfile
from pathlib import Path
from unittest.mock import Mock, patch

import pytest
import torch
import torch.nn as nn
from reports.utils.achievement_inference import (
    AchievementClassifier,
    filter_standards_by_model,
    get_model_dir,
    get_top_k_with_content,
    load_model,
    predict_top_k,
)

pytestmark = pytest.mark.django_db


class TestAchievementClassifier:
    """Test AchievementClassifier model architecture"""

    def test_classifier_with_hidden_size(self):
        """Test classifier with hidden_size parameter"""
        model_name = "klue/roberta-base"
        num_classes = 10
        hidden_size = 512

        classifier = AchievementClassifier(model_name=model_name, num_classes=num_classes, hidden_size=hidden_size)

        assert classifier.intermediate is not None
        assert isinstance(classifier.intermediate, nn.Sequential)
        assert classifier.classifier.in_features == hidden_size

    def test_classifier_forward_cls_pooling(self):
        """Test forward method with CLS pooling (default)"""
        classifier = AchievementClassifier(model_name="klue/roberta-base", num_classes=10)
        batch_size = 2
        seq_len = 10
        hidden_size = 768

        input_ids = torch.randint(0, 1000, (batch_size, seq_len))
        attention_mask = torch.ones(batch_size, seq_len)

        # Mock encoder output
        mock_outputs = Mock()
        mock_outputs.last_hidden_state = torch.randn(batch_size, seq_len, hidden_size)
        mock_outputs.pooler_output = torch.randn(batch_size, hidden_size)

        with patch.object(classifier.encoder, "__call__", return_value=mock_outputs):
            result = classifier.forward(input_ids, attention_mask)

        assert "logits" in result
        assert result["logits"].shape == (batch_size, 10)
        assert result["loss"] is None

    def test_classifier_forward_mean_pooling(self):
        """Test forward method with mean pooling"""
        classifier = AchievementClassifier(model_name="klue/roberta-base", num_classes=10, pooling="mean")
        batch_size = 2
        seq_len = 10
        hidden_size = 768

        input_ids = torch.randint(0, 1000, (batch_size, seq_len))
        attention_mask = torch.ones(batch_size, seq_len)

        mock_outputs = Mock()
        mock_outputs.last_hidden_state = torch.randn(batch_size, seq_len, hidden_size)
        mock_outputs.pooler_output = torch.randn(batch_size, hidden_size)

        with patch.object(classifier.encoder, "__call__", return_value=mock_outputs):
            result = classifier.forward(input_ids, attention_mask)

        assert "logits" in result
        assert result["logits"].shape == (batch_size, 10)

    def test_classifier_forward_pooler_output(self):
        """Test forward method with pooler_output"""
        classifier = AchievementClassifier(model_name="klue/roberta-base", num_classes=10, pooling="pooler")
        batch_size = 2
        seq_len = 10
        hidden_size = 768

        input_ids = torch.randint(0, 1000, (batch_size, seq_len))
        attention_mask = torch.ones(batch_size, seq_len)

        mock_outputs = Mock()
        mock_outputs.last_hidden_state = torch.randn(batch_size, seq_len, hidden_size)
        mock_outputs.pooler_output = torch.randn(batch_size, hidden_size)

        with patch.object(classifier.encoder, "__call__", return_value=mock_outputs):
            result = classifier.forward(input_ids, attention_mask)

        assert "logits" in result
        assert result["logits"].shape == (batch_size, 10)

    def test_classifier_forward_with_intermediate(self):
        """Test forward method with intermediate layer"""
        classifier = AchievementClassifier(model_name="klue/roberta-base", num_classes=10, hidden_size=512)
        batch_size = 2
        seq_len = 10

        input_ids = torch.randint(0, 1000, (batch_size, seq_len))
        attention_mask = torch.ones(batch_size, seq_len)

        mock_outputs = Mock()
        mock_outputs.last_hidden_state = torch.randn(batch_size, seq_len, 768)
        mock_outputs.pooler_output = torch.randn(batch_size, 768)

        with patch.object(classifier.encoder, "__call__", return_value=mock_outputs):
            result = classifier.forward(input_ids, attention_mask)

        assert "logits" in result
        assert result["logits"].shape == (batch_size, 10)

    def test_classifier_forward_with_labels(self):
        """Test forward method with labels for loss calculation"""
        classifier = AchievementClassifier(model_name="klue/roberta-base", num_classes=10)
        batch_size = 2
        seq_len = 10

        input_ids = torch.randint(0, 1000, (batch_size, seq_len))
        attention_mask = torch.ones(batch_size, seq_len)
        labels = torch.randint(0, 10, (batch_size,))

        mock_outputs = Mock()
        mock_outputs.last_hidden_state = torch.randn(batch_size, seq_len, 768)
        mock_outputs.pooler_output = torch.randn(batch_size, 768)

        with patch.object(classifier.encoder, "__call__", return_value=mock_outputs):
            result = classifier.forward(input_ids, attention_mask, labels=labels)

        assert "logits" in result
        assert "loss" in result
        assert result["loss"] is not None
        assert result["loss"].item() > 0


class TestModelLoading:
    """Test model loading and caching logic"""

    def test_get_model_dir(self):
        """Test get_model_dir returns correct path"""
        model_dir = get_model_dir()
        assert isinstance(model_dir, Path)
        assert model_dir.name == "models"

    @patch("reports.utils.achievement_inference._model_cache", {})
    @patch("reports.utils.achievement_inference._model_lock")
    @patch("reports.utils.achievement_inference.AutoTokenizer.from_pretrained")
    @patch("reports.utils.achievement_inference.AutoModel.from_pretrained")
    @patch("reports.utils.achievement_inference.AutoConfig.from_pretrained")
    @patch("reports.utils.achievement_inference.torch.load")
    @patch("reports.utils.achievement_inference.logger")
    def test_load_model_caching(self, mock_logger, mock_torch_load, mock_config, mock_model, mock_tokenizer, mock_lock):
        """Test model caching"""
        with tempfile.TemporaryDirectory() as tmpdir:
            model_dir = Path(tmpdir)
            device = torch.device("cpu")

            # Create mock files
            (model_dir / "label_mappings.json").write_text(
                json.dumps(
                    {
                        "idx_to_code": {"0": "CODE1", "1": "CODE2"},
                        "code_to_content": {"CODE1": "Content1", "CODE2": "Content2"},
                        "num_classes": 2,
                    }
                )
            )
            (model_dir / "model.pt").touch()

            # Mock transformers
            mock_config_obj = Mock()
            mock_config_obj.hidden_size = 768
            mock_config.from_pretrained.return_value = mock_config_obj

            mock_tokenizer_obj = Mock()
            mock_tokenizer.from_pretrained.return_value = mock_tokenizer_obj

            mock_encoder = Mock()
            mock_model.from_pretrained.return_value = mock_encoder

            # Create actual model to get proper state_dict structure
            actual_model = AchievementClassifier(
                model_name="klue/roberta-base", num_classes=2, dropout=0.1, pooling="cls"
            )
            mock_state_dict = actual_model.state_dict()
            mock_torch_load.return_value = mock_state_dict

            # First load
            model1, tokenizer1, config1, mappings1 = load_model(model_dir, device)

            # Second load should use cache
            model2, tokenizer2, config2, mappings2 = load_model(model_dir, device)

            # Should return cached model
            assert model1 is model2
            assert tokenizer1 is tokenizer2

    @patch("reports.utils.achievement_inference._model_cache", {})
    @patch("reports.utils.achievement_inference._model_lock")
    @patch("reports.utils.achievement_inference.AutoTokenizer.from_pretrained")
    @patch("reports.utils.achievement_inference.AutoModel.from_pretrained")
    @patch("reports.utils.achievement_inference.AutoConfig.from_pretrained")
    @patch("reports.utils.achievement_inference.torch.load")
    def test_load_model_config_in_parent_dir(self, mock_torch_load, mock_config, mock_model, mock_tokenizer, mock_lock):
        """Test config.json loading from parent directory"""
        with tempfile.TemporaryDirectory() as tmpdir:
            model_dir = Path(tmpdir) / "models"
            model_dir.mkdir()
            parent_dir = model_dir.parent
            device = torch.device("cpu")

            # Config in parent, mappings in model_dir
            (parent_dir / "config.json").write_text(
                json.dumps({"base_model": "klue/roberta-large", "max_length": 256, "num_classes": 2})
            )
            (model_dir / "label_mappings.json").write_text(
                json.dumps(
                    {
                        "idx_to_code": {"0": "CODE1"},
                        "code_to_content": {"CODE1": "Content1"},
                    }
                )
            )
            (model_dir / "model.pt").touch()

            mock_config_obj = Mock()
            mock_config_obj.hidden_size = 768
            mock_config.from_pretrained.return_value = mock_config_obj

            mock_tokenizer_obj = Mock()
            mock_tokenizer.from_pretrained.return_value = mock_tokenizer_obj

            mock_encoder = Mock()
            mock_model.from_pretrained.return_value = mock_encoder

            # Create actual model to get proper state_dict structure
            actual_model = AchievementClassifier(
                model_name="klue/roberta-base", num_classes=2, dropout=0.1, pooling="cls"
            )
            mock_state_dict = actual_model.state_dict()
            mock_torch_load.return_value = mock_state_dict

            model, tokenizer, config, mappings = load_model(model_dir, device)

            assert config["base_model"] == "klue/roberta-large"

    @patch("reports.utils.achievement_inference._model_cache", {})
    @patch("reports.utils.achievement_inference._model_lock")
    @patch("reports.utils.achievement_inference.AutoTokenizer.from_pretrained")
    @patch("reports.utils.achievement_inference.AutoModel.from_pretrained")
    @patch("reports.utils.achievement_inference.AutoConfig.from_pretrained")
    @patch("reports.utils.achievement_inference.torch.load")
    def test_load_model_default_config(self, mock_torch_load, mock_config, mock_model, mock_tokenizer, mock_lock):
        """Test default config when config.json doesn't exist"""
        with tempfile.TemporaryDirectory() as tmpdir:
            model_dir = Path(tmpdir)
            device = torch.device("cpu")

            (model_dir / "label_mappings.json").write_text(
                json.dumps(
                    {
                        "idx_to_code": {"0": "CODE1"},
                        "code_to_content": {"CODE1": "Content1"},
                        "num_classes": 1,
                    }
                )
            )
            (model_dir / "model.pt").touch()

            mock_config_obj = Mock()
            mock_config_obj.hidden_size = 768
            mock_config.from_pretrained.return_value = mock_config_obj

            mock_tokenizer_obj = Mock()
            mock_tokenizer.from_pretrained.return_value = mock_tokenizer_obj

            mock_encoder = Mock()
            mock_model.from_pretrained.return_value = mock_encoder

            # Create actual model to get proper state_dict structure
            actual_model = AchievementClassifier(
                model_name="klue/roberta-base", num_classes=1, dropout=0.1, pooling="cls"
            )
            mock_state_dict = actual_model.state_dict()
            mock_torch_load.return_value = mock_state_dict

            model, tokenizer, config, mappings = load_model(model_dir, device)

            assert config["base_model"] == "klue/roberta-large"
            assert config["max_length"] == 256
            assert config["dropout"] == 0.1
            assert config["pooling"] == "cls"

    @patch("reports.utils.achievement_inference._model_cache", {})
    @patch("reports.utils.achievement_inference._model_lock")
    @patch("reports.utils.achievement_inference.AutoTokenizer.from_pretrained")
    @patch("reports.utils.achievement_inference.AutoModel.from_pretrained")
    @patch("reports.utils.achievement_inference.AutoConfig.from_pretrained")
    @patch("reports.utils.achievement_inference.torch.load")
    def test_load_model_mappings_in_parent_dir(
        self, mock_torch_load, mock_config, mock_model, mock_tokenizer, mock_lock
    ):
        """Test label_mappings.json loading from parent directory"""
        with tempfile.TemporaryDirectory() as tmpdir:
            model_dir = Path(tmpdir) / "models"
            model_dir.mkdir()
            parent_dir = model_dir.parent
            device = torch.device("cpu")

            (parent_dir / "label_mappings.json").write_text(
                json.dumps(
                    {
                        "idx_to_code": {"0": "CODE1"},
                        "code_to_content": {"CODE1": "Content1"},
                        "num_classes": 1,
                    }
                )
            )
            (model_dir / "model.pt").touch()

            mock_config_obj = Mock()
            mock_config_obj.hidden_size = 768
            mock_config.from_pretrained.return_value = mock_config_obj

            mock_tokenizer_obj = Mock()
            mock_tokenizer.from_pretrained.return_value = mock_tokenizer_obj

            mock_encoder = Mock()
            mock_model.from_pretrained.return_value = mock_encoder

            # Create actual model to get proper state_dict structure
            actual_model = AchievementClassifier(
                model_name="klue/roberta-base", num_classes=1, dropout=0.1, pooling="cls"
            )
            mock_state_dict = actual_model.state_dict()
            mock_torch_load.return_value = mock_state_dict

            model, tokenizer, config, mappings = load_model(model_dir, device)

            assert "CODE1" in mappings["idx_to_code"]["0"]

    @patch("reports.utils.achievement_inference._model_cache", {})
    @patch("reports.utils.achievement_inference._model_lock")
    def test_load_model_missing_mappings(self, mock_lock):
        """Test FileNotFoundError when mappings file doesn't exist"""
        with tempfile.TemporaryDirectory() as tmpdir:
            model_dir = Path(tmpdir)
            device = torch.device("cpu")

            # No label_mappings.json file

            with pytest.raises(FileNotFoundError, match="label_mappings.json not found"):
                load_model(model_dir, device)

    @patch("reports.utils.achievement_inference._model_cache", {})
    @patch("reports.utils.achievement_inference._model_lock")
    @patch("reports.utils.achievement_inference.AutoTokenizer.from_pretrained")
    @patch("reports.utils.achievement_inference.AutoModel.from_pretrained")
    @patch("reports.utils.achievement_inference.AutoConfig.from_pretrained")
    @patch("reports.utils.achievement_inference.torch.load")
    def test_load_model_infer_base_model_from_tokenizer_config(
        self, mock_torch_load, mock_config, mock_model, mock_tokenizer, mock_lock
    ):
        """Test base_model inference from tokenizer_config.json"""
        with tempfile.TemporaryDirectory() as tmpdir:
            model_dir = Path(tmpdir)
            device = torch.device("cpu")

            (model_dir / "label_mappings.json").write_text(
                json.dumps(
                    {
                        "idx_to_code": {"0": "CODE1"},
                        "code_to_content": {"CODE1": "Content1"},
                        "num_classes": 1,
                    }
                )
            )
            (model_dir / "model.pt").touch()
            (model_dir / "tokenizer_config.json").write_text(json.dumps({"model_type": "roberta"}))

            mock_config_obj = Mock()
            mock_config_obj.hidden_size = 768
            mock_config.from_pretrained.return_value = mock_config_obj

            mock_tokenizer_obj = Mock()
            mock_tokenizer.from_pretrained.return_value = mock_tokenizer_obj

            mock_encoder = Mock()
            mock_model.from_pretrained.return_value = mock_encoder

            # Create actual model to get proper state_dict structure
            actual_model = AchievementClassifier(
                model_name="klue/roberta-base", num_classes=1, dropout=0.1, pooling="cls"
            )
            mock_state_dict = actual_model.state_dict()
            mock_torch_load.return_value = mock_state_dict

            model, tokenizer, config, mappings = load_model(model_dir, device)

            assert config["base_model"] == "klue/roberta-large"

    @patch("reports.utils.achievement_inference._model_cache", {})
    @patch("reports.utils.achievement_inference._model_lock")
    @patch("reports.utils.achievement_inference.AutoTokenizer.from_pretrained")
    @patch("reports.utils.achievement_inference.AutoModel.from_pretrained")
    @patch("reports.utils.achievement_inference.AutoConfig.from_pretrained")
    @patch("reports.utils.achievement_inference.torch.load")
    def test_load_model_infer_base_model_bert_type(
        self, mock_torch_load, mock_config, mock_model, mock_tokenizer, mock_lock
    ):
        """Test base_model inference for bert type"""
        with tempfile.TemporaryDirectory() as tmpdir:
            model_dir = Path(tmpdir)
            device = torch.device("cpu")

            (model_dir / "label_mappings.json").write_text(
                json.dumps(
                    {
                        "idx_to_code": {"0": "CODE1"},
                        "code_to_content": {"CODE1": "Content1"},
                        "num_classes": 1,
                    }
                )
            )
            (model_dir / "model.pt").touch()
            (model_dir / "tokenizer_config.json").write_text(json.dumps({"model_type": "bert"}))

            mock_config_obj = Mock()
            mock_config_obj.hidden_size = 768
            mock_config.from_pretrained.return_value = mock_config_obj

            mock_tokenizer_obj = Mock()
            mock_tokenizer.from_pretrained.return_value = mock_tokenizer_obj

            mock_encoder = Mock()
            mock_model.from_pretrained.return_value = mock_encoder

            # Create actual model to get proper state_dict structure
            actual_model = AchievementClassifier(
                model_name="klue/roberta-base", num_classes=1, dropout=0.1, pooling="cls"
            )
            mock_state_dict = actual_model.state_dict()
            mock_torch_load.return_value = mock_state_dict

            model, tokenizer, config, mappings = load_model(model_dir, device)

            assert config["base_model"] == "klue/roberta-large"

    @patch("reports.utils.achievement_inference._model_cache", {})
    @patch("reports.utils.achievement_inference._model_lock")
    @patch("reports.utils.achievement_inference.AutoTokenizer.from_pretrained")
    @patch("reports.utils.achievement_inference.AutoModel.from_pretrained")
    @patch("reports.utils.achievement_inference.AutoConfig.from_pretrained")
    @patch("reports.utils.achievement_inference.torch.load")
    @patch("reports.utils.achievement_inference.logger")
    def test_load_model_caching_and_logging(
        self, mock_logger, mock_torch_load, mock_config, mock_model, mock_tokenizer, mock_lock
    ):
        """Test model loading, caching, and logging"""
        with tempfile.TemporaryDirectory() as tmpdir:
            model_dir = Path(tmpdir)
            device = torch.device("cpu")

            (model_dir / "label_mappings.json").write_text(
                json.dumps(
                    {
                        "idx_to_code": {"0": "CODE1"},
                        "code_to_content": {"CODE1": "Content1"},
                        "num_classes": 1,
                    }
                )
            )
            (model_dir / "model.pt").touch()

            mock_config_obj = Mock()
            mock_config_obj.hidden_size = 768
            mock_config.from_pretrained.return_value = mock_config_obj

            mock_tokenizer_obj = Mock()
            mock_tokenizer.from_pretrained.return_value = mock_tokenizer_obj

            mock_encoder = Mock()
            mock_model.from_pretrained.return_value = mock_encoder

            # Create actual model to get proper state_dict structure
            actual_model = AchievementClassifier(
                model_name="klue/roberta-base", num_classes=1, dropout=0.1, pooling="cls"
            )
            mock_state_dict = actual_model.state_dict()
            mock_torch_load.return_value = mock_state_dict

            model, tokenizer, config, mappings = load_model(model_dir, device)

            assert model is not None
            assert tokenizer is not None
            assert config is not None
            assert mappings is not None

            # Check logging was called
            mock_logger.info.assert_called_once()


class TestPredictTopK:
    """Test predict_top_k function"""

    @patch("reports.utils.achievement_inference.load_model")
    @patch("reports.utils.achievement_inference.logger")
    def test_predict_top_k_file_not_found(self, mock_logger, mock_load_model):
        """Test predict_top_k with FileNotFoundError"""
        mock_load_model.side_effect = FileNotFoundError("Model not found")

        result = predict_top_k("test text", top_k=10)

        assert result == []
        mock_logger.error.assert_called_once()

    @patch("reports.utils.achievement_inference.load_model")
    @patch("reports.utils.achievement_inference.logger")
    def test_predict_top_k_general_exception(self, mock_logger, mock_load_model):
        """Test predict_top_k with general exception"""
        mock_load_model.side_effect = Exception("Unexpected error")

        result = predict_top_k("test text", top_k=10)

        assert result == []
        mock_logger.error.assert_called_once()

    @patch("reports.utils.achievement_inference.load_model")
    def test_predict_top_k_with_filter_codes(self, mock_load_model):
        """Test predict_top_k with filter_codes parameter"""
        # Mock model, tokenizer, config, mappings
        mock_model = Mock()
        mock_tokenizer = Mock()
        mock_config = {"max_length": 256}
        mock_mappings = {
            "idx_to_code": {"0": "CODE1", "1": "CODE2", "2": "CODE3"},
            "code_to_content": {"CODE1": "Content1", "CODE2": "Content2", "CODE3": "Content3"},
        }

        mock_load_model.return_value = (mock_model, mock_tokenizer, mock_config, mock_mappings)

        # Mock tokenizer output - tokenizer is callable
        input_ids = torch.randint(0, 1000, (1, 10))
        attention_mask = torch.ones(1, 10)
        mock_tokenizer.return_value = {
            "input_ids": input_ids,
            "attention_mask": attention_mask,
        }

        # Mock model forward output - model returns dict with "logits" key
        # CODE1: logit=0.1, CODE2: logit=0.6, CODE3: logit=0.3
        logits = torch.tensor([[0.1, 0.6, 0.3]])
        mock_outputs = {"logits": logits}
        mock_model.return_value = mock_outputs

        filter_codes = {"CODE1", "CODE3"}
        result = predict_top_k("test text", top_k=10, filter_codes=filter_codes)

        assert len(result) <= 2  # Only CODE1 and CODE3
        assert all(r["code"] in filter_codes for r in result)

    @patch("reports.utils.achievement_inference.load_model")
    def test_predict_top_k_without_filter_codes(self, mock_load_model):
        """Test predict_top_k without filter_codes"""
        # Mock model, tokenizer, config, mappings
        mock_model = Mock()
        mock_tokenizer = Mock()
        mock_config = {"max_length": 256}
        mock_mappings = {
            "idx_to_code": {"0": "CODE1", "1": "CODE2", "2": "CODE3"},
            "code_to_content": {"CODE1": "Content1", "CODE2": "Content2", "CODE3": "Content3"},
        }

        mock_load_model.return_value = (mock_model, mock_tokenizer, mock_config, mock_mappings)

        # Mock tokenizer output - tokenizer is callable
        input_ids = torch.randint(0, 1000, (1, 10))
        attention_mask = torch.ones(1, 10)
        mock_tokenizer.return_value = {
            "input_ids": input_ids,
            "attention_mask": attention_mask,
        }

        # Mock model forward output - model returns dict with "logits" key
        # CODE1: logit=0.6, CODE2: logit=0.3, CODE3: logit=0.1
        logits = torch.tensor([[0.6, 0.3, 0.1]])
        mock_outputs = {"logits": logits}
        mock_model.return_value = mock_outputs

        result = predict_top_k("test text", top_k=2)

        assert len(result) == 2
        assert all("code" in r and "content" in r and "probability" in r for r in result)


class TestFilterStandardsByModel:
    """Test filter_standards_by_model function"""

    @patch("reports.utils.achievement_inference.logger")
    @patch("reports.utils.achievement_inference.predict_top_k")
    def test_filter_standards_empty_list(self, mock_predict, mock_logger):
        """Test filter_standards_by_model with empty list"""
        result = filter_standards_by_model("test", [])

        assert result == []
        mock_logger.warning.assert_called_once()

    @patch("reports.utils.achievement_inference.logger")
    @patch("reports.utils.achievement_inference.predict_top_k")
    def test_filter_standards_prediction_failed(self, mock_predict, mock_logger):
        """Test filter_standards_by_model when prediction fails"""
        mock_predict.return_value = []

        standards = [{"code": "CODE1", "content": "Content1"}]
        result = filter_standards_by_model("test", standards)

        assert result == standards  # Fallback to all standards
        mock_logger.warning.assert_called_once()

    @patch("reports.utils.achievement_inference.logger")
    @patch("reports.utils.achievement_inference.predict_top_k")
    def test_filter_standards_below_min_results(self, mock_predict, mock_logger):
        """Test filter_standards_by_model when results below minimum"""
        mock_predict.return_value = [
            {"code": "CODE1", "content": "Content1", "probability": 0.9},
            {"code": "CODE2", "content": "Content2", "probability": 0.8},
        ]

        standards = [
            {"code": "CODE1", "content": "Content1"},
            {"code": "CODE2", "content": "Content2"},
            {"code": "CODE3", "content": "Content3"},
        ]

        result = filter_standards_by_model("test", standards, top_k=20, min_results=3)

        assert result == standards  # Fallback to all standards
        mock_logger.warning.assert_called_once()

    @patch("reports.utils.achievement_inference.logger")
    @patch("reports.utils.achievement_inference.predict_top_k")
    def test_filter_standards_success(self, mock_predict, mock_logger):
        """Test filter_standards_by_model successful filtering"""
        mock_predict.return_value = [
            {"code": "CODE2", "content": "Content2", "probability": 0.9},
            {"code": "CODE1", "content": "Content1", "probability": 0.8},
            {"code": "CODE3", "content": "Content3", "probability": 0.7},
        ]

        standards = [
            {"code": "CODE1", "content": "Content1"},
            {"code": "CODE2", "content": "Content2"},
            {"code": "CODE3", "content": "Content3"},
        ]

        result = filter_standards_by_model("test", standards, top_k=20, min_results=3)

        assert len(result) == 3
        assert result[0]["code"] == "CODE2"  # Sorted by probability
        assert result[1]["code"] == "CODE1"
        assert result[2]["code"] == "CODE3"
        mock_logger.info.assert_called_once()


class TestGetTopKWithContent:
    """Test get_top_k_with_content function"""

    @patch("reports.utils.achievement_inference.predict_top_k")
    def test_get_top_k_with_content(self, mock_predict):
        """Test get_top_k_with_content"""
        mock_predict.return_value = [{"code": "CODE1", "content": "Content1", "probability": 0.9}]

        result = get_top_k_with_content("test", top_k=10)

        assert result == mock_predict.return_value
        mock_predict.assert_called_once_with("test", top_k=10)
