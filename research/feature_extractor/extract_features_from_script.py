"""
extract_features_from_script.py
- 단일 JSON 파일에 대해:
  * "script" 텍스트로부터 간투어(필러) 지표 (룰/퍼지 + 선택적 SBERT 보조)
  * "script" 텍스트로부터 의미 근접 중복(near-duplicate) 지표 (SBERT + 클러스터 k-1)
  * "script" 문장 단위 semantic 1D 피처 (인접 유사도/일관성/주제 경로 등)
- 계산된 feature들을 동일 JSON 파일에 in-place로 병합 저장

외부 사용:
    from extract_features_from_script import enrich_json_file
    enrich_json_file("path/to/file.json", prefix="sem_", use_embeddings=True, add_semantic_1d=True)

CLI 예시:
    python extract_features_from_script.py --json_path path/to/file.json --prefix sem_ --use_embeddings \
        --emb_thr 0.80 --thr 0.88 --add_semantic_1d --sem_high_thr 0.85 --sem_low_thr 0.50
"""

import argparse
import difflib
import json
import os
import re
import unicodedata
from typing import Dict, List, Optional, Set, Tuple, Union

import numpy as np

try:
    from sentence_transformers import SentenceTransformer
except Exception:
    SentenceTransformer = None  # 임베딩 보조/near-dup/semantic 1D를 쓰지 않으면 없어도 동작

# =========================================================
# 공통 토크나이저 & 전처리
# =========================================================
_TOKEN_RE = re.compile(r"[가-힣A-Za-z0-9]+")
RE_ELONG = re.compile(r"([가-힣A-Za-z])\1{2,}")  # 같은 글자 3+ 반복 → 2로 축약
RE_DOTS = re.compile(r"\.{2,}")  # 점 2+ → "…"
RE_SPACE = re.compile(r"\s+")


def normalize_text_basic(t: str) -> str:
    t = RE_ELONG.sub(r"\1\1", t)
    t = RE_DOTS.sub("…", t)
    t = RE_SPACE.sub(" ", t.strip())
    return t


def tokenize_ko(t: str) -> List[str]:
    return _TOKEN_RE.findall(t)


# === 카운트 헬퍼 ===
_HANGUL_SYL_RE = re.compile(r"[가-힣]")  # 한글 음절 블록 (가~힣)


def count_syllables_ko(text: str) -> int:
    """
    매우 단순한 근사: 한글 '음절 블록' 개수를 세어 음절 수로 사용.
    (영문/숫자는 음절 계산에서 제외; 필요시 규칙 확장 가능)
    """
    return len(_HANGUL_SYL_RE.findall(text))


def count_words_ko(text: str) -> int:
    """토큰 개수 = 단어 수(근사)"""
    return len(tokenize_ko(normalize_text_basic(text)))


def count_sentences_ko(text: str) -> int:
    """간단 문장 분할 규칙 기반 문장 수"""
    parts = re.split(r"[\.!\?\n…]+", unicodedata.normalize("NFC", text))
    return sum(1 for p in parts if p.strip())


# =========================================================
# (A) 필러(간투어) 지표 — 룰/퍼지 + 선택적 임베딩 보조
# =========================================================
FILLER_BASE = {
    "어",
    "음",
    "에",
    "그",
    "아",
    "음흠",
    "그니까",
    "그러니까",
    "그런데",
    "그런가",
    "그러면",
    "뭔가",
    "뭐랄까",
    "약간",
    "일단",
    "아니",
    "그리고",
    "어쩌면",
    "음…",
    "어…",
}

FILLER_MULTI = {
    "그 뭐지",
    "그 뭐냐",
    "뭐라고 해야",
    "뭐라 해야",
    "뭐였더라",
    "이제 뭐",
    "어 뭐지",
    "아 뭐지",
    "그거 뭐지",
}


def fuzzy_in_lexicon(tok: str, lex: set, ratio_thr: float = 0.86) -> bool:
    for w in lex:
        if difflib.SequenceMatcher(None, tok, w).ratio() >= ratio_thr:
            return True
    return False


def find_multiword_fillers(text: str, multi: set) -> int:
    norm = normalize_text_basic(text)
    count = 0
    for phrase in multi:
        # "그 뭐지" → r"\b그\s*뭐지\b" (공백 유연 매칭)
        pat = r"\b" + r"\s*".join(map(re.escape, phrase.split())) + r"\b"
        count += len(re.findall(pat, norm))
    return count


