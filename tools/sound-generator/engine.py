"""Tiny synth engine for the DayFlow alarm library. Everything is generated from math: no samples, no third-party audio."""
import numpy as np
from scipy.signal import butter, sosfilt, lfilter, fftconvolve

SR = 32000
rng = np.random.default_rng(2027)
_NOTE = {'C': 0, 'C#': 1, 'D': 2, 'D#': 3, 'E': 4, 'F': 5, 'F#': 6, 'G': 7, 'G#': 8, 'A': 9, 'A#': 10, 'B': 11}

def hz(name):
    n, o = name[:-1], int(name[-1])
    return 440.0 * 2 ** ((_NOTE[n] + 12 * (o + 1) - 69) / 12)

def N(sec): return int(round(sec * SR))
def tt(n): return np.arange(n) / SR

# ---------- oscillators ----------
def sine(f, n, ph=0.0): return np.sin(2 * np.pi * f * tt(n) + ph)
def saw(f, n):
    k = int(max(1, min(40, (SR / 2 - 500) // f))); t = tt(n); x = np.zeros(n)
    for h in range(1, k + 1): x += np.sin(2 * np.pi * f * h * t) / h
    return x * 0.6
def square(f, n, duty=0.5):
    # zero-mean: a narrow pulse wave has a DC offset that makes clicks between notes
    return np.where(((tt(n) * f) % 1.0) < duty, 0.5, -0.5) - 0.5 * (2 * duty - 1)
def tri(f, n): return 2 / np.pi * np.arcsin(np.sin(2 * np.pi * f * tt(n)))
def supersaw(f, n, spread=0.011):
    return sum(saw(f * (1 + d), n) for d in (-2 * spread, -spread, 0.0, spread, 2 * spread)) / 5
def fm(f, n, ratio=2.0, index=2.0, itau=0.3):
    t = tt(n); I = index * np.exp(-t / itau)
    return np.sin(2 * np.pi * f * t + I * np.sin(2 * np.pi * f * ratio * t))
def noise(n): return rng.standard_normal(n)
def sweep(f0, f1, n):
    t = tt(n); T = max(t[-1], 1e-6); f = f0 * (f1 / f0) ** (t / T)
    return np.sin(2 * np.pi * np.cumsum(f) / SR)

# ---------- envelopes & filters ----------
def env(n, a=0.005, d=0.1, s=0.7, r=0.1):
    a_, d_, r_ = int(a * SR), int(d * SR), int(r * SR); s_ = max(0, n - a_ - d_ - r_)
    e = np.concatenate([np.linspace(0, 1, a_, endpoint=False), np.linspace(1, s, d_, endpoint=False),
                        np.full(s_, s), np.linspace(s, 0, r_)])
    if len(e) < n: e = np.pad(e, (0, n - len(e)))
    return e[:n]
def dec(n, tau, atk=0.002):
    e = np.exp(-tt(n) / tau); a = int(atk * SR)
    if a > 0: e[:a] *= np.linspace(0, 1, a, endpoint=False)
    return e
def lp(x, fc, order=2): return sosfilt(butter(order, min(fc, SR / 2 - 200), 'low', fs=SR, output='sos'), x)
def hp(x, fc, order=2): return sosfilt(butter(order, fc, 'high', fs=SR, output='sos'), x)
def bp(x, lo, hi, order=2): return sosfilt(butter(order, [lo, min(hi, SR / 2 - 200)], 'band', fs=SR, output='sos'), x)

# ---------- effects ----------
def echo(x, delay, fb=0.4, taps=4):
    y = x.copy()
    for k in range(1, taps + 1):
        d = int(delay * SR * k)
        if d < len(x): y[d:] += x[:-d] * fb ** k
    return y
def reverb(x, wet=0.3, t60=1.4, damp=4500):
    n = int(t60 * SR); t = tt(n); ir = noise(n) * np.exp(-6.9 * t / t60); ir = lp(ir, damp)
    p = int(0.012 * SR); ir[:p] *= np.linspace(0, 1, p); ir /= np.sqrt((ir ** 2).sum())
    return x * (1 - 0.5 * wet) + wet * fftconvolve(x, ir)[:len(x)]
def crush(x, bits=6, hold=3):
    y = np.repeat(x[::hold], hold)[:len(x)]; q = 2 ** bits
    return np.round(y * q) / q

# ---------- instruments: f(freq, seconds) -> samples ----------
def bell(f, d, index=2.2): n = N(d); return fm(f, n, 3.5, index, 0.22) * dec(n, d * 0.42)
def epiano(f, d):  n = N(d); return lp(fm(f, n, 1.0, 1.5, 0.30) + 0.18 * sine(2 * f, n) * dec(n, 0.2), 4200) * dec(n, d * 0.55, 0.003)
def mallet(f, d):  n = N(d); return (sine(f, n) + 0.45 * sine(4 * f, n) * dec(n, 0.07)) * dec(n, 0.22)
def musicbox(f, d):n = N(d); return (sine(f, n) + 0.3 * sine(2 * f, n) + 0.1 * sine(3 * f, n)) * dec(n, 0.33)
def ping(f, d):    n = N(d); return (sine(f, n) + 0.35 * sine(2.76 * f, n) * dec(n, 0.25) + 0.12 * sine(5.4 * f, n) * dec(n, 0.1)) * dec(n, 0.45)
def padnote(f, d, att=0.8, rel=0.8, cut=1600):
    n = N(d); return lp(supersaw(f, n, 0.006), cut) * env(n, att, 0.1, 0.9, rel) * 0.8
def lead(f, d):    n = N(d); return lp(supersaw(f, n), 5200) * env(n, 0.01, 0.1, 0.6, min(0.15, d * 0.5))
def stab(f, d=0.3):n = N(d); return lp(supersaw(f, n), 3600) * dec(n, 0.16, 0.003)
def chip(f, d):    n = N(d); return square(f, n, 0.25) * env(n, 0.002, 0.05, 0.7, min(0.04, d * 0.4))
def bass(f, d):    n = N(d); return lp(saw(f, n), 520) * env(n, 0.004, 0.08, 0.7, min(0.06, d * 0.4))
def pluck(f, d):
    n = N(d); k = int(round(SR / f)); a = np.zeros(k + 2); a[0] = 1; a[k] = -0.993 / 2; a[k + 1] = -0.993 / 2
    x = np.zeros(n); x[:k] = lp(rng.uniform(-1, 1, k), 6000); return lfilter([1.0], a, x) * 1.8
def pluck_saw(f, d=0.2): n = N(d); return lp(supersaw(f, n), 7000) * dec(n, 0.09, 0.002)
def drip(f, d=0.5):
    n = N(d); t = tt(n); fr = f * (1 + 1.4 * (1 - np.exp(-t / 0.025)))
    return np.sin(2 * np.pi * np.cumsum(fr) / SR) * dec(n, 0.14)
def pop(f, d=0.2):
    n = N(d); t = tt(n); fr = f * (1 + 1.2 * (1 - np.exp(-t / 0.018)))
    return np.sin(2 * np.pi * np.cumsum(fr) / SR) * dec(n, 0.07, 0.001)

# ---------- drums ----------
def kick(g=1.0, d=0.32):
    n = N(d); t = tt(n); f = 46 + 110 * np.exp(-t / 0.045)
    return np.sin(2 * np.pi * np.cumsum(f) / SR) * dec(n, 0.16, 0.001) * g
def sub808(f=55.0, d=1.0, glide=0.05, drive=1.6):
    n = N(d); t = tt(n); fr = f * (1 + 1.1 * np.exp(-t / glide))
    return np.tanh(drive * np.sin(2 * np.pi * np.cumsum(fr) / SR)) * dec(n, d * 0.45, 0.002)
def hat(g=1.0, d=0.07): n = N(d); return hp(noise(n), 7500) * dec(n, 0.018, 0.0005) * g
def clap(g=1.0):
    out = np.zeros(N(0.3))
    for off in (0.0, 0.012, 0.024):
        x = bp(noise(N(0.2)), 1100, 3800) * dec(N(0.2), 0.035 if off < 0.024 else 0.09, 0.0005); i = N(off); out[i:i + len(x)] += x
    return out * 1.2 * g
def snare(g=1.0): n = N(0.25); return (bp(noise(n), 1500, 6000) * dec(n, 0.07) + 0.5 * sine(190, n) * dec(n, 0.05)) * g

# ---------- composition helpers ----------
class Track:
    def __init__(s, sec): s.b = np.zeros(N(sec))
    def add(s, x, t0, g=1.0):
        i = N(t0)
        if i >= len(s.b): return
        j = min(len(s.b), i + len(x)); s.b[i:j] += x[:j - i] * g
        return s

def seq(tr, inst, items, g=1.0):
    """items: (time, note, seconds[, gain])"""
    for it in items:
        t0, nm, d = it[:3]; gg = it[3] if len(it) > 3 else 1.0
        tr.add(inst(hz(nm), d), t0, g * gg)

def chord(tr, inst, names, t0, d, g=1.0, strum=0.0):
    for k, nm in enumerate(names): tr.add(inst(hz(nm), d), t0 + k * strum, g)

def finalize(x, length, target_db, peak=0.92):
    x = x[:N(length)].copy(); x -= np.mean(x)
    fi, fo = int(0.004 * SR), int(0.09 * SR)
    x[:fi] *= np.linspace(0, 1, fi); x[-fo:] *= np.linspace(1, 0, fo)
    mx = np.max(np.abs(x)); active = np.abs(x) > 0.02 * mx
    rms = np.sqrt(np.mean(x[active] ** 2)); x = x * (10 ** (target_db / 20) / rms)
    x = np.tanh(x * 1.1) / np.tanh(1.1)
    pk = np.max(np.abs(x))
    if pk > peak: x *= peak / pk
    return x
