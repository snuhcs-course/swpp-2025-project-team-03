# infer_xgb.py
import argparse
import glob
import json
import os
import warnings

import joblib
import numpy as np
import pandas as pd

warnings.filterwarnings("ignore")

# 훈련 때 사용한 FEATURE_COLUMNS와 동일하게 유지
FEATURE_COLUMNS = [
    "repeat_cnt_ratio",
    "filler_words_cnt_ratio",
    "pause_cnt_ratio",
    "voc_speed",
    "percent_silence",
    "min_f0_hz",
    "max_f0_hz",
    "range_f0_hz",
    "tot_slope_f0_st_per_s",
    "end_slope_f0_st_per_s",
    "word_speed",
    "avg_sentence_len",
    "adj_sim_mean",
    "adj_sim_std",
    "adj_sim_p10",
    "adj_sim_p50",
    "adj_sim_p90",
    "adj_sim_frac_high",
    "adj_sim_frac_low",
    "topic_path_len",
    "dist_to_centroid_mean",
    "dist_to_centroid_std",
    "coherence_score",
    "intra_coh",
    "inter_div",
]


def to_letter_grade(num):
    # 학습 스크립트와 동일한 매핑 (A: 7~8, B: 5~6, C: 3~4, D: 1~2)
    if num >= 7:
        return "A"
    elif num >= 5:
        return "B"
    elif num >= 3:
        return "C"
    else:
        return "D"


def build_feature_row(js: dict):
    """
    훈련 시 전처리 규칙과 동일하게 JSON에서 피처를 추출해 dict 반환
    - *_ratio 항목은 word_cnt로 나눠서 계산
    - 없거나 0인 경우/키 누락은 합리적으로 처리
    """
    word_cnt = js.get("word_cnt", 0) or 0

    # 0 division 방지: 분모가 0이면 0.0으로
    def safe_ratio(n, d):
        if d is None or d == 0 or n is None:
            return 0.0
        return float(n) / float(d)

    row = {
        "repeat_cnt_ratio": safe_ratio(js.get("repeat_cnt", 0), word_cnt),
        "filler_words_cnt_ratio": safe_ratio(js.get("filler_words_cnt", 0), word_cnt),
        "pause_cnt_ratio": safe_ratio(js.get("pause_0_5_cnt", 0), word_cnt),
        "voc_speed": js.get("voc_speed", None),
        "percent_silence": js.get("percent_silence", None),
        "min_f0_hz": js.get("min_f0_hz", None),
        "max_f0_hz": js.get("max_f0_hz", None),
        "range_f0_hz": js.get("range_f0_hz", None),
        "tot_slope_f0_st_per_s": js.get("tot_slope_f0_st_per_s", None),
        "end_slope_f0_st_per_s": js.get("end_slope_f0_st_per_s", None),
        "word_speed": js.get("word_speed", 0),
        "avg_sentence_len": js.get("avg_sentence_len", 0),
        "adj_sim_mean": js.get("adj_sim_mean", None),
        "adj_sim_std": js.get("adj_sim_std", None),
        "adj_sim_p10": js.get("adj_sim_p10", None),
        "adj_sim_p50": js.get("adj_sim_p50", None),
        "adj_sim_p90": js.get("adj_sim_p90", None),
        "adj_sim_frac_high": js.get("adj_sim_frac_high", None),
        "adj_sim_frac_low": js.get("adj_sim_frac_low", None),
        "topic_path_len": js.get("topic_path_len", None),
        "dist_to_centroid_mean": js.get("dist_to_centroid_mean", None),
        "dist_to_centroid_std": js.get("dist_to_centroid_std", None),
        "coherence_score": js.get("coherence_score", None),
        "intra_coh": js.get("intra_coh", None),
        "inter_div": js.get("inter_div", None),
    }

    # None이 남아있으면 0으로 채움(훈련 데이터가 모두 유효값만 사용됐다면,
    # 실사용에서 값이 빠진 경우 0 대체가 가장 안전한 기본값)
    for k, v in row.items():
        if row[k] is None:
            row[k] = 0.0

    return row


def main():
    ap = argparse.ArgumentParser(description="XGBoost 회귀 모델 inference (JSON 파일 단일/배치)")
    ap.add_argument(
        "--model_path", type=str, required=True, help="joblib로 저장된 XGBoost 모델 경로 (ex: model_xgb.joblib)"
    )
    ap.add_argument(
        "--json_path",
        type=str,
        required=True,
        help="JSON 파일 경로 또는 글롭 패턴 (ex: sample.json, './**/*_presentation.json')",
    )
    ap.add_argument("--out_csv", type=str, default=None, help="배치 처리 결과를 저장할 CSV 경로(옵션)")
    args = ap.parse_args()

    # 모델 로드
    model = joblib.load(args.model_path)

    # 입력 파일 목록 (단일 경로 또는 글롭)
    if any(ch in args.json_path for ch in ["*", "?", "["]):
        files = sorted(glob.glob(args.json_path, recursive=True))
    else:
        files = [args.json_path]

    if not files:
        print("입력 JSON 파일을 찾지 못했습니다.")
        return

    rows = []
    for fp in files:
        try:
            with open(fp, "r", encoding="utf-8") as f:
                js = json.load(f)
        except Exception as e:
            print(f"[WARN] JSON 로드 실패: {fp} ({e})")
            continue

        feat_row = build_feature_row(js)
        X = pd.DataFrame([feat_row], columns=FEATURE_COLUMNS)

        # 예측 (연속 등급)
        y_pred = model.predict(X)[0]
        # 1~8로 반올림 & 클리핑
        y_round = int(np.clip(np.round(y_pred), 1, 8))
        y_letter = to_letter_grade(y_round)

        rows.append(
            {
                "file": fp,
                "pred_cont": float(y_pred),
                "pred_rounded": y_round,
                "pred_letter": y_letter,
            }
        )

        print(f"{os.path.basename(fp)} -> pred_cont={y_pred:.4f}, pred_rounded={y_round}, letter={y_letter}")

    if args.out_csv and rows:
        df_out = pd.DataFrame(rows)
        df_out.to_csv(args.out_csv, index=False, encoding="utf-8-sig")
        print(f"\n저장 완료: {args.out_csv}")


if __name__ == "__main__":
    main()
