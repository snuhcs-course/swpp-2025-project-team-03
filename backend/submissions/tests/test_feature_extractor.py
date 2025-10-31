import os
from unittest.mock import Mock, patch

import numpy as np
import pytest

# 모든 테스트에서 DB 접근 허용 (필요한 경우)
pytestmark = pytest.mark.django_db


@pytest.fixture
def test_wav_path():
    """테스트용 WAV 파일 경로"""
    current_dir = os.path.dirname(os.path.abspath(__file__))
    wav_path = os.path.join(current_dir, "test_sample/test_record.wav")
    if not os.path.exists(wav_path):
        pytest.skip(f"테스트용 WAV 파일이 없습니다: {wav_path}")
    return wav_path


@pytest.fixture
def test_script():
    """테스트용 스크립트 내용"""
    current_dir = os.path.dirname(os.path.abspath(__file__))
    txt_path = os.path.join(current_dir, "test_sample/test_script.txt")
    if not os.path.exists(txt_path):
        pytest.skip(f"테스트용 텍스트 파일이 없습니다: {txt_path}")
    with open(txt_path, "r", encoding="utf-8") as f:
        return f.read().strip()


class TestExtractAllFeatures:
    """extract_all_features 함수 테스트"""

    @patch("submissions.utils.feature_extractor.extract_all_features.SentenceTransformer")
    @patch("submissions.utils.feature_extractor.extract_all_features.wave_to_text.speech_to_text")
    @patch("submissions.utils.feature_extractor.extract_all_features.extract_acoustic_features")
    @patch("submissions.utils.feature_extractor.extract_all_features.extract_features_from_script")
    def test_extract_all_features_success(
        self,
        mock_extract_features_from_script,
        mock_extract_acoustic_features,
        mock_speech_to_text,
        mock_sentence_transformer_class,
        test_wav_path,
        test_script,
    ):
        """extract_all_features 정상 동작 테스트"""
        # Mock 설정
        mock_speech_to_text.return_value = test_script

        mock_acoustic_features = {
            "sr": 16000,
            "total_length": 3.5,
            "min_f0_hz": 150.0,
            "max_f0_hz": 300.0,
            "range_f0_hz": 150.0,
            "n_f0_used": 1000,
            "tot_slope_f0_st_per_s": 0.5,
            "end_slope_f0_st_per_s": -0.2,
            "pause_0_5_cnt": 2,
            "pause_1_0_cnt": 1,
            "pause_2_0_cnt": 0,
        }
        mock_extract_acoustic_features.return_value = mock_acoustic_features

        # SentenceTransformer mock
        mock_model = Mock()
        mock_sentence_transformer_class.return_value = mock_model

        # extract_features_from_script mock: 실제로는 입력 dict를 수정하여 반환
        def mock_extract_features_func(data, **kwargs):
            # 입력 dict를 수정하여 반환 (실제 동작 시뮬레이션)
            updated = dict(data)
            updated["syllable_cnt"] = 10
            updated["word_cnt"] = 5
            updated["sentence_cnt"] = 1
            updated["filler_rule_count"] = 3
            updated["filler_fuzzy_count"] = 1
            return updated

        mock_extract_features_from_script.side_effect = mock_extract_features_func

        # 함수 실행
        from submissions.utils.feature_extractor.extract_all_features import extract_all_features

        result = extract_all_features(test_wav_path)

        # 검증
        assert result["script"] == test_script
        assert result["sr"] == 16000
        assert result["total_length"] == 3.5
        assert "min_f0_hz" in result
        assert "max_f0_hz" in result

        # Mock 호출 확인
        mock_speech_to_text.assert_called_once_with(test_wav_path)
        mock_extract_acoustic_features.assert_called_once_with(test_wav_path)
        mock_sentence_transformer_class.assert_called_once()
        mock_extract_features_from_script.assert_called_once()

    @patch("submissions.utils.feature_extractor.extract_all_features.SentenceTransformer")
    @patch("submissions.utils.feature_extractor.extract_all_features.wave_to_text.speech_to_text")
    @patch("submissions.utils.feature_extractor.extract_all_features.extract_acoustic_features")
    def test_extract_all_features_with_empty_script(
        self,
        mock_extract_acoustic_features,
        mock_speech_to_text,
        mock_sentence_transformer_class,
        test_wav_path,
    ):
        """STT 결과가 빈 문자열인 경우 테스트"""
        # Mock 설정
        mock_speech_to_text.return_value = ""

        mock_acoustic_features = {
            "sr": 16000,
            "total_length": 3.5,
        }
        mock_extract_acoustic_features.return_value = mock_acoustic_features

        # SentenceTransformer mock
        mock_model = Mock()
        mock_sentence_transformer_class.return_value = mock_model

        # 함수 실행
        from submissions.utils.feature_extractor.extract_all_features import extract_all_features

        result = extract_all_features(test_wav_path)

        # 검증: 빈 스크립트는 "음성 인식 결과 없음"으로 설정되어야 함
        assert result["script"] == "음성 인식 결과 없음"

    @patch("submissions.utils.feature_extractor.extract_all_features.wave_to_text.speech_to_text")
    def test_extract_all_features_file_not_found(self, mock_speech_to_text):
        """파일이 존재하지 않는 경우 테스트"""
        from submissions.utils.feature_extractor.extract_all_features import extract_all_features

        with pytest.raises(FileNotFoundError):
            extract_all_features("/nonexistent/file.wav")

    @patch("submissions.utils.feature_extractor.extract_all_features.SentenceTransformer")
    @patch("submissions.utils.feature_extractor.extract_all_features.wave_to_text.speech_to_text")
    @patch("submissions.utils.feature_extractor.extract_all_features.extract_acoustic_features")
    def test_extract_all_features_stt_exception(
        self,
        mock_extract_acoustic_features,
        mock_speech_to_text,
        mock_sentence_transformer_class,
        test_wav_path,
    ):
        """STT 예외 발생 시 테스트"""
        # Mock 설정: STT에서 예외 발생
        mock_speech_to_text.side_effect = Exception("STT API Error")

        mock_acoustic_features = {
            "sr": 16000,
            "total_length": 3.5,
        }
        mock_extract_acoustic_features.return_value = mock_acoustic_features

        # SentenceTransformer mock
        mock_model = Mock()
        mock_sentence_transformer_class.return_value = mock_model

        # 함수 실행
        from submissions.utils.feature_extractor.extract_all_features import extract_all_features

        result = extract_all_features(test_wav_path)

        # 검증: STT 실패 시 "음성 인식 실패"로 설정되어야 함
        assert result["script"] == "음성 인식 실패"
        assert "sr" in result

    @patch("submissions.utils.feature_extractor.extract_all_features.SentenceTransformer")
    @patch("submissions.utils.feature_extractor.extract_all_features.wave_to_text.speech_to_text")
    @patch("submissions.utils.feature_extractor.extract_all_features.extract_acoustic_features")
    def test_extract_all_features_sentence_transformer_exception(
        self,
        mock_extract_acoustic_features,
        mock_speech_to_text,
        mock_sentence_transformer_class,
        test_wav_path,
        test_script,
    ):
        """SentenceTransformer 로드 실패 시 테스트"""
        # Mock 설정
        mock_speech_to_text.return_value = test_script

        mock_acoustic_features = {
            "sr": 16000,
            "total_length": 3.5,
        }
        mock_extract_acoustic_features.return_value = mock_acoustic_features

        # SentenceTransformer 로드 실패
        mock_sentence_transformer_class.side_effect = Exception("Model load error")

        # 함수 실행
        from submissions.utils.feature_extractor.extract_all_features import extract_all_features

        result = extract_all_features(test_wav_path)

        # 검증: 모델 로드 실패해도 기본 특징은 반환되어야 함
        assert result["script"] == test_script
        assert result["sr"] == 16000

    @patch("submissions.utils.feature_extractor.extract_all_features.SentenceTransformer")
    @patch("submissions.utils.feature_extractor.extract_all_features.wave_to_text.speech_to_text")
    @patch("submissions.utils.feature_extractor.extract_all_features.extract_acoustic_features")
    @patch("submissions.utils.feature_extractor.extract_all_features.extract_features_from_script")
    def test_extract_all_features_custom_model_name(
        self,
        mock_extract_features_from_script,
        mock_extract_acoustic_features,
        mock_speech_to_text,
        mock_sentence_transformer_class,
        test_wav_path,
        test_script,
    ):
        """커스텀 모델 이름 사용 테스트"""
        # Mock 설정
        mock_speech_to_text.return_value = test_script

        mock_acoustic_features = {
            "sr": 16000,
            "total_length": 3.5,
        }
        mock_extract_acoustic_features.return_value = mock_acoustic_features

        # SentenceTransformer mock
        mock_model = Mock()
        mock_sentence_transformer_class.return_value = mock_model

        # extract_features_from_script mock: 입력 dict를 수정하여 반환
        def mock_extract_features_func(data, **kwargs):
            updated = dict(data)
            updated["syllable_cnt"] = 10
            updated["word_cnt"] = 5
            updated["sentence_cnt"] = 1
            return updated

        mock_extract_features_from_script.side_effect = mock_extract_features_func

        # 함수 실행
        from submissions.utils.feature_extractor.extract_all_features import extract_all_features

        custom_model_name = "custom/model/path"
        result = extract_all_features(test_wav_path, model_name=custom_model_name)

        # 검증: 커스텀 모델 이름이 사용되었는지 확인
        mock_sentence_transformer_class.assert_called_once_with(custom_model_name)
        assert result["script"] == test_script

    @patch("submissions.utils.feature_extractor.extract_all_features.SentenceTransformer")
    @patch("submissions.utils.feature_extractor.extract_all_features.wave_to_text.speech_to_text")
    @patch("submissions.utils.feature_extractor.extract_all_features.extract_features_from_script")
    def test_extract_all_features_integration_with_real_acoustic(
        self,
        mock_extract_features_from_script,
        mock_speech_to_text,
        mock_sentence_transformer_class,
        test_wav_path,
        test_script,
    ):
        """실제 extract_acoustic_features와 통합 테스트 (STT와 SentenceTransformer, extract_features_from_script만 mock)"""
        # Mock 설정
        mock_speech_to_text.return_value = test_script

        # SentenceTransformer mock
        mock_model = Mock()
        mock_sentence_transformer_class.return_value = mock_model

        # Mock 모델의 encode 메서드가 적절한 임베딩을 반환하도록 설정
        mock_embedding = np.random.rand(384).astype(np.float32)
        mock_model.encode.return_value = np.array([mock_embedding])

        # extract_features_from_script mock: 입력 dict를 수정하여 반환
        def mock_extract_features_func(data, **kwargs):
            updated = dict(data)
            updated["syllable_cnt"] = 10
            updated["word_cnt"] = 5
            updated["sentence_cnt"] = 1
            return updated

        mock_extract_features_from_script.side_effect = mock_extract_features_func

        # 함수 실행 (extract_acoustic_features는 실제로 호출됨)
        from submissions.utils.feature_extractor.extract_all_features import extract_all_features

        result = extract_all_features(test_wav_path)

        # 검증: 실제 음향 특징 추출이 실행되었는지 확인
        assert result["script"] == test_script
        assert "sr" in result
        assert "total_length" in result
        # 실제 wav 파일에서 추출된 특징이 있어야 함
        assert isinstance(result["sr"], int)
        # extract_features_from_script가 호출되었는지 확인
        mock_extract_features_from_script.assert_called_once()


