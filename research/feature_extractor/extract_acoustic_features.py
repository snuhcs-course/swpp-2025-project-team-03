import argparse
import json
import time

import librosa
import numpy as np
import pyworld as pw
import soundfile as sf


def moving_average(x, k):
    if k <= 1:
        return x
    return np.convolve(x, np.ones(k) / k, mode="valid")


def interp_internal_nans(x: np.ndarray) -> np.ndarray:
    """NaN을 내부 구간에서만 선형보간, 양끝 NaN은 그대로 남겨둠"""
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


def compute_f0(y, sr, fmin=50.0, fmax=600.0, hop=256, smooth_ms=30.0):
    # hop(ms) 변환
    frame_period = hop / sr * 1000.0

    # pyworld dio + stonemask
    _f0, t = pw.dio(y.astype(np.float64), sr, f0_floor=fmin, f0_ceil=fmax, frame_period=frame_period)
    f0 = pw.stonemask(y.astype(np.float64), _f0, t, sr)

    # 0인 구간은 무성으로 보고 NaN 처리
    f0 = np.where(f0 > 0, f0, np.nan)

    # 내부 구간을 보간
    f0i = interp_internal_nans(f0)

    # 경계 필터 (fmin/fmax 5%)
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


def _remove_short_runs(mask: np.ndarray, min_len: int, val: int) -> np.ndarray:
    if mask.size == 0:
        return mask
    x = mask.astype(np.int8)
    d = np.diff(np.pad(x, (1, 1)))
    starts = np.where(d == 1)[0]
    ends = np.where(d == -1)[0]
    out = x.copy()
    for s, e in zip(starts, ends):
        if (e - s) < min_len and np.all(out[s:e] == val):
            out[s:e] = 1 - val
    return out.astype(bool)


def _binary_open_close(mask: np.ndarray, min_speech_frames: int, min_silence_frames: int) -> np.ndarray:
    opened = _remove_short_runs(mask, min_speech_frames, val=1)  # 짧은 발화 제거
    closed = _remove_short_runs(opened, min_silence_frames, val=0)  # 짧은 침묵 제거
    return closed


def silence_features_fast(
    y, sr, frame_length=1024, hop_length=512, top_db=40, min_speech_sec=0.12, min_silence_sec=0.16
):
    if len(y) < frame_length:  # if audio is shorter than one frame
        total = len(y) / sr
        return dict(
            total_length=float(total),
            total_silence_sec=float(total),
            percent_silence=float(100.0 if total > 0 else np.nan),
            intervals=np.empty((0, 2), dtype=np.int64),
        )
    # rms window
    frames = np.lib.stride_tricks.sliding_window_view(y, frame_length)[::hop_length]
    rms = np.sqrt(np.mean(frames**2, axis=1))

    # convert to db
    ref = np.max(rms) + 1e-10
    db = 20 * np.log10(rms / ref + 1e-10)

    # threshold filtering
    speech_mask = db > -top_db

    min_speech_frames = max(1, int(round(min_speech_sec * sr / hop_length)))
    min_silence_frames = max(1, int(round(min_silence_sec * sr / hop_length)))
    smooth_mask = _binary_open_close(speech_mask, min_speech_frames, min_silence_frames)

    # intervals로 변환 (프레임 -> 샘플)
    x = smooth_mask.astype(np.int8)
    d = np.diff(np.pad(x, (1, 1)))
    starts = np.where(d == 1)[0]
    ends = np.where(d == -1)[0]
    starts_samp = starts * hop_length
    ends_samp = np.minimum(ends * hop_length + frame_length, len(y))
    intervals = (
        np.stack([starts_samp, ends_samp], axis=1).astype(np.int64)
        if starts.size and ends.size
        else np.empty((0, 2), dtype=np.int64)
    )

    total = len(y) / sr
    if len(intervals) == 0:
        silence = total
    else:
        speaking = np.sum((intervals[:, 1] - intervals[:, 0])) / sr
        silence = max(0.0, total - speaking)
    return dict(
        total_length=float(total),
        total_silence_sec=float(silence),
        percent_silence=float((silence / total * 100.0) if total > 0 else np.nan),
        intervals=intervals,  # now intervals provided
    )


def rms_features(
    y, sr, intervals=None, frame_length=2048, hop=256, center=True, robust=False, eps=1e-8, noise_gain=1.5
):
    # primitive RMS
    rms = librosa.feature.rms(y=y, frame_length=frame_length, hop_length=hop, center=center).ravel()
    n_frames = len(rms)
    if n_frames == 0:
        return dict(min_rms=np.nan, median_rms=np.nan, noise_rms=np.nan)

    # center timestamps of frame
    t = librosa.frames_to_time(np.arange(n_frames), sr=sr, hop_length=hop)

    if intervals is None or len(intervals) == 0:
        speech_mask = np.ones(n_frames, dtype=bool)
    else:
        speech_mask = np.zeros(n_frames, dtype=bool)
        for s, e in intervals:
            ts = s / sr
            te = e / sr
            speech_mask |= (t >= ts) & (t <= te)

    # calculate noise rms
    nonspeech = (~speech_mask) & np.isfinite(rms)
    if nonspeech.any():
        noise_rms = float(np.median(rms[nonspeech]))
    else:
        noise_rms = float(np.percentile(rms[np.isfinite(rms)], 5))

    # 발화 프레임 추출 + 노이즈 하한 적용
    speech_vals = rms[speech_mask & np.isfinite(rms)]
    if speech_vals.size == 0:
        return dict(min_rms=np.nan, median_rms=np.nan, noise_rms=noise_rms)

    floor = max(eps, noise_rms * noise_gain)
    speech_vals = np.maximum(speech_vals, floor)

    if robust:
        min_r = float(np.percentile(speech_vals, 10))
    else:
        min_r = float(np.min(speech_vals))

    med_r = float(np.median(speech_vals))

    return dict(min_rms=min_r, median_rms=med_r, noise_rms=noise_rms)