def rule_fuzzy_fillers(
    text: str, base: set = FILLER_BASE, multi: set = FILLER_MULTI, token_ratio_thr: float = 0.86
) -> Dict[str, int]:
    norm = normalize_text_basic(text)
    toks = tokenize_ko(norm)

    single_cnt = 0
    for tk in toks:
        if tk in base or fuzzy_in_lexicon(tk, base, ratio_thr=token_ratio_thr):
            single_cnt += 1

    multi_cnt = find_multiword_fillers(text, multi)  # 원문으로 탐색
    return {"filler_single_cnt": single_cnt, "filler_multi_cnt": multi_cnt, "filler_words_cnt": single_cnt + multi_cnt}


DEFAULT_SEED_PHRASES = [
    "어",
    "음",
    "에",
    "그",
    "아",
    "그니까",
    "그러니까",
    "뭐랄까",
    "약간",
    "일단",
    "그 뭐지",
    "어 뭐지",
    "뭐라고 해야",
]


def sbert_helper_load(model_name: str) -> Optional[SentenceTransformer]:
    if SentenceTransformer is None:
        return None
    try:
        return SentenceTransformer(model_name)
    except Exception:
        return None


def embeddings_boost_count(
    text: str, model: Optional[SentenceTransformer], seeds: List[str], emb_thr: float = 0.78, ngram_max: int = 2
) -> int:
    if model is None:
        return 0
    norm = normalize_text_basic(text)
    toks = tokenize_ko(norm)

    spans = []
    for n in range(1, ngram_max + 1):
        for i in range(len(toks) - n + 1):
            s = " ".join(toks[i : i + n])
            if 2 <= len(s) <= 8:
                spans.append(s)
    if not spans:
        return 0

    seed_embs = model.encode(seeds, normalize_embeddings=True)
    proto = np.mean(seed_embs, axis=0)
    proto /= np.linalg.norm(proto) + 1e-12

    span_embs = model.encode(spans, normalize_embeddings=True)
    span_embs = np.asarray(span_embs, dtype=np.float32)
    sims = span_embs @ proto  # cosine
    return int(np.sum(sims >= emb_thr))


# =========================================================
# (B) 의미 근접 중복(near-dup) — SBERT 임베딩 + 클러스터(k-1)
# =========================================================
_STOP_NEARDUP_DEFAULT: Set[str] = {
    # 필러/기능어는 near-dup에서 기본 제외
    "어",
    "음",
    "에",
    "그",
    "아",
    "그니까",
    "그러니까",
    "뭐랄까",
    "약간",
    "일단",
    "그 뭐지",
    "어 뭐지",
    "뭐라고 해야",
}


class _DSU:
    def __init__(self, n: int):
        self.p = list(range(n))
        self.r = [0] * n

    def find(self, x: int) -> int:
        while self.p[x] != x:
            self.p[x] = self.p[self.p[x]]
            x = self.p[x]
        return x

    def union(self, a: int, b: int) -> bool:
        pa, pb = self.find(a), self.find(b)
        if pa == pb:
            return False
        if self.r[pa] < self.r[pb]:
            pa, pb = pb, pa
        self.p[pb] = pa
        if self.r[pa] == self.r[pb]:
            self.r[pa] += 1
        return True


def _make_spans(
    tokens: List[str], n_min: int = 1, n_max: int = 2, min_char_len: int = 2, skip_words: Set[str] = None
) -> List[Tuple[str, Tuple[int, int]]]:
    skip_words = skip_words or set()
    spans: List[Tuple[str, Tuple[int, int]]] = []
    L = len(tokens)
    for n in range(n_min, n_max + 1):
        for i in range(L - n + 1):
            if any(t in skip_words for t in tokens[i : i + n]):
                continue
            s = " ".join(tokens[i : i + n])
            if len(s) < min_char_len:
                continue
            spans.append((s, (i, i + n)))
    return spans