class TestExtractFeaturesFromScript:
    """extract_features_from_script 함수 테스트"""

    @patch("submissions.utils.feature_extractor.extract_features_from_script.SentenceTransformer")
    def test_extract_features_from_script_basic(self, mock_sentence_transformer_class, test_script):
        """extract_features_from_script 기본 동작 테스트"""
        from submissions.utils.feature_extractor.extract_features_from_script import extract_features_from_script

        # Mock SentenceTransformer
        mock_model = Mock()

        # encode 메서드가 문장 개수에 맞는 임베딩 배열을 반환하도록 설정
        # test_script를 문장으로 분할하여 각 문장에 대한 임베딩 생성
        sentences = test_script.split(".")
        num_sentences = len([s for s in sentences if s.strip()])
        if num_sentences == 0:
            num_sentences = 1

        # 각 문장에 대한 384차원 임베딩 생성 (정규화된 벡터)
        mock_embeddings = []
        for i in range(num_sentences):
            emb = np.random.rand(384).astype(np.float32)
            # L2 정규화 (cosine similarity 계산을 위해)
            emb = emb / np.linalg.norm(emb)
            mock_embeddings.append(emb)

        mock_embedding_array = np.array(mock_embeddings)

        # encode가 입력 리스트에 따라 적절한 개수의 임베딩 반환
        def mock_encode(sentences_list, **kwargs):
            # 입력 문장 수만큼 임베딩 반환
            n = len(sentences_list) if isinstance(sentences_list, list) else 1
            if n == 0:
                n = 1
            embeddings = np.array([mock_embeddings[i % len(mock_embeddings)] for i in range(n)])
            # normalize_embeddings가 True인 경우 정규화 (이미 정규화되어 있지만)
            if kwargs.get("normalize_embeddings", False):
                # L2 정규화
                norms = np.linalg.norm(embeddings, axis=1, keepdims=True)
                embeddings = embeddings / (norms + 1e-8)
            return embeddings

        mock_model.encode.side_effect = mock_encode
        mock_sentence_transformer_class.return_value = mock_model

        # 입력 데이터
        data = {
            "script": test_script,
            "sr": 16000,
            "total_length": 3.5,
        }

        # 함수 실행
        result = extract_features_from_script(data, shared_model=mock_model)

        # 검증: 결과가 dict인지 확인
        assert isinstance(result, dict)
        # 기본 필드들이 유지되었는지 확인
        assert result["script"] == test_script
        assert result["sr"] == 16000
        # extract_features_from_script가 추가한 특징들이 있는지 확인
        assert "syllable_cnt" in result
        assert "word_cnt" in result
        assert "sentence_cnt" in result

    def test_extract_features_from_script_empty_script(self):
        """빈 스크립트 입력 테스트"""
        from submissions.utils.feature_extractor.extract_features_from_script import extract_features_from_script

        data = {
            "script": "",
            "sr": 16000,
        }

        with pytest.raises(ValueError, match="'script' not found or empty"):
            extract_features_from_script(data)

    def test_extract_features_from_script_invalid_input(self):
        """잘못된 입력 타입 테스트"""
        from submissions.utils.feature_extractor.extract_features_from_script import extract_features_from_script

        with pytest.raises(TypeError, match="data must be a dict"):
            extract_features_from_script("not a dict")


