import argparse
import json
from pathlib import Path


def extract_fields_to_label_test(
    root: str,
    src_name: str = "label",
    dst_name: str = "label_test",
    fill_defaults: bool = False,
    encoding: str = "utf-8",
) -> None:
    """
    주어진 root 아래 src_name 폴더 내 모든 .json에서 'script', 'eval_grade'만 추출하여
    dst_name 폴더에 동일한 상대 경로/파일명으로 저장.

    Args:
        root: 루트 경로 (예: C:/Users/me/public_speech/dataset)
        src_name: 소스 폴더명 (기본 'label')
        dst_name: 목적지 폴더명 (기본 'label_test')
        fill_defaults: 필드 누락 시 기본값으로 채워 저장할지 (script="", eval_garde="")
        encoding: 파일 인코딩
    """
    root_path = Path(root)
    src_dir = root_path / src_name
    dst_dir = root_path / dst_name

    if not src_dir.exists() or not src_dir.is_dir():
        raise FileNotFoundError(f"소스 폴더를 찾을 수 없습니다: {src_dir}")

    written = 0
    skipped = 0
    errors = 0

    for src_file in src_dir.rglob("*.json"):
        try:
            # 상대 경로 보존하여 목적지 경로 생성
            rel_path = src_file.relative_to(src_dir)
            out_file = dst_dir / rel_path
            out_file.parent.mkdir(parents=True, exist_ok=True)

            with src_file.open("r", encoding=encoding) as f:
                data = json.load(f)

            out = {}

            if "script" in data and isinstance(data["script"], str):
                out["script"] = data["script"]
            elif fill_defaults:
                out["script"] = ""

            if "eval_grade" in data and isinstance(data["eval_grade"], str):
                out["eval_grade"] = data["eval_grade"]
            elif fill_defaults:
                out["eval_grade"] = ""

            # 두 필드가 모두 없다면 기본값 미사용 시 스킵
            if not out and not fill_defaults:
                skipped += 1
                continue

            with out_file.open("w", encoding=encoding) as f:
                json.dump(out, f, ensure_ascii=False, indent=2)

            written += 1

        except json.JSONDecodeError:
            errors += 1
            print(f"[JSON ERROR] {src_file}")
        except Exception as e:
            errors += 1
            print(f"[ERROR] {src_file} -> {e}")

    print(f"\n[DONE] root={root_path}")
    print(f" - source dir : {src_dir}")
    print(f" - target dir : {dst_dir}")
    print(f" - written    : {written} file(s)")
    print(f" - skipped    : {skipped} file(s) (no target keys)")
    print(f" - errors     : {errors} file(s)")


def main():
    ap = argparse.ArgumentParser(
        description="label 폴더의 모든 JSON에서 'script'와 'eval_grade'만 추출하여 label_test에 저장"
    )
    ap.add_argument("--root", required=True, help="루트 경로 (예: C:/Users/me/public_speech/dataset)")
    ap.add_argument("--src", default="label", help="소스 폴더명 (기본: label)")
    ap.add_argument("--dst", default="label_test", help="목적지 폴더명 (기본: label_test)")
    ap.add_argument(
        "--fill-defaults", action="store_true", help="누락된 필드를 기본값(script='', eval_grade='')으로 채워 저장"
    )
    ap.add_argument("--encoding", default="utf-8", help="파일 인코딩 (기본: utf-8)")
    args = ap.parse_args()

    extract_fields_to_label_test(
        root=args.root,
        src_name=args.src,
        dst_name=args.dst,
        fill_defaults=args.fill_defaults,
        encoding=args.encoding,
    )


if __name__ == "__main__":
    main()
