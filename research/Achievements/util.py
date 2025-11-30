import chardet


def detect_encoding(csv_path: str) -> str:
    """Detect encoding using chardet (fallback to utf-8)."""
    try:
        with open(csv_path, "rb") as f:
            result = chardet.detect(f.read(50000))
        enc = result.get("encoding") or "utf-8"
        conf = result.get("confidence", 0)
        return enc if conf >= 0.5 else "utf-8"
    except Exception:
        return "utf-8"
