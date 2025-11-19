#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Google Cloud Speech-to-Text API를 사용한 음성 인식
단어별 타임스탬프 제공
"""

import io
import os

from google.cloud import speech
from google.oauth2 import service_account

# Google Cloud 인증 정보
GOOGLE_APPLICATION_CREDENTIALS = os.getenv("GOOGLE_APPLICATION_CREDENTIALS")


def get_google_client():
    """Google Cloud Speech 클라이언트 생성"""
    try:
        if GOOGLE_APPLICATION_CREDENTIALS:
            credentials = service_account.Credentials.from_service_account_file(GOOGLE_APPLICATION_CREDENTIALS)
            client = speech.SpeechClient(credentials=credentials)
        else:
            client = speech.SpeechClient()
        return client
    except Exception as e:
        print(f"Google Cloud 클라이언트 생성 오류: {e}")
        return None


def speech_to_text_with_timestamps(audio_file_path, language_code="ko-KR"):
    """
    Google Cloud Speech-to-Text를 사용하여 음성을 텍스트로 변환
    단어별 타임스탬프 포함

    Args:
        audio_file_path (str): 오디오 파일 경로
        language_code (str): 언어 코드 (기본값: ko-KR)

    Returns:
        dict: 인식 결과와 타임스탬프 정보
    """
    try:
        client = get_google_client()
        if not client:
            return {"error": "Google Cloud 클라이언트 생성 실패"}

        # 오디오 파일 읽기
        with io.open(audio_file_path, "rb") as audio_file:
            content = audio_file.read()

        # 오디오 설정
        audio = speech.RecognitionAudio(content=content)

        config = speech.RecognitionConfig(
            encoding=speech.RecognitionConfig.AudioEncoding.WEBM_OPUS,  # WebM Opus 명시
            sample_rate_hertz=48000,  # WebM Opus 파일의 실제 샘플링 레이트
            language_code=language_code,
            enable_automatic_punctuation=True,
            enable_word_time_offsets=True,  # 단어별 타임스탬프 활성화
            enable_word_confidence=True,  # 단어별 신뢰도 활성화
            model="latest_long",  # 최신 장시간 모델 사용 (분당 7원)
        )

        # 음성 인식 수행 (긴 오디오를 위한 비동기 처리)
        try:
            response = client.recognize(config=config, audio=audio)
        except Exception as sync_error:
            if "Sync input too long" in str(sync_error):
                print("파일이 너무 길어서 비동기 처리로 전환합니다...")
                operation = client.long_running_recognize(config=config, audio=audio)
                print("비동기 음성 인식 처리 중...")
                response = operation.result(timeout=120)
            else:
                raise sync_error

        if not response.results:
            return {"error": "인식 결과가 없습니다"}

        results = []
        for result in response.results:
            alternative = result.alternatives[0]

            # 전체 텍스트
            full_text = alternative.transcript

            # 단어별 타임스탬프
            words = []
            for word_info in alternative.words:
                word_data = {
                    "word": word_info.word,
                    "start_time": word_info.start_time.total_seconds(),
                    "end_time": word_info.end_time.total_seconds(),
                    "confidence": word_info.confidence,
                }
                words.append(word_data)

            results.append({"text": full_text, "confidence": alternative.confidence, "words": words})

        return {"success": True, "results": results, "total_words": sum(len(r["words"]) for r in results)}

    except Exception as e:
        return {"error": f"음성 인식 오류: {str(e)}"}


def format_timestamped_result(result):
    """
    타임스탬프가 포함된 결과를 사용자 친화적인 형태로 포맷팅

    Args:
        result (dict): speech_to_text_with_timestamps의 결과

    Returns:
        str: 포맷팅된 결과
    """
    if not result.get("success"):
        return f"오류: {result.get('error', '알 수 없는 오류')}"

    formatted_results = []

    for i, res in enumerate(result["results"]):
        formatted_results.append(f"=== 결과 {i + 1} ===")
        formatted_results.append(f"전체 텍스트: {res['text']}")
        formatted_results.append(f"전체 신뢰도: {res['confidence']:.2f}")
        formatted_results.append("단어별 타임스탬프:")

        for word_data in res["words"]:
            start_time = word_data["start_time"]
            end_time = word_data["end_time"]
            word = word_data["word"]
            confidence = word_data["confidence"]

            formatted_results.append(f"  [{start_time:.2f}s - {end_time:.2f}s] '{word}' (신뢰도: {confidence:.2f})")

    return "\n".join(formatted_results)


def main():
    """테스트 함수"""
    print("🎤 Google Cloud Speech-to-Text 테스트")
    print("=" * 50)

    # 환경변수 확인
    if not GOOGLE_APPLICATION_CREDENTIALS:
        print("  경고: GOOGLE_APPLICATION_CREDENTIALS 환경변수가 설정되지 않았습니다.")
        print("Google Cloud 서비스 계정 키 파일 경로를 설정해주세요.")
        print("예: export GOOGLE_APPLICATION_CREDENTIALS='path/to/service-account-key.json'")
        return

    # 테스트 파일 경로 (사용자가 제공)
    test_file = input("테스트할 오디오 파일 경로를 입력하세요: ").strip()

    if not os.path.exists(test_file):
        print(f" 파일을 찾을 수 없습니다: {test_file}")
        return

    print(f"📁 파일: {test_file}")
    print("🔄 음성 인식 중...")

    # 음성 인식 수행
    result = speech_to_text_with_timestamps(test_file)

    # 결과 출력
    print("\n📝 인식 결과:")
    print("=" * 50)
    print(format_timestamped_result(result))

    # JSON 형태로도 출력
    print("\n📊 JSON 형태 결과:")
    print("=" * 50)
    # print(json.dumps(result, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
