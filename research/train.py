import argparse
import glob
import json
import os
import warnings

import joblib
import numpy as np
import pandas as pd
from sklearn.metrics import accuracy_score, confusion_matrix, mean_absolute_error, mean_squared_error
from xgboost import XGBRegressor

warnings.filterwarnings("ignore")


# 등급을 숫자로 변환하는 맵
GRADE_MAP = {"A+": 8, "A0": 7, "B+": 6, "B0": 5, "C+": 4, "C0": 3, "D+": 2, "D0": 1}

# 사용할 피처 목록
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
TARGET_COLUMN = "eval_grade_num"


def load_and_preprocess_data(dataset_path, split):
    """
    데이터셋 경로에서 JSON 파일을 로드하고 전처리하여 DataFrame으로 반환합니다.
    :param dataset_path: 데이터셋의 루트 경로
    :param split: 'train' 또는 'valid'
    :return: 전처리된 데이터가 담긴 pandas DataFrame
    """
    data = []
    label_path = os.path.join(dataset_path, split, "label_test")
    json_files = glob.glob(os.path.join(label_path, "**", "*_presentation.json"), recursive=True)

    print(f"{split} 데이터 로딩 중... 총 {len(json_files)}개의 파일을 찾았습니다.")

    for file_path in json_files:
        try:
            with open(file_path, "r", encoding="utf-8") as f:
                label_data = json.load(f)

            word_cnt = label_data.get("word_cnt")
            if not word_cnt or word_cnt == 0:
                continue  # 단어 수가 0이면 비율 계산이 불가능하므로 건너뜁니다.

            # 실제 JSON 구조에 맞게 피처 추출 로직 수정
            features = {
                "repeat_cnt_ratio": label_data.get("repeat_cnt", 0) / word_cnt,
                "filler_words_cnt_ratio": label_data.get("filler_words_cnt", 0) / word_cnt,
                "pause_cnt_ratio": label_data.get("pause_0_5_cnt", 0) / word_cnt,
                "voc_speed": label_data.get("voc_speed"),
                "percent_silence": label_data.get("percent_silence"),
                "min_f0_hz": label_data.get("min_f0_hz"),
                "max_f0_hz": label_data.get("max_f0_hz"),
                "range_f0_hz": label_data.get("range_f0_hz"),
                "tot_slope_f0_st_per_s": label_data.get("tot_slope_f0_st_per_s"),
                "end_slope_f0_st_per_s": label_data.get("end_slope_f0_st_per_s"),
                "eval_grade_num": GRADE_MAP.get(label_data.get("eval_grade")),
                "word_speed": label_data.get("word_speed", 0),
                "avg_sentence_len": label_data.get("avg_sentence_len", 0),
                "adj_sim_mean": label_data.get("adj_sim_mean"),
                "adj_sim_std": label_data.get("adj_sim_std"),
                "adj_sim_p10": label_data.get("adj_sim_p10"),
                "adj_sim_p50": label_data.get("adj_sim_p50"),
                "adj_sim_p90": label_data.get("adj_sim_p90"),
                "adj_sim_frac_high": label_data.get("adj_sim_frac_high"),
                "adj_sim_frac_low": label_data.get("adj_sim_frac_low"),
                "topic_path_len": label_data.get("topic_path_len"),
                "dist_to_centroid_mean": label_data.get("dist_to_centroid_mean"),
                "dist_to_centroid_std": label_data.get("dist_to_centroid_std"),
                "coherence_score": label_data.get("coherence_score"),
                "intra_coh": label_data.get("intra_coh"),
                "inter_div": label_data.get("inter_div"),
            }

            # 모든 피처와 타겟이 유효한 값인지 확인
            if all(v is not None for v in features.values()):
                data.append(features)

        except json.JSONDecodeError:
            print(f"경고: {file_path} 파일이 비어있거나 잘못된 JSON 형식입니다.")
        except Exception as e:
            print(f"에러: {file_path} 처리 중 오류: {e}")

    df = pd.DataFrame(data)
    return df


