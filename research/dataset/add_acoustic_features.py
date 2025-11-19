import argparse
import json
import os
import sys

sys.path.append(os.path.dirname(os.path.dirname(__file__)))
from feature_extractor import extract_acoustic_features


def extract_acoustic_features_from_audio(json_path, audio_root="data", label_root="label"):
    """
    JSON 파일과 동일한 base 이름의 WAV 오디오에서 feature 추출
    (audio는 audio_root 루트 디렉토리 밑의 하위 디렉토리에 있다고 가정)
    """
    # JSON 파일명 -> WAV 파일명으로 변환
    base_wav = os.path.basename(json_path).replace("_presentation.json", ".wav")

    # label_root를 기준으로 한 상대 경로 추출
    rel_dir = os.path.relpath(os.path.dirname(json_path), label_root)

    # 오디오 파일의 전체 경로 조합
    audio_path = os.path.join(audio_root, rel_dir, base_wav)

    if not os.path.exists(audio_path):
        print(f" Audio not found: {audio_path}")
        return {}

    try:
        feats = extract_acoustic_features(
            audio_path,
            fmin=50.0,
            fmax=600.0,
            hop=256,
            smooth_ms=30.0,
            end_win_s=3.0,
            top_db=40.0,
            robust=False,
        )
        return feats
    except Exception as e:
        print(f" Feature extraction failed for {audio_path}: {e}")
        return {}


def add_acoustic_features_inplace(label_root="label", audio_root="data", save_backup=False):
    """
    label_root 밑의 모든 JSON 파일을 순회하면서 prosody feature를 추가하고,
    같은 파일에 병합하여 저장 (in-place)
    """
    for dirpath, _, filenames in os.walk(label_root):
        for filename in filenames:
            if filename.lower().endswith(".json"):
                json_path = os.path.join(dirpath, filename)

                try:
                    with open(json_path, "r", encoding="utf-8") as f:
                        data = json.load(f)

                    # 백업 옵션
                    if save_backup:
                        backup_path = json_path + ".bak"
                        with open(backup_path, "w", encoding="utf-8") as bf:
                            json.dump(data, bf, ensure_ascii=False, indent=2)

                    # prosody features 추가 (label_root 전달)
                    feats = extract_acoustic_features_from_audio(
                        json_path, audio_root=audio_root, label_root=label_root
                    )
                    data.update(feats)  # 기존 JSON에 feature 합치기

                    with open(json_path, "w", encoding="utf-8") as f:
                        json.dump(data, f, ensure_ascii=False, indent=2)

                    print(f" Updated: {json_path}")
                except Exception as e:
                    print(f" Failed: {json_path} ({e})")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="Add acoustic features in-place to JSON files based on a root directory."
    )
    parser.add_argument(
        "--input_root",
        type=str,
        default="dataset/train",
        help="Root directory containing 'data' and 'label' subdirectories.",
    )
    parser.add_argument(
        "--label",
        type=str,
        default="label",
        help="Name of the label subdirectory (default: 'label').",
    )
    parser.add_argument("--backup", action="store_true", help="Create a .bak backup before overwriting.")
    args = parser.parse_args()

    label_root = os.path.join(args.input_root, args.label)
    audio_root = os.path.join(args.input_root, "data")

    add_acoustic_features_inplace(label_root=label_root, audio_root=audio_root, save_backup=args.backup)