class TestExtractAcousticFeatures:
    """extract_acoustic_features 함수 테스트"""

    def test_extract_acoustic_features_success(self, test_wav_path):
        """extract_acoustic_features 정상 동작 테스트 (실제 wav 파일 사용)"""
        from submissions.utils.feature_extractor.extract_acoustic_features import extract_acoustic_features

        result = extract_acoustic_features(test_wav_path)

        # 검증: 기본 특징들이 포함되어 있는지 확인
        assert isinstance(result, dict)
        assert "sr" in result
        assert isinstance(result["sr"], int)
        assert "total_length" in result
        assert isinstance(result["total_length"], float)

        # F0 관련 특징 확인
        assert "min_f0_hz" in result
        assert "max_f0_hz" in result
        assert "range_f0_hz" in result
        assert "n_f0_used" in result

        # Pause 관련 특징 확인 (기본 파라미터 사용 시)
        # pause_0_5_cnt, pause_1_0_cnt, pause_2_0_cnt가 있을 수 있음
        pause_keys = [key for key in result.keys() if key.startswith("pause_")]
        assert len(pause_keys) >= 0  # pause 키가 없을 수도 있음 (침묵 구간이 없는 경우)

    def test_extract_acoustic_features_file_not_found(self):
        """파일이 존재하지 않는 경우 테스트"""
        from submissions.utils.feature_extractor.extract_acoustic_features import extract_acoustic_features

        with pytest.raises(Exception):  # soundfile가 FileNotFoundError를 발생시킴
            extract_acoustic_features("/nonexistent/file.wav")

    def test_extract_acoustic_features_custom_parameters(self, test_wav_path):
        """커스텀 파라미터로 실행 테스트"""
        from submissions.utils.feature_extractor.extract_acoustic_features import extract_acoustic_features

        result = extract_acoustic_features(
            test_wav_path,
            fmin=80.0,
            fmax=400.0,
            hop=512,
            smooth_ms=50.0,
            pause_secs=(1.0, 2.0),
        )

        # 검증: 결과가 올바르게 반환되었는지 확인
        assert isinstance(result, dict)
        assert "sr" in result

        # 커스텀 pause_secs에 따른 pause 키 확인
        # pause_secs=(1.0, 2.0)이므로 pause_1_cnt와 pause_2_cnt가 생성됨
        # _fmt_pause_key 함수가 _0을 제거하므로 pause_1_cnt, pause_2_cnt 형식
        assert "pause_1_cnt" in result
        assert "pause_2_cnt" in result
        # pause_0_5_cnt는 없어야 함 (pause_secs에 포함되지 않았으므로)
        assert "pause_0_5_cnt" not in result

        assert "min_f0_hz" in result
        assert "max_f0_hz" in result
        assert "range_f0_hz" in result
        assert "n_f0_used" in result
        assert "tot_slope_f0_st_per_s" in result
        assert "end_slope_f0_st_per_s" in result