def semantic_near_dup_count(
    text: str,
    model: Optional[SentenceTransformer],
    *,
    thr: float = 0.85,
    window: int = 3,
    ngram_max: int = 2,
    return_pairs: bool = False,
    min_char_len: int = 2,
    skip_words: Set[str] = _STOP_NEARDUP_DEFAULT,
) -> Dict[str, object]:
    tokens = tokenize_ko(text)
    spans = _make_spans(tokens, 1, ngram_max, min_char_len=min_char_len, skip_words=skip_words)
    if not spans or model is None:
        # model이 없으면 near-dup 계산 생략(0으로)
        return {"sem_near_dup_cnt": 0, "sem_near_dup_pairs": []} if return_pairs else {"sem_near_dup_cnt": 0}

    span_texts = [s for s, _ in spans]
    embs = model.encode(span_texts, convert_to_tensor=False, normalize_embeddings=True)
    embs = np.asarray(embs, dtype=np.float32)

    n = len(spans)
    dsu = _DSU(n)
    kept_pairs = []

    for i in range(n):
        j_end = min(i + 1 + window, n)
        for j in range(i + 1, j_end):
            (li, ri), (lj, rj) = spans[i][1], spans[j][1]
            # 겹치는 구간은 제외 (완전/부분 포함 모두)
            if lj < ri and li < rj:
                continue
            sim = float((embs[i] * embs[j]).sum())  # cosine (정규화 가정)
            if sim >= thr:
                dsu.union(i, j)
                if return_pairs:
                    kept_pairs.append(
                        {
                            "i": i,
                            "j": j,
                            "span_i": (spans[i][0], spans[i][1]),
                            "span_j": (spans[j][0], spans[j][1]),
                            "sim": round(sim, 3),
                        }
                    )

    comp_size: Dict[int, int] = {}
    for i in range(n):
        root = dsu.find(i)
        comp_size[root] = comp_size.get(root, 0) + 1

    events = sum(max(0, sz - 1) for sz in comp_size.values())
    out = {"sem_near_dup_cnt": int(events)}
    if return_pairs:
        out["sem_near_dup_pairs"] = kept_pairs
        out["spans"] = spans
    return out


# =========================================================
# (C) 문장 단위 semantic 1D 피처
# =========================================================
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

    embs = model.encode(sents, normalize_embeddings=True)
    embs = np.asarray(embs)  # (T, D)

    adj_sim = np.sum(embs[:-1] * embs[1:], axis=1)
    adj_cos = np.clip(adj_sim, -1.0, 1.0)

    adj_dist = np.sqrt(np.maximum(0.0, 2.0 - 2.0 * adj_cos))
    topic_path_len = float(np.sum(adj_dist))

    centroid = embs.mean(axis=0)
    centroid = centroid / (np.linalg.norm(centroid) + 1e-12)
    dist_to_centroid = np.sqrt(np.maximum(0.0, 2.0 - 2.0 * np.clip(embs @ centroid, -1.0, 1.0)))

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
            # 자기 자신 제외 평균 유사도
            cs = seg @ seg.T
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
        "adj_sim_mean": float(np.mean(adj_sim)),
        "adj_sim_std": float(np.std(adj_sim)),
        "adj_sim_p10": pct(adj_sim, 10),
        "adj_sim_p50": pct(adj_sim, 50),
        "adj_sim_p90": pct(adj_sim, 90),
        "adj_sim_frac_high": float(np.mean(adj_sim >= high_thr)),
        "adj_sim_frac_low": float(np.mean(adj_sim <= low_thr)),
        "topic_path_len": topic_path_len,
        "dist_to_centroid_mean": float(np.mean(dist_to_centroid)),
        "dist_to_centroid_std": float(np.std(dist_to_centroid)),
        "coherence_score": float(1.0 - np.mean(dist_to_centroid)),
        "intra_coh": float(np.mean(intra_list)),
        "inter_div": inter_div,
    }


def extract_semantic_features(
    script_or_json: Union[str, dict],
    *,
    model: Optional[SentenceTransformer] = None,
    model_name: str = "snunlp/KR-SBERT-V40K-klueNLI-augSTS",
    high_thr: float = 0.85,
    low_thr: float = 0.50,
    prefix: Optional[str] = None,
) -> Dict[str, float]:
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
            else:
                with open(path, "r", encoding="utf-8") as f:
                    script = f.read()
        else:
            script = path
    else:
        raise TypeError("script_or_json은 str(경로/텍스트) 또는 dict여야 합니다.")

    m = model or (SentenceTransformer(model_name) if SentenceTransformer is not None else None)
    if m is None:
        raise RuntimeError("SentenceTransformer 사용 불가: 패키지 미설치 또는 로드 실패")

    feats = _semantic_1d_features_from_script(
        script_text=script,
        model=m,
        high_thr=high_thr,
        low_thr=low_thr,
    )
    if prefix:
        feats = {f"{prefix}{k}": v for k, v in feats.items()}
    return feats