def _fmt_pause_key(sec: float) -> str:
    s = str(sec).replace(".", "_")
    if s.endswith("_0"):
        s = s[:-2]
    return f"pause_{s}_cnt"


def count_pauses_from_intervals(
    intervals: np.ndarray,
    sr: int,
    thresholds_sec=(0.5, 1.0, 2.0),
) -> dict:
    out = {}
    thresholds = sorted(float(x) for x in thresholds_sec)
    for thr in thresholds:
        out[_fmt_pause_key(thr)] = 0

    if intervals is None or len(intervals) == 0:
        return out

    silences_sec = []
    for i in range(len(intervals) - 1):
        _, e = intervals[i]
        s_next, _ = intervals[i + 1]
        gap = (s_next - e) / sr
        if gap > 0:
            silences_sec.append(gap)

    for thr in thresholds:
        cnt = sum(1 for g in silences_sec if g >= thr)
        out[_fmt_pause_key(thr)] = int(cnt)

    return out


def extract_acoustic_features(
    path,
    fmin=50.0,
    fmax=600.0,
    hop=256,
    smooth_ms=30.0,
    end_win_s=0.6,
    top_db=40,
    robust=False,
    pause_secs=(0.5, 1.0, 2.0),
):
    start_time = time.time()
    y, sr = sf.read(path, dtype="float32", always_2d=False)
    if y.ndim > 1:
        y = y[:, 0]

    end_time = time.time()
    print(f"Audio loaded in {end_time - start_time:.4f}sec")

    start_time = time.time()
    sil = silence_features_fast(y, sr, hop_length=hop, top_db=top_db)  # use only fast VAD
    end_time = time.time()
    print(f"VAD in {end_time - start_time:.4f}sec")

    # # skip rms features
    # rms = rms_features(y, sr, intervals=intervals, hop=hop, robust=robust)

    start_time = time.time()
    f = compute_f0(y, sr, fmin=fmin, fmax=fmax, hop=hop, smooth_ms=smooth_ms)
    end_time = time.time()
    print(f"f0 in {end_time - start_time:.4f}sec")

    out = dict(**{k: v for k, v in sil.items() if k != "intervals"}, sr=int(sr))

    pause_dict = count_pauses_from_intervals(
        intervals=sil["intervals"],
        sr=sr,
        thresholds_sec=pause_secs,
    )
    out.update(pause_dict)
    if f.get("f0") is None or len(f["f0"]) < 2:
        out.update(
            dict(
                tot_slope_f0_st_per_s=np.nan,
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
    out["tot_slope_f0_st_per_s"] = float(linear_slope(t, f0_st))
    if len(t) >= 2:
        mask = t >= (t[-1] - end_win_s)
        out["end_slope_f0_st_per_s"] = float(linear_slope(t[mask], f0_st[mask]))
    else:
        out["end_slope_f0_st_per_s"] = np.nan
    return out


def _parse_pause_secs(s: str):
    if not s:
        return (0.5, 1.0, 2.0)
    vals = []
    for tok in s.split(","):
        tok = tok.strip()
        if not tok:
            continue
        vals.append(float(tok))
    return tuple(vals)


if __name__ == "__main__":
    ap = argparse.ArgumentParser(description="Extract prosodic features for uncertainty from a WAV file.")
    ap.add_argument("wav_path")
    ap.add_argument("--fmin", type=float, default=50.0)
    ap.add_argument("--fmax", type=float, default=600.0)
    ap.add_argument("--hop", type=int, default=256)
    ap.add_argument("--smooth_ms", type=float, default=30.0)
    ap.add_argument("--end_win_s", type=float, default=3.0)
    ap.add_argument("--top_db", type=float, default=40.0)
    ap.add_argument("--robust", action="store_true", help="use percentile stats (p5/p95) for f0 min/max")
    ap.add_argument(
        "--pause_secs",
        type=str,
        default="0.5,1.0,2.0",
        help="쉼(침묵) 카운트 임계값(초)들을 콤마로 구분해 지정. ex) '0.5,1,2'",
    )
    args = ap.parse_args()

    start_time = time.time()
    feats = extract_acoustic_features(
        args.wav_path,
        args.fmin,
        args.fmax,
        args.hop,
        args.smooth_ms,
        args.end_win_s,
        args.top_db,
        robust=args.robust,
        pause_secs=_parse_pause_secs(args.pause_secs),
    )
    print(json.dumps(feats, ensure_ascii=False, indent=2))
    end_time = time.time()

    print(f"time consumed: {end_time - start_time:.4f} sec")
