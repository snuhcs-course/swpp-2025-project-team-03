import argparse
import os
import sys
import warnings
from pprint import pprint

sys.path.append(os.path.dirname(os.path.dirname(__file__)))


from feature_extractor import extract_acoustic_features, extract_semantic_features
from stt import wave_to_text

warnings.filterwarnings("ignore")


def extract_all_features(wav_path: str, model_name: str = "snunlp/KR-SBERT-V40K-klueNLI-augSTS") -> dict:
    """
    WAV 파일에서 STT, 음향, 스크립트 기반, 의미론적 특징을 모두 추출합니다.
    수정된 extract_features_from_script.enrich_json_file 함수를 활용하여
    딕셔너리를 직접 전달하고 인-메모리에서 모든 특징을 통합합니다.
    """
    if not os.path.exists(wav_path):
        raise FileNotFoundError(f"WAV 파일을 찾을 수 없습니다: {wav_path}")

    print("1. speech to text...")
    script = wave_to_text.speech_to_text(wav_path)

    print("2. extract acoustic features...")
    acoustic_feats = extract_acoustic_features(wav_path)

    # below stores integrated features
    features_dict = {"script": script, **acoustic_feats}

    print("3. extract semantic features")
    semantic_feats = extract_semantic_features(features_dict)
    features_dict.update(semantic_feats)

    print("4. extract features from script...")
    # TODO: 도연아 잘 부탁해

    return features_dict


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="WAV 파일에서 모든 특징을 추출합니다.")
    parser.add_argument("wav_path", type=str, help="입력 WAV 파일의 경로.")
    parser.add_argument(
        "--model_name",
        type=str,
        default="snunlp/KR-SBERT-V40K-klueNLI-augSTS",
        help="의미론적 특징 추출을 위한 SBERT 모델 이름.",
    )
    args = parser.parse_args()

    final_features = extract_all_features(args.wav_path, args.model_name)
    pprint(final_features)