# =========================================================
# (D) 단일 JSON 파일: feature 계산 & 저장 (외부 import용)
# =========================================================
def enrich_json_file(
    json_path: str,
    *,
    prefix: str = "",
    save_backup: bool = True,
    # 필러(룰/퍼지 + 임베딩 보조)
    token_ratio_thr: float = 0.86,
    use_embeddings: bool = False,
    embed_model_name: str = "snunlp/KR-SBERT-V40K-klueNLI-augSTS",
    emb_thr: float = 0.78,
    # near-dup (SBERT + k-1)
    neardup_thr: float = 0.85,
    neardup_window: int = 6,
    neardup_ngram_max: int = 2,
    neardup_min_char_len: int = 2,
    neardup_skip_fillers: bool = True,
    store_pairs: bool = False,
    # semantic 1D
    add_semantic_1d: bool = True,
    sem_high_thr: float = 0.85,
    sem_low_thr: float = 0.50,
    shared_model: Optional[SentenceTransformer] = None,
) -> Dict[str, object]:
    """
    단일 JSON 파일을 열어 feature 계산 후 in-place로 저장.
    반환: 저장된 키/값을 포함한 dict (요약)
    """
    add_semantic_1d = True
    if not os.path.isfile(json_path):
        raise FileNotFoundError(f"JSON file not found: {json_path}")

    with open(json_path, "r", encoding="utf-8") as f:
        data = json.load(f)
    orig_data = dict(data)

    script = data.get("script")
    if not isinstance(script, str) or not script.strip():
        raise ValueError(f"'script' not found or empty in: {json_path}")
    total_length = data.get("total_length")
    if not isinstance(total_length, (int, float)) or total_length <= 0:
        raise ValueError(f"'total_length' not found or invalid in: {json_path}")
    # --- 기본 카운트(문자/단어/문장) 추가 ---
    syllable_cnt = count_syllables_ko(script)
    word_cnt_calc = count_words_ko(script)
    sentence_cnt = count_sentences_ko(script)

    # prefix 없이 '고정 키' 이름으로 저장 (데이터셋 표준 키로 가정)
    data["syllable_cnt"] = int(syllable_cnt)
    data["word_cnt"] = int(word_cnt_calc)
    data["sentence_cnt"] = int(sentence_cnt)

    data["voc_speed"] = float(syllable_cnt) / float(total_length)  # 음절 속도 (syllables/sec)
    data["word_speed"] = float(word_cnt_calc) / float(total_length)  # 단어 속도 (words/sec)
    data["avg_word_len"] = (
        float(syllable_cnt) / float(word_cnt_calc) if word_cnt_calc > 0 else 0.0
    )  # 평균 단어 길이 (음절/단어)
    data["avg_sentence_len"] = (
        float(word_cnt_calc) / float(sentence_cnt) if sentence_cnt > 0 else 0.0
    )  # 평균 문장 길이 (단어/문장)
    # (1) 필러 지표
    rule_counts = rule_fuzzy_fillers(script, token_ratio_thr=token_ratio_thr)

    embed_model = None
    if use_embeddings:
        embed_model = shared_model if shared_model is not None else sbert_helper_load(embed_model_name)
        if embed_model is None:
            print("[WARN] SBERT을 로드하지 못해 임베딩 보조/near-dup/semantic-1D 일부가 제한됩니다.")
            use_embeddings = False

    filler_extra = (
        embeddings_boost_count(script, embed_model, DEFAULT_SEED_PHRASES, emb_thr=emb_thr) if use_embeddings else 0
    )
    filler_total = rule_counts["filler_words_cnt"] + filler_extra

    def put(k: str, v):
        data[(prefix + k) if prefix else k] = v

    put("filler_words_cnt", int(filler_total))
    put("filler_single_cnt", int(rule_counts["filler_single_cnt"]))
    put("filler_multi_cnt", int(rule_counts["filler_multi_cnt"]))
    if use_embeddings:
        put("filler_embed_boost_cnt", int(filler_extra))

    # (2) 의미 근접 중복(near-dup)
    neardup_model = embed_model if embed_model is not None else shared_model
    if neardup_model is None and SentenceTransformer is not None:
        try:
            neardup_model = SentenceTransformer(embed_model_name)
        except Exception:
            neardup_model = None

    neardup_result = semantic_near_dup_count(
        script,
        model=neardup_model,
        thr=neardup_thr,
        window=neardup_window,
        ngram_max=neardup_ngram_max,
        return_pairs=store_pairs,
        min_char_len=neardup_min_char_len,
        skip_words=(_STOP_NEARDUP_DEFAULT if neardup_skip_fillers else set()),
    )
    put("repeat_cnt", int(neardup_result["sem_near_dup_cnt"]))
    if store_pairs and "sem_near_dup_pairs" in neardup_result:
        put("near_dup_pairs", neardup_result["sem_near_dup_pairs"])

    # (3) semantic 1D
    if add_semantic_1d:
        sem_model = neardup_model if neardup_model is not None else shared_model
        if sem_model is None and SentenceTransformer is not None:
            try:
                sem_model = SentenceTransformer(embed_model_name)
            except Exception:
                sem_model = None
        if sem_model is not None:
            sem_feats = _semantic_1d_features_from_script(
                script_text=script, model=sem_model, high_thr=sem_high_thr, low_thr=sem_low_thr
            )
            # 접두사 적용: prefix + key
            for k, v in sem_feats.items():
                put(k, v)
        else:
            print("[WARN] semantic 1D 미계산: SentenceTransformer 로드 실패")

    # 백업 & 저장
    if save_backup:
        with open(json_path + ".bak", "w", encoding="utf-8") as bf:
            json.dump(orig_data, bf, ensure_ascii=False, indent=2)

    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

    summary = {
        "json_path": json_path,
        "saved_keys": [k for k in data.keys() if k not in orig_data],
        "filler_words_cnt": data.get((prefix + "filler_words_cnt") if prefix else "filler_words_cnt"),
        "repeat_cnt": data.get((prefix + "repeat_cnt") if prefix else "repeat_cnt"),
    }
    return summary


