import argparse
import os
from concurrent.futures import ProcessPoolExecutor, as_completed

from moviepy import VideoFileClip


def convert_single(mp4_path: str):
    wav_path = os.path.splitext(mp4_path)[0] + ".wav"
    try:
        video = VideoFileClip(mp4_path)
        audio = video.audio
        audio.write_audiofile(wav_path, logger=None)
        audio.close()
        video.close()
        os.remove(mp4_path)  # MP4 파일 삭제
        return f"✅ Converted and removed: {mp4_path}"
    except Exception as e:
        return f"❌ Failed: {mp4_path} ({e})"


def gather_mp4_files(root_dir="."):
    mp4_files = []
    for dirpath, _, filenames in os.walk(root_dir):
        for filename in filenames:
            if filename.lower().endswith(".mp4"):
                mp4_files.append(os.path.join(dirpath, filename))
    return mp4_files


def convert_all(root_dir=".", max_workers=4):
    mp4_files = gather_mp4_files(root_dir)
    if not mp4_files:
        print("⚠️ No mp4 files found.")
        return

    print(f"Found {len(mp4_files)} mp4 files. Starting conversion with {max_workers} workers...")

    with ProcessPoolExecutor(max_workers=max_workers) as executor:
        futures = {executor.submit(convert_single, f): f for f in mp4_files}
        for future in as_completed(futures):
            print(future.result())


if __name__ == "__main__":
    ap = argparse.ArgumentParser(description="Convert every mp4 files to wav files from all subdirectory")
    ap.add_argument("--input_root", default=".", help="root folder to search")
    args = ap.parse_args()

    # 사용자에게 실행 여부 확인
    user_input = input(
        f"All .mp4 files in '{os.path.abspath(args.input_root)}' and its subdirectories will be converted to .wav. Proceed? (y/N): "
    )
    if user_input.lower() != "y":
        print("Conversion cancelled.")
        exit()

    # 현재 디렉토리 이하의 모든 mp4 변환
    # CPU 코어 개수에 맞게 max_workers 조절
    convert_all(args.input_root, max_workers=4)
