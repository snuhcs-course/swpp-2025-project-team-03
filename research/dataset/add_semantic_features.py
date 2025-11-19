import argparse
import json
import os
import sys

sys.path.append(os.path.dirname(os.path.dirname(__file__)))
from feature_extractor import extract_semantic_features
from sentence_transformers import SentenceTransformer


def add_semantic_features_inplace(
    input_root: str = "dataset",
    model_name: str = "snunlp/KR-SBERT-V40K-klueNLI-augSTS",
    prefix: str = "sem_",
    save_backup: bool = False,
):
    """
    input_root 아래 모든 .json 파일을 순회하며
    JSON의 'script' 텍스트로 semantic features를 계산해 같은 파일에 병합 저장합니다.
    """
    # 모델 한 번만 로드
    model = SentenceTransformer(model_name)
    print(f"[INFO] SBERT loaded: {model_name}")

    n_ok, n_skip, n_err = 0, 0, 0

    for dirpath, _, filenames in os.walk(input_root):
        for filename in filenames:
            if not filename.lower().endswith(".json"):
                continue

            json_path = os.path.join(dirpath, filename)

            try:
                # 원본 로드
                with open(json_path, "r", encoding="utf-8") as f:
                    data = json.load(f)

                # script 유무 확인은 extract_semantic_features에서 처리
                # dict를 그대로 넘기면 내부에서 data['script'] 사용
                feats = extract_semantic_features(
                    data,  # dict 입력 지원
                    model=model,  # 이미 로드된 모델 재사용
                    prefix=prefix,
                )

                # 백업 옵션
                if save_backup:
                    backup_path = json_path + ".bak"
                    with open(backup_path, "w", encoding="utf-8") as bf:
                        json.dump(data, bf, ensure_ascii=False, indent=2)

                # 병합 & 저장(같은 경로에 덮어쓰기)
                data.update(feats)
                with open(json_path, "w", encoding="utf-8") as f:
                    json.dump(data, f, ensure_ascii=False, indent=2)

                print(f" Updated: {json_path}")
                n_ok += 1

            except ValueError as ve:
                #'script 없음' 같은 케이스
                print(f" Skip (no script?): {json_path} ({ve})")
                n_skip += 1
            except Exception as e:
                print(f" Failed: {json_path} ({e})")
                n_err += 1

    print(f"\n[SUMMARY] ok={n_ok}, skip={n_skip}, err={n_err}")


if __name__ == "__main__":
    ap = argparse.ArgumentParser(description="Add SBERT-based semantic features in-place to JSON files under 'label/'.")
    ap.add_argument("--input_root", default="dataset", help="JSON 루트 폴더(기본: label)")
    ap.add_argument("--model_name", default="snunlp/KR-SBERT-V40K-klueNLI-augSTS", help="HuggingFace SBERT 모델 이름")
    ap.add_argument("--prefix", default="sem_", help="결과 키 접두사 (기본: sem_)")
    ap.add_argument("--backup", action="store_true", help="덮어쓰기 전 .bak 백업 저장")
    args = ap.parse_args()

    add_semantic_features_inplace(
        input_root=args.input_root,
        model_name=args.model_name,
        prefix=args.prefix,
        save_backup=args.backup,
    )