def main():
    parser = argparse.ArgumentParser(description="발표 평가 등급(숫자) 예측 - XGBoost 회귀")
    parser.add_argument("--dataset_path", type=str, default="./dataset", help="데이터셋의 루트 경로")
    parser.add_argument("--model_output_path", type=str, default="./model.joblib", help="학습된 모델을 저장할 경로")
    args = parser.parse_args()

    # 데이터 로드
    train_df = load_and_preprocess_data(args.dataset_path, "train")
    valid_df = load_and_preprocess_data(args.dataset_path, "valid")

    if train_df.empty or valid_df.empty:
        print("오류: 학습 또는 검증 데이터가 비어있습니다. 데이터 경로와 파일 내용을 확인해주세요.")
        return

    # 피처/타겟 분리
    X_train = train_df[FEATURE_COLUMNS].copy()
    y_train = train_df[TARGET_COLUMN].copy()
    X_valid = valid_df[FEATURE_COLUMNS].copy()
    y_valid = valid_df[TARGET_COLUMN].copy()

    print(f"학습 데이터: {X_train.shape}, 검증 데이터: {X_valid.shape}")

    # XGBoost 회귀 모델 (고정 하이퍼파라미터 적용)
    # tree_method는 CPU: 'hist', GPU 사용 시 'gpu_hist'
    model = XGBRegressor(
        n_estimators=2731,
        max_depth=4,
        learning_rate=0.028522191973286638,
        subsample=0.742110376026309,
        colsample_bytree=0.7187056310787405,
        reg_lambda=0.6883344760395778,
        reg_alpha=0.7782595992848244,
        min_child_weight=3,
        gamma=0.44988621458172695,
        objective="reg:squarederror",
        random_state=42,
        eval_metric="rmse",
        tree_method="hist",
        n_jobs=-1,
    )
    # --- 불균형 보정: 등급별 가중치 계산 (inverse frequency with smoothing) ---
    counts = y_train.value_counts().sort_index()  # 등급별 개수
    max_cnt = counts.max()
    # 과보정 방지용 지수(alpha)와 클리핑 범위는 상황에 맞게 조절
    alpha = 0.5  # 0.5~1.0 권장 (0.5=루트 보정, 1.0=정확한 역비율)
    raw_weight = (max_cnt / counts) ** alpha
    # 평균 1로 정규화 + 과한 가중치 클리핑
    raw_weight = raw_weight / raw_weight.mean()
    weight_map = raw_weight.clip(lower=0.5, upper=3.0)  # 과보정 방지

    # 각 샘플 가중치 벡터
    w = y_train.map(weight_map).astype(float).values
    print("Class weights:", weight_map.to_dict())
    print("모델 학습을 시작합니다 (XGBoost Regressor,)...")
    model.fit(
        X_train,
        y_train,
        sample_weight=w,
    )
    print("모델 학습 완료")

    # 예측 및 평가
    print("모델 평가를 시작합니다...")
    y_pred = model.predict(X_valid)
    mse = mean_squared_error(y_valid, y_pred)
    rmse = np.sqrt(mse)
    mae = mean_absolute_error(y_valid, y_pred)

    # 회귀를 등급으로 변환: 반올림
    y_pred_rounded = np.clip(np.round(y_pred), 1, 8)

    print(f"검증 RMSE: {rmse:.4f}")
    print(f"검증 MAE : {mae:.4f}")

    def to_letter_grade(num):
        if num >= 7:  # A+:8, A0:7
            return "A"
        elif num >= 5:  # B+:6, B0:5
            return "B"
        elif num >= 3:  # C+:4, C0:3
            return "C"
        else:  # D+:2, D0:1
            return "D"

    # y_valid (정답)과 y_pred_rounded (예측) 둘 다 매핑
    y_true_letters = [to_letter_grade(v) for v in y_valid]
    y_pred_letters = [to_letter_grade(v) for v in y_pred_rounded]

    acc_abcd = accuracy_score(y_true_letters, y_pred_letters)
    print(f"A/B/C/D 그룹 Accuracy: {acc_abcd:.2%}")

    # --- Confusion Matrix 출력 ---
    labels = ["A", "B", "C", "D"]
    cm = confusion_matrix(y_true_letters, y_pred_letters, labels=labels)
    print("A/B/C/D Confusion Matrix:")
    print(cm)
    # 간단한 특성 중요도
    try:
        importances = model.feature_importances_
        imp_df = pd.DataFrame({"feature": FEATURE_COLUMNS, "importance": importances}).sort_values(
            "importance", ascending=False
        )
        print("\nTop feature importances:")
        for _, row in imp_df.head(10).iterrows():
            print(f"  {row['feature']:>28s}: {row['importance']:.4f}")
    except Exception:
        pass

    # 모델 저장
    joblib.dump(model, args.model_output_path)
    print(f"\n학습된 XGBoost 모델이 저장되었습니다: '{args.model_output_path}'")


if __name__ == "__main__":
    main()
