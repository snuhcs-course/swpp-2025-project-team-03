# extract_features.py
import argparse
import json
import numpy as np
import librosa


def moving_average(x, k):
    if k <= 1:
        return x
    return np.convolve(x, np.ones(k) / k, mode="valid")


def interp_internal_nans(x: np.ndarray) -> np.ndarray:
    """NaN을 내부 구간에서만 선형보간, 양끝의 NaN은 그대로 남겨둠(외삽하지 않음)"""
    xi = x.copy()
    n = len(xi)
    if n == 0:
        return xi
    idx = np.arange(n)
    valid = np.isfinite(xi) & (xi > 0)
    if valid.sum() < 2:
        return xi

    first = np.argmax(valid)
    last = n - 1 - np.argmax(valid[::-1])
    mid = (idx >= first) & (idx <= last)
    xi[mid & ~valid] = np.interp(idx[mid & ~valid], idx[valid], xi[valid])
    return xi


def compute_f0(y, sr, fmin=50.0, fmax=600.0, hop=256, smooth_ms=30.0, use_pyin=True):
    if use_pyin:
        f0, voiced_flag, _ = librosa.pyin(
            y, fmin=fmin, fmax=fmax, sr=sr, frame_length=2048, hop_length=hop, center=True
        )  # noqa: F841
        # pYIN은 무성 프레임을 NaN으로 부여
    else:
        f0 = librosa.yin(y, fmin=fmin, fmax=fmax, sr=sr, frame_length=2048, hop_length=hop, center=True)
        voiced_flag = np.isfinite(f0) & (f0 > 0)  # noqa: F841

    t = librosa.frames_to_time(np.arange(len(f0)), sr=sr, hop_length=hop)

    # 내부 구간을 보간
    f0i = interp_internal_nans(f0)

    # 경계 근접 프레임 제거 (fmin/fmax의 5% 여유)
    lower = fmin * 1.05
    upper = fmax * 0.95
    valid = np.isfinite(f0i) & (f0i > lower) & (f0i < upper)

    if valid.sum() < 2:
        return dict(f0=None, t=t, voiced_count=int(np.isfinite(f0).sum()))

    # log scale smoothing
    f0_st = 12.0 * np.log2(f0i[valid])
    tv = t[valid]
    if smooth_ms > 0 and len(tv) > 1:
        dt = np.median(np.diff(tv))
        k = max(1, int(round((smooth_ms / 1000.0) / dt)))
        k |= 1
        if k > 1 and len(f0_st) >= k:
            f0_st = moving_average(f0_st, k)
            pad = (k - 1) // 2
            tv = tv[pad : len(tv) - pad]

    return dict(f0=f0i[valid], f0_st=f0_st, t=tv, voiced_count=int(valid.sum()))


def linear_slope(x, y):
    if x is None or y is None or len(x) < 2:
        return np.nan
    b, _a = np.polyfit(x, y, 1)
    return float(b)


def silence_features(y, sr, top_db=40):
    intervals = librosa.effects.split(y, top_db=top_db)
    total = len(y) / sr
    speaking = sum((e - s) / sr for s, e in intervals)
    silence = max(0.0, total - speaking)
    return dict(
        total_silence_sec=float(silence), percent_silence=float((silence / total * 100.0) if total > 0 else np.nan)
    )


def rms_features(y, frame_length=2048, hop=256):
    rms = librosa.feature.rms(y=y, frame_length=frame_length, hop_length=hop, center=True).ravel()
    return dict(min_rms=float(np.min(rms)) if rms.size else np.nan)


def extract_features(path, fmin=50.0, fmax=600.0, hop=256, smooth_ms=30.0, end_win_s=0.6, top_db=40, robust=False):
    y, sr = librosa.load(path, sr=None, mono=True)
    sil = silence_features(y, sr, top_db=top_db)
    rms = rms_features(y, hop=hop)
    f = compute_f0(y, sr, fmin=fmin, fmax=fmax, hop=hop, smooth_ms=smooth_ms, use_pyin=True)
    out = dict(**sil, **rms, sr=int(sr))

    if f.get("f0") is None or len(f["f0"]) < 2:
        out.update(
            dict(
                abs_slope_f0_st_per_s=np.nan,
                end_slope_f0_st_per_s=np.nan,
                min_f0_hz=np.nan,
                max_f0_hz=np.nan,
                range_f0_hz=np.nan,
                n_f0_used=int(f.get("voiced_count", 0)),
            )
        )
        return out

    t, f0, f0_st = f["t"], f["f0"], f["f0_st"]
    out["n_f0_used"] = int(f["voiced_count"])

    # min/max of f0
    if robust:
        # remove p5/p95 outliers
        p5, p95 = np.percentile(f0, [5, 95])
        out["min_f0_hz"] = float(p5)
        out["max_f0_hz"] = float(p95)
    else:
        out["min_f0_hz"] = float(np.min(f0))
        out["max_f0_hz"] = float(np.max(f0))
    out["range_f0_hz"] = float(out["max_f0_hz"] - out["min_f0_hz"])

    # calculate slope
    out["abs_slope_f0_st_per_s"] = float(abs(linear_slope(t, f0_st)))
    if len(t) >= 2:
        mask = t >= (t[-1] - end_win_s)
        out["end_slope_f0_st_per_s"] = float(linear_slope(t[mask], f0_st[mask]))
    else:
        out["end_slope_f0_st_per_s"] = np.nan
    return out


if __name__ == "__main__":
    ap = argparse.ArgumentParser(description="Extract prosodic features for (un)certainty from a WAV file.")
    ap.add_argument("wav_path")
    ap.add_argument("--fmin", type=float, default=50.0)
    ap.add_argument("--fmax", type=float, default=600.0)
    ap.add_argument("--hop", type=int, default=256)
    ap.add_argument("--smooth_ms", type=float, default=30.0)
    ap.add_argument("--end_win_s", type=float, default=2)
    ap.add_argument("--top_db", type=float, default=40.0)
    ap.add_argument("--robust", action="store_true", help="use percentile stats (p5/p95) for f0 min/max")
    args = ap.parse_args()

    feats = extract_features(
        args.wav_path, args.fmin, args.fmax, args.hop, args.smooth_ms, args.end_win_s, args.top_db, robust=args.robust
    )
    print(json.dumps(feats, ensure_ascii=False, indent=2))
