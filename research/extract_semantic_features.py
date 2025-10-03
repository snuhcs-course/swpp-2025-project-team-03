import json
import os
import re
import unicodedata
from typing import Dict, List, Optional, Union

import numpy as np
from sentence_transformers import SentenceTransformer
from sklearn.metrics.pairwise import cosine_similarity


def _normalize(s: str) -> str:
    s = unicodedata.normalize("NFC", s)
    return re.sub(r"\s+", " ", s).strip()


def _split_sentences_ko(text: str) -> List[str]:
    t = _normalize(text)
    parts = re.split(r"[\.!\?\n…]+", t)
    return [p.strip() for p in parts if p.strip()]


def _semantic_1d_features_from_script(
    script_text: str,
    model: SentenceTransformer,
    high_thr: float = 0.85,
    low_thr: float = 0.50,
) -> Dict[str, float]:
    sents = _split_sentences_ko(script_text)
    T = len(sents)
    if T <= 1:
        return {
            "adj_sim_mean": 1.0 if T == 1 else 0.0,
            "adj_sim_std": 0.0,
            "adj_sim_p10": 1.0 if T == 1 else 0.0,
            "adj_sim_p50": 1.0 if T == 1 else 0.0,
            "adj_sim_p90": 1.0 if T == 1 else 0.0,
            "adj_sim_frac_high": 1.0 if T == 1 else 0.0,
            "adj_sim_frac_low": 0.0,
            "topic_path_len": 0.0,
            "dist_to_centroid_mean": 0.0,
            "dist_to_centroid_std": 0.0,
            "coherence_score": 1.0,
            "intra_coh": 1.0,
            "inter_div": 0.0,
        }

    # 임베딩 (정규화)
    embs = model.encode(sents, normalize_embeddings=True)
    embs = np.asarray(embs)  # (T, D)

    # 인접 코사인 유사도(정규화 벡터 → 점곱)
    adj_sim = np.sum(embs[:-1] * embs[1:], axis=1)
    adj_cos = np.clip(adj_sim, -1.0, 1.0)

    # L2 거리로 변환 후 누적 길이(주제 경로 길이)
    adj_dist = np.sqrt(np.maximum(0.0, 2.0 - 2.0 * adj_cos))
    topic_path_len = float(np.sum(adj_dist))

    # 전역 센트로이드 대비 거리
    centroid = embs.mean(axis=0)
    centroid = centroid / (np.linalg.norm(centroid) + 1e-12)
    dist_to_centroid = np.sqrt(np.maximum(0.0, 2.0 - 2.0 * np.clip(embs @ centroid, -1.0, 1.0)))

    # 앞/중/끝 세그먼트 내부/간
    idx1, idx2 = int(T * 1 / 3), int(T * 2 / 3)
    segs = [
        embs[:idx1] if idx1 > 0 else embs[:1],
        embs[idx1:idx2] if idx2 - idx1 > 0 else embs[idx1 : idx1 + 1],
        embs[idx2:] if T - idx2 > 0 else embs[-1:],
    ]
    intra_list, seg_centroids = [], []
    for seg in segs:
        c = seg.mean(axis=0)
        c = c / (np.linalg.norm(c) + 1e-12)
        seg_centroids.append(c)
        if len(seg) >= 2:
            cs = cosine_similarity(seg, seg)
            iu = (np.sum(cs) - np.trace(cs)) / (len(seg) * len(seg) - 1)
            intra_list.append(float(iu))
        else:
            intra_list.append(1.0)

    inter_cs = []
    for i in range(3):
        for j in range(i + 1, 3):
            inter_cs.append(float(np.dot(seg_centroids[i], seg_centroids[j])))
    inter_div = 1.0 - float(np.mean(inter_cs)) if inter_cs else 0.0

    def pct(x, q):
        return float(np.percentile(x, q))

    return {
        # 문장 간 의미적 연결성 (adjacent sentence similarity)
        "adj_sim_mean": float(np.mean(adj_sim)),  # 인접 문장 유사도의 평균
        "adj_sim_std": float(np.std(adj_sim)),  # 인접 문장 유사도의 표준편차
        "adj_sim_p10": pct(adj_sim, 10),  # 인접 문장 유사도의 10% 분위값
        "adj_sim_p50": pct(adj_sim, 50),  # 인접 문장 유사도의 중앙값
        "adj_sim_p90": pct(adj_sim, 90),  # 인접 문장 유사도의 90% 분위값
        "adj_sim_frac_high": float(np.mean(adj_sim >= high_thr)),  # 높은 유사도(>=high_thr) 비율
        "adj_sim_frac_low": float(np.mean(adj_sim <= low_thr)),  # 낮은 유사도(<=low_thr) 비율
        # 주제 전환 / 전반적 일관성 관련
        "topic_path_len": topic_path_len,  # 인접 문장 간 의미 거리 누적 길이 (길수록 주제 전환 많음)
        "dist_to_centroid_mean": float(np.mean(dist_to_centroid)),  # 전체 문장 중심과의 평균 거리
        "dist_to_centroid_std": float(np.std(dist_to_centroid)),  # 전체 문장 중심과의 거리 분산
        "coherence_score": float(1.0 - np.mean(dist_to_centroid)),  # 문서 전반의 의미적 응집도 (높을수록 일관성 높음)
        # 구간 내부/간 응집도
        "intra_coh": float(np.mean(intra_list)),  # 각 구간(앞/중/끝) 내부의 평균 응집도
        "inter_div": inter_div,  # 구간 간 중심 벡터의 다양성 (높을수록 주제 차이가 큼)
    }


