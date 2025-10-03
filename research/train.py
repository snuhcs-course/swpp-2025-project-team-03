import argparse
import glob
import json
import os

import joblib
import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import accuracy_score, mean_squared_error

# 등급을 숫자로 변환하는 맵
GRADE_MAP = {"A+": 8, "A0": 7, "B+": 6, "B0": 5, "C+": 4, "C0": 3, "D+": 2, "D0": 1}

# 사용할 피처 목록
FEATURE_COLUMNS = [
    "repeat_cnt_ratio",
    "filler_words_cnt_ratio",
    "pause_cnt_ratio",
    "wrong_cnt_ratio",
    "voc_speed",
    "percent_silence",
    "min_f0_hz",
    "max_f0_hz",
    "range_f0_hz",
    "tot_slope_f0_st_per_s",
    "end_slope_f0_st_per_s",
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
    label_path = os.path.join(dataset_path, split, "label")
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
                "pause_cnt_ratio": label_data.get("pause_cnt", 0) / word_cnt,
                "wrong_cnt_ratio": label_data.get("wrong_cnt", 0) / word_cnt,
                "voc_speed": label_data.get("voc_speed"),
                "percent_silence": label_data.get("percent_silence"),
                "min_f0_hz": label_data.get("min_f0_hz"),
                "max_f0_hz": label_data.get("max_f0_hz"),
                "range_f0_hz": label_data.get("range_f0_hz"),
                "tot_slope_f0_st_per_s": label_data.get("tot_slope_f0_st_per_s"),
                "end_slope_f0_st_per_s": label_data.get("end_slope_f0_st_per_s"),
                "eval_grade_num": GRADE_MAP.get(label_data.get("eval_grade")),
            }

            # 모든 피처가 유효한 값인지 확인
            if all(value is not None for value in features.values()):
                data.append(features)

        except json.JSONDecodeError:
            print(f"경고: {file_path} 파일이 비어있거나 잘못된 JSON 형식입니다.")
        except Exception as e:
            print(f"에러: {file_path} 파일을 처리하는 중 오류 발생: {e}")

    return pd.DataFrame(data)


def main():
    """
    메인 학습 로직을 실행합니다.
    """
    parser = argparse.ArgumentParser(description="발표 평가 등급 예측 모델을 학습합니다.")
    parser.add_argument(
        "--dataset_path", type=str, default="C:\\Users\\suhan\\public_speech\\dataset", help="데이터셋의 루트 경로"
    )
    parser.add_argument("--model_output_path", type=str, default="./model.joblib", help="학습된 모델을 저장할 경로")
    args = parser.parse_args()

    # 데이터 로드
    train_df = load_and_preprocess_data(args.dataset_path, "train")
    valid_df = load_and_preprocess_data(args.dataset_path, "valid")

    if train_df.empty or valid_df.empty:
        print("오류: 학습 또는 검증 데이터가 비어있습니다. 데이터 경로와 파일 내용을 확인해주세요.")
        return

    # 피처와 타겟 분리
    X_train = train_df[FEATURE_COLUMNS]
    y_train = train_df[TARGET_COLUMN]
    X_valid = valid_df[FEATURE_COLUMNS]
    y_valid = valid_df[TARGET_COLUMN]

    print(f"학습 데이터: {X_train.shape}, 검증 데이터: {X_valid.shape}")

    # 모델 생성 및 학습
    print("모델 학습을 시작합니다...")
    model = RandomForestRegressor(n_estimators=100, random_state=42, oob_score=True)
    model.fit(X_train, y_train)
    print("모델 학습 완료.")

    # 모델 평가
    print("모델 평가를 시작합니다...")
    y_pred = model.predict(X_valid)
    mse = mean_squared_error(y_valid, y_pred)
    rmse = np.sqrt(mse)

    print(f"검증 데이터셋 Mean Squared Error (MSE): {mse:.4f}")
    print(f"검증 데이터셋 Root Mean Squared Error (RMSE): {rmse:.4f}")
    print(f"모델 Out-of-Bag (OOB) Score: {model.oob_score_:.4f}")

    # 정확도 계산 (회귀 결과를 반올림하여 가장 가까운 정수 등급으로 변환)
    y_pred_rounded = np.round(y_pred)
    accuracy = accuracy_score(y_valid, y_pred_rounded)
    print(f"검증 데이터셋 정확도 (Accuracy, 반올림 기준): {accuracy:.2%}")

    # 모델 저장
    joblib.dump(model, args.model_output_path)
    print(f"학습된 모델이 '{args.model_output_path}'에 저장되었습니다.")
    print("\nInference Logic is Coming Soon!!!\n")


if __name__ == "__main__":
    main()
