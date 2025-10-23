#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import subprocess
import sys
import time
from datetime import datetime

from flask import Flask, jsonify, request
from flask_cors import CORS

# 상위 디렉토리의 모듈들 import
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from google_stt import speech_to_text_with_timestamps

app = Flask(__name__)
CORS(app)  # CORS 허용

# 업로드 폴더 설정
UPLOAD_FOLDER = "uploads"
if not os.path.exists(UPLOAD_FOLDER):
    os.makedirs(UPLOAD_FOLDER)

app.config["UPLOAD_FOLDER"] = UPLOAD_FOLDER
app.config["MAX_CONTENT_LENGTH"] = 16 * 1024 * 1024  # 16MB 제한


def convert_audio_to_wav(input_path, output_path):
    """오디오 파일을 WAV 형식으로 변환 (Google STT 최적화)"""
    try:
        # ffmpeg를 사용하여 WAV로 변환 (Google STT 최적화)
        cmd = [
            "ffmpeg",
            "-i",
            input_path,
            "-ar",
            "16000",  # 16kHz 샘플링 (Google STT 표준)
            "-ac",
            "1",  # 모노 채널
            "-acodec",
            "pcm_s16le",  # 16-bit PCM 인코딩
            "-f",
            "wav",  # WAV 포맷 강제
            "-y",  # 덮어쓰기
            output_path,
        ]
        result = subprocess.run(cmd, check=True, capture_output=True, text=True)
        print(f"오디오 변환 성공: {input_path} -> {output_path}")
        return True
    except subprocess.CalledProcessError as e:
        print(f"오디오 변환 오류: {e}")
        if hasattr(e, "stderr") and e.stderr:
            print(f"FFmpeg stderr: {e.stderr}")
        return False
    except FileNotFoundError:
        print("ffmpeg가 설치되지 않았습니다. 원본 파일을 사용합니다.")
        print("Google STT 정확도를 위해 ffmpeg 설치를 권장합니다.")
        return False


@app.route("/api/health", methods=["GET"])
def health_check():
    """서버 상태 확인"""
    return jsonify(
        {
            "status": "healthy",
            "timestamp": datetime.now().isoformat(),
            "message": "STT API 서버가 정상적으로 작동 중입니다.",
        }
    )


@app.route("/api/google-speech", methods=["POST"])
def google_speech_to_text():
    """Google Cloud Speech-to-Text API를 사용한 음성 인식 (단어별 타임스탬프)"""
    try:
        if "audio" not in request.files:
            return jsonify({"success": False, "error": "오디오 파일이 없습니다."}), 400

        file = request.files["audio"]
        if file.filename == "":
            return jsonify({"success": False, "error": "파일이 선택되지 않았습니다."}), 400

        # 파일 저장
        original_filename = file.filename
        original_filepath = os.path.join(app.config["UPLOAD_FOLDER"], f"{original_filename}_original")
        file.save(original_filepath)

        # 오디오 변환 시도
        wav_filepath = os.path.join(app.config["UPLOAD_FOLDER"], f"{original_filename}.wav")
        convert_success = convert_audio_to_wav(original_filepath, wav_filepath)

        final_filepath = wav_filepath if convert_success else original_filepath

        print(f"Google STT 처리 파일: {final_filepath}")
        print(f"변환 성공 여부: {convert_success}")

        # 파일 크기 확인
        file_size = os.path.getsize(final_filepath)
        print(f"파일 크기: {file_size} bytes ({file_size / 1024:.1f} KB)")

        # 음성 인식 시작 시간 기록
        start_time = time.time()

        # Google Cloud Speech-to-Text API 사용
        print(f"Google STT 음성 인식 시작: {final_filepath}")
        result = speech_to_text_with_timestamps(final_filepath, language_code="ko-KR")

        # 처리 시간 계산
        processing_time = time.time() - start_time

        # 파일 크기 확인
        file_size = os.path.getsize(final_filepath)
        print(f"처리된 파일 크기: {file_size} bytes")
        print(f"Google STT 결과: {result}")

        # 임시 파일 삭제
        try:
            os.remove(original_filepath)
            if convert_success and os.path.exists(wav_filepath):
                os.remove(wav_filepath)
        except:
            pass

        if result.get("success"):
            return jsonify(
                {
                    "success": True,
                    "results": result.get("results", []),
                    "total_words": result.get("total_words", 0),
                    "processing_time": round(processing_time, 2),
                    "timestamp": datetime.now().isoformat(),
                    "language": "ko-KR",
                    "service": "google-cloud",
                    "file_size": file_size,
                }
            )
        else:
            return (
                jsonify(
                    {
                        "success": False,
                        "error": f"Google STT 인식 실패: {result.get('error', '알 수 없는 오류')}",
                        "processing_time": round(processing_time, 2),
                        "file_size": file_size,
                    }
                ),
                500,
            )

    except Exception as e:
        return jsonify({"success": False, "error": f"Google STT 처리 중 오류가 발생했습니다: {str(e)}"}), 500


@app.route("/api/upload-progress", methods=["POST"])
def upload_progress():
    """업로드 진행률 시뮬레이션 (실제로는 클라이언트에서 처리)"""
    return jsonify({"success": True, "message": "업로드 완료"})


if __name__ == "__main__":
    print("🎯 Google STT API 서버를 시작합니다...")
    print("📡 서버 주소: http://localhost:5000")
    print("🔗 API 엔드포인트: http://localhost:5000/api/google-speech")
    print("🎤 단어별 타임스탬프 지원")
    print("=" * 50)

    app.run(debug=True, host="0.0.0.0", port=5000)
