import os
import tempfile
from unittest.mock import Mock, patch

import numpy as np
import pytest
from submissions.utils.feature_extractor.extract_features_from_script import _semantic_1d_features_from_script
from submissions.utils.feature_extractor.extract_features_from_script import (
    extract_semantic_features as extract_semantic_features_from_script,
)

pytestmark = pytest.mark.django_db


@pytest.fixture
def test_script():
    """테스트용 스크립트 내용"""
    current_dir = os.path.dirname(os.path.abspath(__file__))
    txt_path = os.path.join(current_dir, "test_sample", "test_script.txt")
    if not os.path.exists(txt_path):
        pytest.skip(f"테스트용 텍스트 파일이 없습니다: {txt_path}")
    with open(txt_path, "r", encoding="utf-8") as f:
        return f.read().strip()


class TestSemantic1DFeaturesFromScript:
    """_semantic_1d_features_from_script 함수 테스트"""

    @patch("submissions.utils.feature_extractor.extract_features_from_script.SentenceTransformer")
    def test_semantic_1d_features_from_script_many_sentences(self, mock_sentence_transformer_class, test_script):
        """여러 문장으로 _semantic_1d_features_from_script 테스트 (model.encode mock)"""
        mock_model = Mock()
        sentences = test_script.split(".")
        num_sentences = len([s for s in sentences if s.strip()])
        if num_sentences < 2:
            num_sentences = 5

        mock_embeddings = []
        for i in range(num_sentences):
            emb = np.random.rand(384).astype(np.float32)
            emb = emb / np.linalg.norm(emb)
            mock_embeddings.append(emb)

        def mock_encode(sentences_list, **kwargs):
            n = len(sentences_list) if isinstance(sentences_list, list) else 1
            if n == 0:
                n = 1
            return np.array([mock_embeddings[i % len(mock_embeddings)] for i in range(n)])

        mock_model.encode.side_effect = mock_encode
        mock_sentence_transformer_class.return_value = mock_model

        result = _semantic_1d_features_from_script(test_script, model=mock_model)

        assert isinstance(result, dict)
        assert "adj_sim_mean" in result
        assert "adj_sim_std" in result
        assert "adj_sim_p10" in result
        assert "adj_sim_p50" in result
        assert "adj_sim_p90" in result
        assert "adj_sim_frac_high" in result
        assert "adj_sim_frac_low" in result
        assert "topic_path_len" in result
        assert "dist_to_centroid_mean" in result
        assert "dist_to_centroid_std" in result
        assert "coherence_score" in result
        assert "intra_coh" in result
        assert "inter_div" in result

        assert isinstance(result["adj_sim_mean"], float)
        assert isinstance(result["coherence_score"], float)

    @patch("submissions.utils.feature_extractor.extract_features_from_script.SentenceTransformer")
    def test_semantic_1d_features_from_script_empty_string(self, mock_sentence_transformer_class):
        """빈 문자열로 _semantic_1d_features_from_script 테스트 (T == 0)"""
        mock_model = Mock()
        mock_sentence_transformer_class.return_value = mock_model

        result = _semantic_1d_features_from_script("", model=mock_model)

        assert result["adj_sim_mean"] == 0.0
        assert result["adj_sim_std"] == 0.0
        assert result["coherence_score"] == 1.0
        mock_model.encode.assert_not_called()

    @patch("submissions.utils.feature_extractor.extract_features_from_script.SentenceTransformer")
    def test_semantic_1d_features_from_script_single_sentence(self, mock_sentence_transformer_class):
        """단일 문장으로 _semantic_1d_features_from_script 테스트 (T == 1)"""
        mock_model = Mock()
        mock_sentence_transformer_class.return_value = mock_model

        result = _semantic_1d_features_from_script("단일 문장입니다.", model=mock_model)

        assert result["adj_sim_mean"] == 1.0
        assert result["adj_sim_std"] == 0.0
        assert result["coherence_score"] == 1.0
        mock_model.encode.assert_not_called()

    @patch("submissions.utils.feature_extractor.extract_features_from_script.SentenceTransformer")
    def test_semantic_1d_features_from_script_with_segments(self, mock_sentence_transformer_class):
        """많은 문장으로 세그먼트 분기 커버 (line 386-390)"""
        mock_model = Mock()
        sentences = [
            "첫 번째 문장입니다.",
            "두 번째 문장입니다.",
            "세 번째 문장입니다.",
            "네 번째 문장입니다.",
            "다섯 번째 문장입니다.",
            "여섯 번째 문장입니다.",
            "일곱 번째 문장입니다.",
            "여덟 번째 문장입니다.",
            "아홉 번째 문장입니다.",
        ]
        script = " ".join(sentences)

        mock_embeddings = []
        for _ in range(len(sentences)):
            emb = np.random.rand(384).astype(np.float32)
            emb = emb / np.linalg.norm(emb)
            mock_embeddings.append(emb)

        def mock_encode(sentences_list, **kwargs):
            n = len(sentences_list)
            return np.array([mock_embeddings[i % len(mock_embeddings)] for i in range(n)])

        mock_model.encode.side_effect = mock_encode
        mock_sentence_transformer_class.return_value = mock_model

        result = _semantic_1d_features_from_script(script, model=mock_model)

        assert isinstance(result, dict)
        assert "intra_coh" in result
        assert "inter_div" in result
        assert 0.0 <= result["intra_coh"] <= 1.0
        assert 0.0 <= result["inter_div"] <= 1.0