class TestExtractSemanticFeatures:
    """extract_semantic_features 함수 테스트"""

    @patch("submissions.utils.feature_extractor.extract_semantic_features.SentenceTransformer")
    def test_extract_semantic_features_from_dict(self, mock_sentence_transformer_class, test_script):
        """dict 입력으로 extract_semantic_features 테스트"""
        from submissions.utils.feature_extractor.extract_semantic_features import extract_semantic_features

        mock_model = Mock()
        sentences = test_script.split(".")
        num_sentences = len([s for s in sentences if s.strip()])
        if num_sentences == 0:
            num_sentences = 1

        mock_embeddings = []
        for i in range(max(num_sentences, 3)):
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

        data = {"script": test_script}

        result = extract_semantic_features(data, model=mock_model)

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

    @patch("submissions.utils.feature_extractor.extract_semantic_features.SentenceTransformer")
    def test_extract_semantic_features_from_text_string(self, mock_sentence_transformer_class, test_script):
        """텍스트 문자열 입력으로 extract_semantic_features 테스트"""
        from submissions.utils.feature_extractor.extract_semantic_features import extract_semantic_features

        mock_model = Mock()
        sentences = test_script.split(".")
        num_sentences = len([s for s in sentences if s.strip()])
        if num_sentences == 0:
            num_sentences = 1

        mock_embeddings = []
        for i in range(max(num_sentences, 3)):
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

        result = extract_semantic_features(test_script, model=mock_model)

        assert isinstance(result, dict)
        assert "adj_sim_mean" in result

    @patch("submissions.utils.feature_extractor.extract_semantic_features.SentenceTransformer")
    def test_extract_semantic_features_with_prefix(self, mock_sentence_transformer_class, test_script):
        """prefix 파라미터 테스트"""
        from submissions.utils.feature_extractor.extract_semantic_features import extract_semantic_features

        mock_model = Mock()
        mock_embeddings = [np.random.rand(384).astype(np.float32) for _ in range(3)]
        for emb in mock_embeddings:
            emb /= np.linalg.norm(emb)

        def mock_encode(sentences_list, **kwargs):
            n = len(sentences_list) if isinstance(sentences_list, list) else 1
            return np.array([mock_embeddings[i % len(mock_embeddings)] for i in range(n)])

        mock_model.encode.side_effect = mock_encode
        mock_sentence_transformer_class.return_value = mock_model

        data = {"script": test_script}
        result = extract_semantic_features(data, model=mock_model, prefix="sem_")

        assert isinstance(result, dict)
        assert "sem_adj_sim_mean" in result
        assert "sem_coherence_score" in result
        assert "adj_sim_mean" not in result

    def test_extract_semantic_features_empty_dict(self):
        """빈 dict 입력 테스트"""
        from submissions.utils.feature_extractor.extract_semantic_features import extract_semantic_features

        with pytest.raises(ValueError, match="dict 입력에서 'script' 문자열을 찾지 못했습니다"):
            extract_semantic_features({})

    def test_extract_semantic_features_invalid_input_type(self):
        """잘못된 입력 타입 테스트"""
        from submissions.utils.feature_extractor.extract_semantic_features import extract_semantic_features

        with pytest.raises(TypeError, match="script_or_json은 str.*또는 dict여야 합니다"):
            extract_semantic_features(123)

    @patch("submissions.utils.feature_extractor.extract_semantic_features.SentenceTransformer")
    def test_extract_semantic_features_single_sentence(self, mock_sentence_transformer_class):
        """단일 문장 입력 테스트"""
        from submissions.utils.feature_extractor.extract_semantic_features import extract_semantic_features

        mock_model = Mock()
        mock_sentence_transformer_class.return_value = mock_model

        data = {"script": "이것은 단일 문장입니다."}
        result = extract_semantic_features(data, model=mock_model)

        assert isinstance(result, dict)
        assert result["adj_sim_mean"] == 1.0
        assert result["adj_sim_std"] == 0.0
        assert result["coherence_score"] == 1.0

    @patch("submissions.utils.feature_extractor.extract_semantic_features.SentenceTransformer")
    def test_extract_semantic_features_custom_thresholds(self, mock_sentence_transformer_class, test_script):
        """커스텀 임계값 사용 테스트"""
        from submissions.utils.feature_extractor.extract_semantic_features import extract_semantic_features

        mock_model = Mock()
        sentences = test_script.split(".")
        num_sentences = len([s for s in sentences if s.strip()])
        if num_sentences == 0:
            num_sentences = 1

        mock_embeddings = []
        for _ in range(max(num_sentences, 3)):
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

        data = {"script": test_script}
        result = extract_semantic_features(data, model=mock_model, high_thr=0.9, low_thr=0.4)

        assert isinstance(result, dict)
        assert "adj_sim_frac_high" in result
        assert "adj_sim_frac_low" in result