# ---------------------------
# add_sementic_features.py에서 재사용하는 함수
# ---------------------------
def extract_semantic_features(
    script_or_json: Union[str, dict],
    *,
    model: Optional[SentenceTransformer] = None,
    model_name: str = "snunlp/KR-SBERT-V40K-klueNLI-augSTS",
    high_thr: float = 0.85,
    low_thr: float = 0.50,
    prefix: Optional[str] = None,
) -> Dict[str, float]:
    """
    Parameters
    ----------
    script_or_json : str | dict
        - str 경로인 경우:
            - .json이면 JSON을 열어 `script` 필드를 읽음
            - .txt면 파일 전체를 텍스트로 사용
            - 그 외 확장자는 "텍스트 그 자체"로 간주
        - dict인 경우: dict.get("script")를 우선 사용, 없으면 예외
    model : SentenceTransformer | None
        외부에서 이미 로드한 모델을 전달 가능. None이면 model_name으로 내부 로드.
    model_name : str
        model이 None일 때 사용할 모델 이름.
    high_thr, low_thr : float
        인접 문장 유사도 판단 임계값(상/하) — 지표 계산에 쓰임.
    prefix : Optional[str]
        결과 키에 접두사를 붙이고 싶을 때(예: "sem_"). None이면 원 키 그대로.

    Returns
    -------
    Dict[str, float]
        semantic 1D 피처 딕셔너리(필요시 prefix 적용).
    """
    # 입력 해석
    if isinstance(script_or_json, dict):
        script = script_or_json.get("script")
        if not isinstance(script, str) or not script.strip():
            raise ValueError("dict 입력에서 'script' 문자열을 찾지 못했습니다.")
    elif isinstance(script_or_json, str):
        path = script_or_json
        if os.path.isfile(path):
            ext = os.path.splitext(path)[1].lower()
            if ext == ".json":
                with open(path, "r", encoding="utf-8") as f:
                    data = json.load(f)
                script = data.get("script")
                if not isinstance(script, str) or not script.strip():
                    raise ValueError(f"JSON에 'script'가 없습니다: {path}")
            elif ext == ".txt":
                with open(path, "r", encoding="utf-8") as f:
                    script = f.read()
            else:
                # 파일이지만 지원 확장자가 아니면 텍스트로 간주해 그대로 로드
                with open(path, "r", encoding="utf-8") as f:
                    script = f.read()
        else:
            # 파일이 아니면 "텍스트 그 자체"로 사용
            script = path
    else:
        raise TypeError("script_or_json은 str(경로/텍스트) 또는 dict여야 합니다.")

    # 모델 준비
    m = model or SentenceTransformer(model_name)

    # 계산
    feats = _semantic_1d_features_from_script(
        script_text=script,
        model=m,
        high_thr=high_thr,
        low_thr=low_thr,
    )

    # 접두사 처리
    if prefix:
        feats = {f"{prefix}{k}": v for k, v in feats.items()}
    return feats


if __name__ == "__main__":
    import argparse

    ap = argparse.ArgumentParser(description="Extract semantic 1D features from script text or JSON(file).")
    ap.add_argument("input", help="텍스트 자체, .txt 파일, 또는 .json(안의 'script' 사용)")
    ap.add_argument("--model_name", default="snunlp/KR-SBERT-V40K-klueNLI-augSTS")
    ap.add_argument("--high_thr", type=float, default=0.85)
    ap.add_argument("--low_thr", type=float, default=0.50)
    ap.add_argument("--prefix", default=None, help="결과 키 접두사 (예: sem_)")
    args = ap.parse_args()

    feats = extract_semantic_features(
        args.input,
        model=None,
        model_name=args.model_name,
        high_thr=args.high_thr,
        low_thr=args.low_thr,
        prefix=args.prefix,
    )
    print(json.dumps(feats, ensure_ascii=False, indent=2))
