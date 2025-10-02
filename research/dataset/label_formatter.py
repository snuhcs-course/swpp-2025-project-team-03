import json
import os


def time_to_seconds(t: str) -> int:
    """HH:MM:SS 또는 MM:SS 문자열을 초 단위로 변환"""
    parts = list(map(int, t.split(":")))
    if len(parts) == 2:  # MM:SS
        m, s = parts
        return m * 60 + s
    elif len(parts) == 3:  # HH:MM:SS
        h, m, s = parts
        return h * 3600 + m * 60 + s
    else:
        raise ValueError(f"Invalid time format: {t}")


def convert_json(data: dict) -> dict:
    """원본 JSON dict → 변환된 dict"""
    # gender 매핑
    gender_map = {"여자": "female", "남자": "male"}
    gender = gender_map.get(data["speaker"]["gender"], data["speaker"]["gender"])

    # job 매핑
    job_map = {"고등학생": "high_school", "중학생": "middle_school"}
    job = job_map.get(data["speaker"]["job"], data["speaker"]["job"])

    # duration 계산
    start_sec = time_to_seconds(data["script"]["start_time"])
    end_sec = time_to_seconds(data["script"]["end_time"])
    duration_sec = end_sec - start_sec

    return {
        "age_flag": data["speaker"]["age_flag"],
        "gender": gender,
        "job": job,
        "aud_flag": data["speaker"]["aud_flag"],
        "start_time": data["script"]["start_time"],
        "end_time": data["script"]["end_time"],
        "duration_sec": duration_sec,
        "present_topic": data["presentation"]["presen_topic"],
        "present_type": data["presentation"]["presen_type"],
        "script": data["script"]["script_stt_txt"],
        "script_tag": data["script"]["script_tag_txt"],
        "syllable_cnt": data["script"]["syllable_cnt"],
        "word_cnt": data["script"]["word_cnt"],
        "sentence_cnt": data["script"]["sent_cnt"],
        "repeat_cnt": data["evaluations"][0]["repeat"]["repeat_cnt"],
        "filler_words_cnt": data["evaluations"][0]["filler_words"]["filler_words_cnt"],
        "pause_cnt": data["evaluations"][0]["pause"]["pause_cnt"],
        "wrong_cnt": data["evaluations"][0]["wrong"]["wrong_cnt"],
        "voc_speed": data["evaluations"][0]["voice_speed"]["voc_speed"],
        "eval_grade": data["average"]["eval_grade"],
    }


def batch_convert(input_root="label_origin", output_root="label"):
    for dirpath, _, filenames in os.walk(input_root):
        for filename in filenames:
            if filename.lower().endswith(".json"):
                in_path = os.path.join(dirpath, filename)

                # 출력 경로: input_root 부분을 output_root로 바꿔줌
                rel_path = os.path.relpath(in_path, input_root)
                out_path = os.path.join(output_root, rel_path)

                os.makedirs(os.path.dirname(out_path), exist_ok=True)

                try:
                    with open(in_path, "r", encoding="utf-8") as f:
                        data = json.load(f)

                    new_data = convert_json(data)

                    with open(out_path, "w", encoding="utf-8") as f:
                        json.dump(new_data, f, ensure_ascii=False, indent=2)

                    print(f"✅ Converted: {in_path} -> {out_path}")
                except Exception as e:
                    print(f"❌ Failed: {in_path} ({e})")


if __name__ == "__main__":
    batch_convert("label_origin", "label_trim")
