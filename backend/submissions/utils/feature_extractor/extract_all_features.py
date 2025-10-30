import argparse
import os
import sys
import warnings
from pprint import pprint

sys.path.append(os.path.dirname(os.path.dirname(__file__)))


from feature_extractor import extract_acoustic_features
from feature_extractor.extract_features_from_script import extract_features_from_script
from sentence_transformers import SentenceTransformer

from .. import wave_to_text

warnings.filterwarnings("ignore")


def extract_all_features(wav_path: str, model_name: str = None) -> dict:
    """
    WAV 파일에서 STT, 음향, 스크립트 기반, 의미론적 특징을 모두 추출합니다.
    수정된 extract_features_from_script.enrich_json_file 함수를 활용하여
    딕셔너리를 직접 전달하고 인-메모리에서 모든 특징을 통합합니다.

    Args:
        wav_path: WAV 파일 경로
        model_name: SentenceTransformer 모델 경로. None이면 로컬 모델 사용.
    """
    if not os.path.exists(wav_path):
        raise FileNotFoundError(f"WAV 파일을 찾을 수 없습니다: {wav_path}")

    # 기본값으로 로컬 모델 경로 사용
    if model_name is None:
        current_dir = os.path.dirname(os.path.abspath(__file__))
        model_name = os.path.join(current_dir, "..", "KR_SBERT_local")

    try:
        script = wave_to_text.speech_to_text(wav_path)

        # STT 결과가 비어있으면 기본값 설정
        if not script or script.strip() == "":
            script = "음성 인식 결과 없음"
    except Exception as e:
        # STT 실패 시에도 기본값으로 계속 진행
        script = "음성 인식 실패"

    try:
        acoustic_feats = extract_acoustic_features(wav_path)
    except Exception as e:
        raise

    # below stores integrated features
    features_dict = {"script": script, **acoustic_feats}

    # load SBERT model once
    try:
        model = SentenceTransformer(model_name)
        script_feats = extract_features_from_script(features_dict, shared_model=model)

    except Exception as e:
        import traceback

        traceback.print_exc()
        # 기본값으로 빈 딕셔너리 반환
        script_feats = {}

    return features_dict


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="WAV 파일에서 모든 특징을 추출합니다.")
    parser.add_argument("--wav_path", type=str, help="입력 WAV 파일의 경로.")
    parser.add_argument(
        "--model_name",
        type=str,
        default=None,
        help="의미론적 특징 추출을 위한 SBERT 모델 이름. 기본값은 로컬 모델(KR_SBERT_local)을 사용합니다.",
    )
    args = parser.parse_args()

    final_features = extract_all_features(args.wav_path, args.model_name)
    pprint(final_features)
