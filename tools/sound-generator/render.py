import os, subprocess, wave, struct, traceback
import numpy as np
from engine import SR, finalize, N
import recipes

HERE = os.path.dirname(os.path.abspath(__file__))
RAW = os.path.normpath(os.path.join(HERE, '..', '..', 'app', 'src', 'main', 'res', 'raw')) + os.sep
import tempfile
TMP = tempfile.mkdtemp(prefix='dayflow-sounds-') + os.sep

def write_wav(path, x):
    with wave.open(path, 'wb') as w:
        w.setnchannels(1); w.setsampwidth(2); w.setframerate(SR)
        w.writeframes(b''.join(struct.pack('<h', int(max(-1, min(1, v)) * 32767)) for v in x))

def encode(wav, ogg):
    subprocess.run(['ffmpeg', '-y', '-loglevel', 'error', '-i', wav, '-ac', '1', '-c:a', 'libvorbis', '-q:a', '4', ogg], check=True)

def analyze(x):
    pk = float(np.max(np.abs(x))); act = np.abs(x) > 0.02 * pk
    rms = float(np.sqrt(np.mean(x[act] ** 2))); db = 20 * np.log10(max(rms, 1e-9))
    sil = float(np.mean(np.abs(x) < 0.003))
    spec = np.abs(np.fft.rfft(x)); fr = np.fft.rfftfreq(len(x), 1 / SR); cen = float((spec * fr).sum() / spec.sum())
    clip = float(np.mean(np.abs(x) >= 0.99))
    end = float(np.sqrt(np.mean(x[-N(0.05):] ** 2))); start = float(np.sqrt(np.mean(x[:N(0.003)] ** 2)))
    return pk, db, sil, cen, clip, end, start

ok = True
print(f"{'id':<19}{'mood':<10}{'sec':>5}{'peak':>6}{'actRMS':>8}{'silent%':>8}{'centroid':>9}{'clip%':>7}{'endRMS':>8}{'KB':>5}")
for sid, name, mood, fn in recipes.CATALOG:
    try:
        sig, length = fn()
        x = finalize(sig, length, recipes.TARGET_DB[mood] + recipes.OFFSET_DB.get(sid, 0))
        write_wav(TMP + sid + '.wav', x); encode(TMP + sid + '.wav', RAW + sid + '.ogg')
        pk, db, sil, cen, clip, end, start = analyze(x)
        kb = os.path.getsize(RAW + sid + '.ogg') / 1024
        flag = ''
        if pk >= 0.995 or clip > 0.0005: flag += ' CLIP'
        if sil > 0.6: flag += ' MOSTLY-SILENT'
        if end > 0.01: flag += ' ABRUPT-END'
        if not (3.0 <= length <= 8.0): flag += ' LENGTH'
        if flag: ok = False
        print(f"{sid:<19}{mood:<10}{length:>5.1f}{pk:>6.2f}{db:>8.1f}{sil*100:>8.1f}{cen:>9.0f}{clip*100:>7.3f}{end:>8.4f}{kb:>5.0f}{flag}")
    except Exception as e:
        ok = False; print(sid, 'FAILED', repr(e)); traceback.print_exc()
print("ALL CLEAN" if ok else "SEE FLAGS ABOVE")
