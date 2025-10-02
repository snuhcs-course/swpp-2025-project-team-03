import json
import os

from research.extract_acoustic_features import extract_features


def extract_features_from_audio(json_path, audio_root="data"):
    """
    JSON 파일과 동일한 base 이름의 WAV 오디오에서 feature 추출
    (audio는 audio_root 루트 디렉토리에만 있다고 가정)
    """
    # JSON 파일명 -> WAV 파일명으로 변환
    base = os.path.basename(json_path).replace("_presentation.json", ".wav")
    audio_path = os.path.join(audio_root, base)

    if not os.path.exists(audio_path):
        print(f"⚠️ Audio not found: {audio_path}")
        return {}

    try:
        feats = extract_features(
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
        print(f"❌ Feature extraction failed for {audio_path}: {e}")
        return {}


def add_features_to_json(input_root="label_trim", output_root="label", audio_root="data"):
    """
    input_root 밑의 모든 JSON 파일을 순회하면서 prosody feature를 추가하고,
    output_root에 동일한 디렉토리 구조로 저장
    """
    for dirpath, _, filenames in os.walk(input_root):
        for filename in filenames:
            if filename.lower().endswith(".json"):
                in_path = os.path.join(dirpath, filename)
                rel_path = os.path.relpath(in_path, input_root)
                out_path = os.path.join(output_root, rel_path)

                os.makedirs(os.path.dirname(out_path), exist_ok=True)

                try:
                    with open(in_path, "r", encoding="utf-8") as f:
                        data = json.load(f)

                    # prosody features 추가
                    feats = extract_features_from_audio(in_path, audio_root=audio_root)
                    data.update(feats)  # 기존 JSON에 feature 합치기

                    with open(out_path, "w", encoding="utf-8") as f:
                        json.dump(data, f, ensure_ascii=False, indent=2)

                    print(f"✅ Updated: {in_path} -> {out_path}")
                except Exception as e:
                    print(f"❌ Failed: {in_path} ({e})")


if __name__ == "__main__":
    add_features_to_json("label_trim", "label", audio_root="./data")