def main():
    ap = argparse.ArgumentParser(
        description="JSON(script/total_length)을 읽어 필러/near-dup/semantic-1D feature를 같은 파일에 저장"
    )
    ap.add_argument("--json_path", type=str, required=True, help="단일 JSON 파일 경로")
    ap.add_argument("--prefix", type=str, default="", help="저장 키 접두사 (예: sem_)")
    ap.add_argument("--backup", action="store_true", help="저장 전 .bak 백업 생성")

    # filler parameter
    ap.add_argument("--token_ratio_thr", type=float, default=0.86, help="필러 퍼지 매칭 임계값")
    ap.add_argument("--use_embeddings", action="store_true", help="필러 임베딩 보조/near-dup/semantic-1D SBERT 사용")
    ap.add_argument("--model_name", type=str, default="snunlp/KR-SBERT-V40K-klueNLI-augSTS", help="SBERT 모델명")
    ap.add_argument("--emb_thr", type=float, default=0.78, help="필러 임베딩 보조 임계값")

    # repeat 파라미터
    ap.add_argument("--thr", type=float, default=0.85, help="near-dup 코사인 유사도 임계값")
    ap.add_argument("--window", type=int, default=3, help="near-dup 비교 윈도우")
    ap.add_argument("--ngram_max", type=int, default=2, help="near-dup 구절 최대 어절 수")
    ap.add_argument("--min_char_len", type=int, default=2, help="near-dup 스팬 최소 글자 길이")
    ap.add_argument("--no_skip_fillers", action="store_true", help="near-dup에서 필러/기능어를 제외하지 않음")
    ap.add_argument("--store_pairs", action="store_true", help="near-dup 매칭 쌍까지 저장(파일 커질 수 있음)")

    # semantic  파라미터
    ap.add_argument("--add_semantic_1d", action="store_true", help="semantic 1D 피처를 추가로 계산/저장")
    ap.add_argument("--sem_high_thr", type=float, default=0.85, help="semantic 1D: 인접 유사도 high 임계")
    ap.add_argument("--sem_low_thr", type=float, default=0.50, help="semantic 1D: 인접 유사도 low 임계")

    args = ap.parse_args()

    summary = enrich_json_file(
        json_path=args.json_path,
        prefix=args.prefix,
        save_backup=args.backup,
        # fillers
        token_ratio_thr=args.token_ratio_thr,
        use_embeddings=args.use_embeddings,
        embed_model_name=args.model_name,
        emb_thr=args.emb_thr,
        # near-dup
        neardup_thr=args.thr,
        neardup_window=args.window,
        neardup_ngram_max=args.ngram_max,
        neardup_min_char_len=args.min_char_len,
        neardup_skip_fillers=(not args.no_skip_fillers),
        store_pairs=args.store_pairs,
        # semantic 1D
        add_semantic_1d=args.add_semantic_1d,
        sem_high_thr=args.sem_high_thr,
        sem_low_thr=args.sem_low_thr,
    )
    print(f"[DONE] {summary}")


if __name__ == "__main__":
    main()