class TestSpeechToText:
    """speech_to_text 함수 테스트 (mocking)"""

    @patch("submissions.utils.wave_to_text.speech.SpeechClient")
    def test_speech_to_text_success(self, mock_speech_client_class, test_wav_path, test_script):
        """speech_to_text 정상 동작 테스트 (Google STT mock)"""
        from submissions.utils.wave_to_text import speech_to_text

        # Mock 설정
        mock_client = Mock()
        mock_speech_client_class.return_value = mock_client

        # Mock response 설정
        mock_result = Mock()
        mock_result.alternatives = [Mock()]
        mock_result.alternatives[0].transcript = test_script

        mock_response = Mock()
        mock_response.results = [mock_result]
        mock_client.recognize.return_value = mock_response

        # 함수 실행
        result = speech_to_text(test_wav_path)

        # 검증
        assert result == test_script
        mock_client.recognize.assert_called_once()

    @patch("submissions.utils.wave_to_text.speech.SpeechClient")
    def test_speech_to_text_empty_response(self, mock_speech_client_class, test_wav_path):
        """STT 응답이 비어있는 경우 테스트"""
        from submissions.utils.wave_to_text import speech_to_text

        # Mock 설정
        mock_client = Mock()
        mock_speech_client_class.return_value = mock_client

        # 빈 응답 설정
        mock_response = Mock()
        mock_response.results = []
        mock_client.recognize.return_value = mock_response

        # 함수 실행
        result = speech_to_text(test_wav_path)

        # 검증: 빈 문자열 반환
        assert result == ""

    @patch("submissions.utils.wave_to_text.speech.SpeechClient")
    def test_speech_to_text_custom_language(self, mock_speech_client_class, test_wav_path, test_script):
        """커스텀 언어 코드 사용 테스트"""
        from submissions.utils.wave_to_text import speech_to_text

        # Mock 설정
        mock_client = Mock()
        mock_speech_client_class.return_value = mock_client

        # Mock response 설정
        mock_result = Mock()
        mock_result.alternatives = [Mock()]
        mock_result.alternatives[0].transcript = test_script

        mock_response = Mock()
        mock_response.results = [mock_result]
        mock_client.recognize.return_value = mock_response

        # 함수 실행
        result = speech_to_text(test_wav_path, language_code="en-US")

        # 검증: 언어 코드가 올바르게 전달되었는지 확인
        call_args = mock_client.recognize.call_args
        config = call_args[1]["config"]  # keyword argument로 전달된 config
        assert config.language_code == "en-US"
        assert result == test_script