class TestExtractSemanticFeaturesFromScript:
    """extract_features_from_script.py의 extract_semantic_features 함수 테스트"""

    @patch("submissions.utils.feature_extractor.extract_features_from_script.SentenceTransformer")
    def test_extract_semantic_features_from_dict(self, mock_sentence_transformer_class, test_script):
        """dict 입력으로 extract_semantic_features 테스트"""
        mock_model = Mock()
        sentences = test_script.split(".")
        num_sentences = len([s for s in sentences if s.strip()])
        if num_sentences < 2:
            num_sentences = 5

        mock_embeddings = []
        for _ in range(num_sentences):
            emb = np.random.rand(384).astype(np.float32)
            emb = emb / np.linalg.norm(emb)
            mock_embeddings.append(emb)

        def mock_encode(sentences_list, **kwargs):
            n = len(sentences_list) if isinstance(sentences_list, list) else 1
            return np.array([mock_embeddings[i % len(mock_embeddings)] for i in range(n)])

        mock_model.encode.side_effect = mock_encode
        mock_sentence_transformer_class.return_value = mock_model

        data = {"script": test_script}
        result = extract_semantic_features_from_script(data, model=mock_model)

        assert isinstance(result, dict)
        assert "adj_sim_mean" in result
        assert "coherence_score" in result

    @patch("submissions.utils.feature_extractor.extract_features_from_script.SentenceTransformer")
    def test_extract_semantic_features_from_file_path(self, mock_sentence_transformer_class, test_script):
        """파일 경로로 extract_semantic_features 테스트"""
        current_dir = os.path.dirname(os.path.abspath(__file__))
        test_script_path = os.path.join(current_dir, "test_sample", "test_script.txt")
        if not os.path.exists(test_script_path):
            pytest.skip(f"테스트용 텍스트 파일이 없습니다: {test_script_path}")

        mock_model = Mock()
        sentences = test_script.split(".")
        num_sentences = len([s for s in sentences if s.strip()])
        if num_sentences < 2:
            num_sentences = 5

        mock_embeddings = []
        for _ in range(num_sentences):
            emb = np.random.rand(384).astype(np.float32)
            emb = emb / np.linalg.norm(emb)
            mock_embeddings.append(emb)

        def mock_encode(sentences_list, **kwargs):
            n = len(sentences_list) if isinstance(sentences_list, list) else 1
            return np.array([mock_embeddings[i % len(mock_embeddings)] for i in range(n)])

        mock_model.encode.side_effect = mock_encode
        mock_sentence_transformer_class.return_value = mock_model

        result = extract_semantic_features_from_script(test_script_path, model=mock_model)

        assert isinstance(result, dict)
        assert "adj_sim_mean" in result
        assert "coherence_score" in result

    @patch("submissions.utils.feature_extractor.extract_features_from_script.SentenceTransformer")
    def test_extract_semantic_features_from_json_file(self, mock_sentence_transformer_class, test_script):
        """JSON 파일로 extract_semantic_features 테스트"""
        import json

        with tempfile.NamedTemporaryFile(mode="w", suffix=".json", delete=False, encoding="utf-8") as tmp_file:
            json.dump({"script": test_script}, tmp_file, ensure_ascii=False)
            tmp_file.flush()
            json_path = tmp_file.name

        try:
            mock_model = Mock()
            sentences = test_script.split(".")
            num_sentences = len([s for s in sentences if s.strip()])
            if num_sentences < 2:
                num_sentences = 5

            mock_embeddings = []
            for _ in range(num_sentences):
                emb = np.random.rand(384).astype(np.float32)
                emb = emb / np.linalg.norm(emb)
                mock_embeddings.append(emb)

            def mock_encode(sentences_list, **kwargs):
                n = len(sentences_list) if isinstance(sentences_list, list) else 1
                return np.array([mock_embeddings[i % len(mock_embeddings)] for i in range(n)])

            mock_model.encode.side_effect = mock_encode
            mock_sentence_transformer_class.return_value = mock_model

            result = extract_semantic_features_from_script(json_path, model=mock_model)

            assert isinstance(result, dict)
            assert "adj_sim_mean" in result
        finally:
            os.unlink(json_path)

    @patch("submissions.utils.feature_extractor.extract_features_from_script.SentenceTransformer")
    def test_extract_semantic_features_with_prefix(self, mock_sentence_transformer_class, test_script):
        """prefix 파라미터와 함께 extract_semantic_features 테스트"""
        mock_model = Mock()
        sentences = test_script.split(".")
        num_sentences = len([s for s in sentences if s.strip()])
        if num_sentences < 2:
            num_sentences = 5

        mock_embeddings = []
        for _ in range(num_sentences):
            emb = np.random.rand(384).astype(np.float32)
            emb = emb / np.linalg.norm(emb)
            mock_embeddings.append(emb)

        def mock_encode(sentences_list, **kwargs):
            n = len(sentences_list) if isinstance(sentences_list, list) else 1
            return np.array([mock_embeddings[i % len(mock_embeddings)] for i in range(n)])

        mock_model.encode.side_effect = mock_encode
        mock_sentence_transformer_class.return_value = mock_model

        data = {"script": test_script}
        result = extract_semantic_features_from_script(data, model=mock_model, prefix="sem_")

        assert isinstance(result, dict)
        assert "sem_adj_sim_mean" in result
        assert "sem_coherence_score" in result
        assert "adj_sim_mean" not in result

    def test_extract_semantic_features_empty_dict(self):
        """빈 dict 입력 테스트"""
        with pytest.raises(ValueError, match="dict 입력에서 'script' 문자열을 찾지 못했습니다"):
            extract_semantic_features_from_script({})

    def test_extract_semantic_features_dict_missing_script(self):
        """script 필드가 없는 dict 입력 테스트"""
        with pytest.raises(ValueError, match="dict 입력에서 'script' 문자열을 찾지 못했습니다"):
            extract_semantic_features_from_script({"content": "no script"})

    def test_extract_semantic_features_invalid_type(self):
        """잘못된 타입 입력 테스트"""
        with pytest.raises(TypeError, match="script_or_json은 str.*또는 dict여야 합니다"):
            extract_semantic_features_from_script(123)

    @patch("submissions.utils.feature_extractor.extract_features_from_script.SentenceTransformer")
    def test_extract_semantic_features_sentence_transformer_none(self, mock_sentence_transformer_class):
        """SentenceTransformer가 None인 경우 테스트"""
        mock_sentence_transformer_class.return_value = None

        with pytest.raises(RuntimeError, match="SentenceTransformer 사용 불가"):
            extract_semantic_features_from_script({"script": "test"}, model=None)
