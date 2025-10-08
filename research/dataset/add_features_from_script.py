import argparse
import os
import sys

sys.path.append(os.path.dirname(os.path.dirname(__file__)))
from extract_features_from_script import enrich_json_file
from sentence_transformers import SentenceTransformer


def add_features_inplace(
    input_root: str = "dataset",
    *,
    prefix: str = "",
    save_backup: bool = False,
    # fillers
    token_ratio_thr: float = 0.86,
    use_embeddings: bool = False,
    model_name: str = "snunlp/KR-SBERT-V40K-klueNLI-augSTS",
    emb_thr: float = 0.78,
    # near-dup
    thr: float = 0.85,
    window: int = 3,
    ngram_max: int = 2,
    min_char_len: int = 2,
    no_skip_fillers: bool = False,
    store_pairs: bool = False,
    # semantic 1D
    add_semantic_1d: bool = False,
    sem_high_thr: float = 0.85,
    sem_low_thr: float = 0.50,
):
    """
    input_root 아래 모든 .json 파일을 순회하며
    enrich_json_file을 호출해 같은 파일에 feature들을 병합 저장합니다.
    """
    shared_model = SentenceTransformer(model_name)
    print(f"[INFO] SBERT loaded once: {model_name}")
    n_ok, n_skip, n_err = 0, 0, 0

    for dirpath, _, filenames in os.walk(input_root):
        for filename in filenames:
            if not filename.lower().endswith(".json"):
                continue

            json_path = os.path.join(dirpath, filename)
            try:
                # enrich_json_file이 내부에서 유효성(script/total_length) 검사를 수행
                summary = enrich_json_file(
                    json_path=json_path,
                    prefix=prefix,
                    save_backup=save_backup,
                    # fillers
                    token_ratio_thr=token_ratio_thr,
                    use_embeddings=use_embeddings,
                    embed_model_name=model_name,
                    emb_thr=emb_thr,
                    # near-dup
                    neardup_thr=thr,
                    neardup_window=window,
                    neardup_ngram_max=ngram_max,
                    neardup_min_char_len=min_char_len,
                    neardup_skip_fillers=(not no_skip_fillers),
                    store_pairs=store_pairs,
                    # semantic 1D
                    add_semantic_1d=add_semantic_1d,
                    sem_high_thr=sem_high_thr,
                    sem_low_thr=sem_low_thr,
                    shared_model=shared_model,
                )
                print(f"✅ Updated: {json_path}  -> {summary}")
                n_ok += 1

            except (ValueError, FileNotFoundError) as ve:
                # 예: script 없음, total_length 없음/0 이하 등
                print(f"⚠️ Skip: {json_path} ({ve})")
                n_skip += 1
            except Exception as e:
                print(f"❌ Failed: {json_path} ({e})")
                n_err += 1

    print(f"\n[SUMMARY] ok={n_ok}, skip={n_skip}, err={n_err}")


if __name__ == "__main__":
    ap = argparse.ArgumentParser(
        description="Run enrich_features.enrich_json_file in-place on all JSON files under input_root."
    )
    ap.add_argument("--input_root", default="dataset", help="JSON 루트 폴더")
    ap.add_argument("--prefix", default="", help="저장 키 접두사 (예: sem_)")
    ap.add_argument("--backup", action="store_true", help="덮어쓰기 전 .bak 백업 저장")

    # fillers
    ap.add_argument("--token_ratio_thr", type=float, default=0.86, help="필러 퍼지 매칭 임계값")
    ap.add_argument("--use_embeddings", action="store_true", help="SBERT 임베딩 보조/near-dup/semantic-1D 사용")
    ap.add_argument("--model_name", default="snunlp/KR-SBERT-V40K-klueNLI-augSTS", help="HuggingFace SBERT 모델")
    ap.add_argument("--emb_thr", type=float, default=0.78, help="필러 임베딩 보조 임계값")

    # near-dup
    ap.add_argument("--thr", type=float, default=0.85, help="near-dup 코사인 유사도 임계값")
    ap.add_argument("--window", type=int, default=3, help="near-dup 비교 윈도우")
    ap.add_argument("--ngram_max", type=int, default=2, help="near-dup 구절 최대 어절 수")
    ap.add_argument("--min_char_len", type=int, default=2, help="near-dup 스팬 최소 글자 길이")
    ap.add_argument("--no_skip_fillers", action="store_true", help="near-dup에서 필러/기능어 제외하지 않음")
    ap.add_argument("--store_pairs", action="store_true", help="near-dup 매칭 쌍까지 저장(파일 커질 수 있음)")

    # semantic 1D
    ap.add_argument("--add_semantic_1d", action="store_true", help="semantic 1D 피처 추가 계산/저장")
    ap.add_argument("--sem_high_thr", type=float, default=0.85, help="semantic 1D: 인접 유사도 high 임계")
    ap.add_argument("--sem_low_thr", type=float, default=0.50, help="semantic 1D: 인접 유사도 low 임계")

    args = ap.parse_args()

    add_features_inplace(
        input_root=args.input_root,
        prefix=args.prefix,
        save_backup=args.backup,
        # fillers
        token_ratio_thr=args.token_ratio_thr,
        use_embeddings=args.use_embeddings,
        model_name=args.model_name,
        emb_thr=args.emb_thr,
        # near-dup
        thr=args.thr,
        window=args.window,
        ngram_max=args.ngram_max,
        min_char_len=args.min_char_len,
        no_skip_fillers=args.no_skip_fillers,
        store_pairs=args.store_pairs,
        # semantic 1D
        add_semantic_1d=args.add_semantic_1d,
        sem_high_thr=args.sem_high_thr,
        sem_low_thr=args.sem_low_thr,
    )
