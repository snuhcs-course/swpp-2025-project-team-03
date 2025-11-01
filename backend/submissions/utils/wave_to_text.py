import argparse
import io
import os

import numpy as np
import resampy
import soundfile as sf
from dotenv import load_dotenv
from google.cloud import speech

load_dotenv()


def resample_to_16k_mono(filepath: str) -> bytes:
    """WAV 파일을 메모리 상에서 16kHz mono PCM16으로 변환"""

    file_size = os.path.getsize(filepath)

    if file_size == 0:
        raise ValueError(f"Empty file: {filepath}")

    try:
        data, samplerate = sf.read(filepath)

        if len(data.shape) > 1:
            data = np.mean(data, axis=1)

        if np.max(np.abs(data)) > 0:
            data = data / np.max(np.abs(data))

        threshold = 0.01
        data = np.where(np.abs(data) < threshold, 0, data)

        if samplerate != 16000:
            data = resampy.resample(data, samplerate, 16000)
            samplerate = 16000

        # 메모리 버퍼에 16bit PCM WAV 저장
        buffer = io.BytesIO()
        sf.write(buffer, data, samplerate, format="WAV", subtype="PCM_16")
        buffer.seek(0)

        wav_bytes = buffer.read()

        return wav_bytes, samplerate

    except Exception as e:
        raise


def speech_to_text(filepath: str, language_code: str = "ko-KR") -> str:
    """Google Cloud STT 요청"""

    client = speech.SpeechClient()

    wav_bytes, sr = resample_to_16k_mono(filepath)

    audio = speech.RecognitionAudio(content=wav_bytes)

    config = speech.RecognitionConfig(
        encoding=speech.RecognitionConfig.AudioEncoding.LINEAR16,
        language_code=language_code,
        enable_automatic_punctuation=True,
        enable_word_time_offsets=True,
        enable_word_confidence=True,
        model="latest_long",
        sample_rate_hertz=16000,
        audio_channel_count=1,
        use_enhanced=True,
        alternative_language_codes=["ko-KR", "en-US"],
    )

    response = client.recognize(config=config, audio=audio)

    if not response.results:
        return ""

    transcript = " ".join(result.alternatives[0].transcript for result in response.results)

    return transcript.strip()


if __name__ == "__main__":  # pragma: no cover
    parser = argparse.ArgumentParser(description="Google STT로 음성파일을 텍스트로 변환 (in-memory)")
    parser.add_argument("audio_path", type=str, help="입력 WAV 파일 경로")
    parser.add_argument("--lang", type=str, default="ko-KR", help="언어 코드 (기본값: ko-KR)")
    args = parser.parse_args()

    if not os.path.exists(args.audio_path):
        print(f"File not found: {args.audio_path}")

    # 파일 크기 및 길이 확인
    file_size = os.path.getsize(args.audio_path)

    try:
        text = speech_to_text(args.audio_path, args.lang)
        if text:
            print(text)
        else:
            print("(인식된 텍스트 없음)")
    except Exception as e:
        print(f"Error: {e}")
