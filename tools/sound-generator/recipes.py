from engine import *

# Each recipe returns (signal, seconds). Names and moods are listed in CATALOG at the bottom.
def glass_drop():
    tr = Track(7.5)
    soft = lambda f, d: bell(f, d, 1.3)
    seq(tr, soft, [(0, 'C6', 1.3), (0.3, 'E6', 1.3), (0.6, 'G6', 1.3), (0.9, 'B6', 1.3)], 0.9)
    tr.add(soft(hz('C7'), 2.4), 1.5, 0.6); tr.add(soft(hz('E5'), 2.6), 1.5, 0.45)
    return lp(reverb(echo(tr.b, 0.36, 0.3, 3), 0.35, 1.6), 5200), 6.2

def lofi_morning():
    tr = Track(7.5)
    for t0, notes in [(0.0, ['C4', 'E4', 'G4', 'B4', 'D5']), (1.95, ['F3', 'A3', 'C4', 'E4', 'G4']), (3.9, ['A3', 'C4', 'E4', 'G4', 'B4'])]:
        chord(tr, epiano, notes, t0, 2.0, 0.55, 0.035)
    tr.add(epiano(hz('G5'), 1.4), 5.7, 0.4)
    n = len(tr.b); idx = rng.integers(0, n, int(n / SR * 20)); c = np.zeros(n); c[idx] = rng.uniform(0.3, 1, len(idx)) * rng.choice([-1, 1], len(idx))
    return reverb(tr.b + hp(lp(c, 6000), 1500) * 0.05, 0.25, 1.3), 7.2

def moonlight():
    tr = Track(8)
    chord(tr, padnote, ['A2', 'E3', 'A3', 'C4', 'E4', 'B4'], 0.0, 6.0, 0.28)
    seq(tr, ping, [(0.9, 'A5', 1.2), (1.6, 'C6', 1.2), (2.3, 'E6', 1.2), (2.7, 'G6', 1.2), (3.5, 'B6', 1.4), (4.1, 'E6', 1.8)], 0.5)
    return reverb(echo(tr.b, 0.5, 0.3, 3), 0.5, 2.0), 6.6

def cloud_nine():
    tr = Track(8)
    chord(tr, padnote, ['F3', 'C4', 'E4', 'A4'], 0.0, 6.0, 0.30, 0.0)
    seq(tr, musicbox, [(0.2, 'A5', 1.5), (1.2, 'C6', 1.5), (2.2, 'E6', 1.5), (3.4, 'A6', 2.2)], 0.55)
    return reverb(tr.b, 0.5, 2.0), 6.6

def dreamy_harp():
    tr = Track(9)
    up = ['C4', 'E4', 'G4', 'C5', 'E5', 'G5', 'C6']
    seq(tr, pluck, [(k * 0.17, nm, 1.6) for k, nm in enumerate(up)], 0.6)
    seq(tr, pluck, [(1.4 + k * 0.17, nm, 1.6) for k, nm in enumerate(reversed(up[2:]))], 0.5)
    tr.add(pluck(hz('E6'), 2.5), 2.9, 0.4)
    return reverb(tr.b, 0.32, 1.8), 7.4

def velvet_keys():
    tr = Track(8)
    chord(tr, epiano, ['E3', 'B3', 'G4', 'D5'], 0.0, 3.0, 0.4, 0.04)
    seq(tr, epiano, [(0, 'E5', 1.0), (0.5, 'G5', 1.0), (1.0, 'B5', 1.2), (1.6, 'A5', 1.0), (2.2, 'G5', 1.0), (2.8, 'E5', 1.0), (3.4, 'D5', 1.0), (4.0, 'E5', 2.4)], 0.7)
    return reverb(tr.b, 0.3, 1.5), 6.0

def mellow_drip():
    tr = Track(7)
    times = [(0, 'E5'), (0.4, 'B5'), (0.9, 'G5'), (1.1, 'D6'), (1.8, 'A5'), (2.3, 'E6'), (2.5, 'B5'), (3.2, 'G5'), (3.9, 'E6')]
    seq(tr, drip, [(t0, nm, 0.5) for t0, nm in times], 0.75)
    return reverb(echo(tr.b, 0.31, 0.5, 4), 0.45, 1.8), 5.6

def kawaii_bells():
    tr = Track(8)
    m = ['E6', 'C6', 'G5', 'C6', 'E6', 'G6', 'E6', 'C6', 'D6', 'B5', 'G5', 'B5', 'D6', 'G6', 'D6', 'B5']
    seq(tr, musicbox, [(k * 0.24, nm, 0.7) for k, nm in enumerate(m)], 0.7)
    chord(tr, musicbox, ['C6', 'E6', 'G6', 'C7'], 4.0, 1.8, 0.45, 0.04)
    return reverb(tr.b, 0.3, 1.4), 6.0

def golden_hour():
    tr = Track(9)
    chord(tr, padnote, ['G3', 'B3', 'D4', 'F#4', 'A4'], 0.0, 6.8, 0.26)
    notes = ['G4', 'B4', 'D5', 'F#5', 'A5', 'B5', 'D6', 'F#6']
    seq(tr, pluck, [(0.4 + k * 0.22, nm, 1.8) for k, nm in enumerate(notes)], 0.6)
    seq(tr, pluck, [(2.4 + k * 0.22, nm, 1.8) for k, nm in enumerate(reversed(notes[2:]))], 0.5)
    tr.add(bell(hz('G6'), 2.6), 4.3, 0.5)
    return reverb(tr.b, 0.4, 2.0), 7.4

def bubble_pop():
    tr = Track(7)
    ts = [0, 0.17, 0.31, 0.55, 0.62, 0.9, 1.2, 1.25, 1.7, 1.78, 2.2, 2.4]
    fs = [420, 560, 700, 500, 820, 640, 900, 560, 740, 980, 620, 860]
    for t0, f in zip(ts, fs): tr.add(pop(f), t0, 0.85)
    chord(tr, bell, ['C6', 'E6', 'G6'], 3.0, 1.4, 0.7, 0.07)
    return reverb(echo(tr.b, 0.22, 0.35, 3), 0.25, 1.2), 5.0

def crystal_ping():
    tr = Track(8)
    pat = ['E6', 'G6', 'B6', 'G6', 'E6', 'B6', 'D7', 'B6', 'G6', 'E6']
    seq(tr, ping, [(k * 0.2, nm, 1.0) for k, nm in enumerate(pat)], 0.6)
    tr.add(ping(hz('E7'), 2.0), 2.2, 0.5)
    return reverb(echo(tr.b, 0.3, 0.45, 5), 0.35, 1.6), 5.4

def soft_marimba():
    tr = Track(8)
    bars = [['C5', 'E5', 'G5', 'E5'], ['A4', 'C5', 'E5', 'C5'], ['F4', 'A4', 'C5', 'A4'], ['G4', 'B4', 'D5', 'B4']]
    k = 0
    for bar in bars:
        for nm in bar:
            swing = 0.03 if k % 2 else 0.0
            tr.add(mallet(hz(nm), 0.6), k * 0.2 + swing, 0.8); k += 1
    chord(tr, mallet, ['C5', 'E5', 'G5', 'C6'], 3.3, 1.2, 0.5, 0.03)
    return reverb(tr.b, 0.25, 1.3), 5.0

def sunrise_synth():
    tr = Track(9)
    chord(tr, padnote, ['C3', 'G3', 'E4'], 0.0, 6.0, 0.25, 0.0)
    arp = ['C4', 'G4', 'C5', 'E5', 'G5', 'C6', 'G5', 'E5'] * 2
    for k, nm in enumerate(arp):
        n = N(0.22); cut = 800 + 5200 * k / len(arp)
        tr.add(lp(supersaw(hz(nm), n), cut) * dec(n, 0.12, 0.003), k * 0.16, 0.7)
    chord(tr, stab, ['C5', 'E5', 'G5', 'C6'], 2.7, 1.6, 0.6)
    return reverb(echo(tr.b, 0.24, 0.3, 3), 0.28, 1.6), 6.2

def starlight_arp():
    tr = Track(8)
    sc = ['A4', 'C5', 'D5', 'E5', 'G5', 'A5', 'C6', 'D6', 'E6', 'G6', 'A6']
    order = sc + list(reversed(sc[:-1]))
    seq(tr, ping, [(k * 0.085, nm, 0.7) for k, nm in enumerate(order)], 0.55)
    chord(tr, bell, ['A5', 'E6', 'A6'], 1.9, 2.0, 0.5, 0.05)
    return reverb(echo(tr.b, 0.255, 0.4, 3), 0.35, 1.8), 4.8

def hyper_pluck():
    tr = Track(7)
    pat = ['G5', 'G5', 'D6', 'C6', 'B5', 'G5', 'E6', 'D6', 'G5', 'G5', 'D6', 'C6', 'B5', 'D6', 'G6', 'D6']
    for k, nm in enumerate(pat):
        tr.add(pluck_saw(hz(nm)), k * 0.125, 0.8)
        if k % 4 == 3: tr.add(sweep(900, 3000, N(0.1)) * dec(N(0.1), 0.05) * 0.4, k * 0.125 + 0.06)
    chord(tr, stab, ['G5', 'B5', 'D6', 'G6'], 2.0, 1.5, 0.55)
    return reverb(echo(tr.b, 0.125 * 3, 0.3, 3), 0.15, 1.0), 4.5

def neon_pulse():
    tr = Track(7); e8 = 60 / 110 / 2
    for bar, (root, tones) in enumerate([('A2', ['A4', 'C5', 'E5', 'A5', 'E5', 'C5']), ('F2', ['F4', 'A4', 'C5', 'F5', 'C5', 'A4'])]):
        for k in range(8): tr.add(bass(hz(root), e8 * 0.9), (bar * 8 + k) * e8, 0.8)
        for k in range(16): tr.add(lead(hz(tones[k % 6]), e8 * 0.45), (bar * 8) * e8 + k * e8 / 2, 0.45)
    for k in range(8): tr.add(kick(0.9), k * 2 * e8)
    for k in range(16): tr.add(hat(0.28), (k * 2 + 1) * e8 / 1.0 * 0.5 if False else k * e8 + (e8 / 2))
    return reverb(echo(tr.b, e8 * 0.75, 0.3, 3), 0.18, 1.0), 4.6

def retro_wave():
    tr = Track(8); beat = 0.6
    chords = [('A1', 'E5'), ('F1', 'C5'), ('C2', 'G5'), ('G1', 'D5')]
    for i, (root, top) in enumerate(chords):
        for k in range(4): tr.add(bass(hz(root) * 2, 0.27), i * 1.2 + k * 0.3, 0.8)
        tr.add(lead(hz(top), 1.1), i * 1.2, 0.6)
        tr.add(lead(hz(top) * 1.5, 1.1), i * 1.2, 0.25)
    for k in range(8): tr.add(kick(0.8), k * beat)
    for k in (2, 6): tr.add(snare(0.6), k * beat)
    return reverb(echo(tr.b, 0.45, 0.35, 3), 0.22, 1.4), 6.4

def pixel_quest():
    tr = Track(6)
    mel = ['C5', 'E5', 'G5', 'C6', 'G5', 'E5', 'G5', 'C6', 'D6', 'B5', 'G5', 'B5', 'C6', 'G5', 'E5', 'C5']
    seq(tr, chip, [(k * 0.12, nm, 0.11) for k, nm in enumerate(mel)], 0.7)
    for k, r in enumerate(['C3', 'C3', 'G2', 'C3']): tr.add(tri(hz(r), N(0.45)) * env(N(0.45), 0.005, 0.05, 0.8, 0.05), k * 0.48, 0.9)
    seq(tr, chip, [(1.95, 'C6', 0.3), (2.25, 'E6', 0.3), (2.55, 'G6', 0.6)], 0.7)
    return echo(tr.b, 0.18, 0.25, 2), 4.4

def trap_tick(): 
    tr = Track(6); beat = 60 / 140
    tr.add(sub808(55, 1.0), 0.0, 0.95); tr.add(sub808(49, 0.9), beat * 3.5, 0.9); tr.add(sub808(55, 1.0), beat * 4.0, 0.95); tr.add(sub808(62, 0.9), beat * 7.5, 0.9)
    for bar in (0, 1):
        tr.add(clap(0.9), bar * 4 * beat + 2 * beat)
        for k in range(8): tr.add(hat(0.4 if k % 2 else 0.6), bar * 4 * beat + k * beat / 2)
        for k in range(4): tr.add(hat(0.45), bar * 4 * beat + 3.5 * beat + k * beat / 8 + 0.0)
    seq(tr, ping, [(0.0, 'E6', 0.8), (0.43, 'B5', 0.8), (0.86, 'G5', 0.8), (1.29, 'B5', 0.8), (1.714, 'E6', 0.8), (2.14, 'D6', 0.8), (2.57, 'B5', 0.8), (3.0, 'G5', 1.0)], 0.5)
    return reverb(tr.b, 0.15, 1.0), 4.8

def digital_heartbeat():
    tr = Track(8)
    for k in range(6):
        t0 = k * 0.85
        tr.add(sub808(52, 0.35, 0.03, 1.4), t0, 0.9); tr.add(sub808(62, 0.3, 0.03, 1.2), t0 + 0.2, 0.6)
        tr.add(ping(hz('E5') if k % 2 == 0 else hz('B5'), 0.6), t0 + 0.45, 0.35)
    return reverb(echo(tr.b, 0.2, 0.3, 2), 0.18, 1.0), 5.4

def pulse_drive():
    tr = Track(7); beat = 60 / 128
    for k in range(8): tr.add(kick(1.0), k * beat)
    for k in range(16):
        tr.add(hat(0.35), k * beat / 2 + beat / 4)
        if k % 2 == 1: chord(tr, stab, ['A3', 'C4', 'E4'], k * beat / 2, 0.2, 0.5)
        elif k % 4 != 0: tr.add(bass(hz('A1') * 2, 0.2), k * beat / 2, 0.7)
    tr.add(hp(noise(N(0.5)), 3000) * np.linspace(0, 1, N(0.5)) * 0.25, 3.25)
    return reverb(tr.b, 0.12, 0.9), 4.0

def power_up():
    tr = Track(6)
    tr.add(sweep(400, 2400, N(0.5)) * env(N(0.5), 0.01, 0.1, 0.6, 0.1), 0.0, 0.35)
    seq(tr, chip, [(k * 0.055, nm, 0.09) for k, nm in enumerate(['C5', 'E5', 'G5', 'C6', 'E6', 'G6', 'C7'])], 0.8)
    chord(tr, stab, ['C5', 'E5', 'G5', 'C6'], 0.45, 1.4, 0.9)
    chord(tr, bell, ['C7', 'E7', 'G6'], 0.5, 2.0, 0.4, 0.04)
    tr.add(kick(0.9), 0.45)
    return reverb(echo(tr.b, 0.15, 0.3, 3), 0.2, 1.2), 3.4

def bass_drop():
    tr = Track(8)
    tr.add(hp(noise(N(1.6)), 2000) * np.linspace(0, 1, N(1.6)) ** 2 * 0.5, 0.0)
    tr.add(sweep(200, 2200, N(1.6)) * np.linspace(0, 1, N(1.6)) * 0.5, 0.0)
    for k in range(4): tr.add(kick(0.7), 0.8 + k * 0.2)
    tr.add(sub808(55, 1.8, 0.08, 2.0), 1.6, 1.0); tr.add(kick(1.0), 1.6); tr.add(clap(0.9), 2.0)
    for t0 in (1.6, 2.0, 2.35, 2.7): chord(tr, stab, ['A3', 'C4', 'E4', 'A4'], t0, 0.35, 0.55)
    return reverb(tr.b, 0.15, 1.0), 4.8

def future_bass_rise():
    tr = Track(8)
    prog = [['F4', 'A4', 'C5', 'E5'], ['G4', 'B4', 'D5', 'F5'], ['A4', 'C5', 'E5', 'G5'], ['B4', 'D5', 'F5', 'A5']]
    for i, notes in enumerate(prog):
        for nm in notes:
            n = N(0.55); pump = np.minimum(1, 0.25 + tt(n) / 0.12)
            tr.add(lp(supersaw(hz(nm), n, 0.014), 3800) * dec(n, 0.4, 0.003) * pump, i * 0.6, 0.5)
    tr.add(hp(noise(N(2.4)), 2500) * np.linspace(0, 1, N(2.4)) ** 2 * 0.35, 0.0)
    chord(tr, stab, ['C5', 'E5', 'G5', 'B5', 'D6'], 2.4, 1.8, 0.7); tr.add(sub808(65, 1.4, 0.05, 1.6), 2.4, 0.9)
    return reverb(tr.b, 0.25, 1.4), 5.2

def wake_up_wave():
    tr = Track(9)
    chord(tr, lambda f, d: padnote(f, d, 2.4, 1.2, 2200), ['C3', 'G3', 'B3', 'E4', 'G4'], 0.0, 6.0, 0.4)
    arp = ['C5', 'E5', 'G5', 'B5', 'C6', 'E6', 'G6', 'B6']
    for k, nm in enumerate(arp * 2): tr.add(ping(hz(nm), 0.8), 0.6 + k * 0.15, 0.25 + 0.5 * k / 16)
    for k in range(5): tr.add(kick(0.95), 2.6 + k * 0.46875)
    for k in (1, 3): tr.add(clap(0.8), 2.6 + k * 0.46875)
    chord(tr, stab, ['C5', 'E5', 'G5', 'B5'], 2.6, 1.6, 0.5)
    return reverb(echo(tr.b, 0.3, 0.3, 2), 0.3, 1.8), 6.2

def hard_reset():
    tr = Track(6)
    for g in range(6):
        for k in range(4):
            f = 1400 if k % 2 == 0 else 1000; t0 = g * 0.55 + k * 0.095
            tr.add(square(f, N(0.07)) * env(N(0.07), 0.002, 0.02, 0.8, 0.02), t0, 0.7)
        tr.add(sub808(55 * (1.12 if g % 2 else 1.0), 0.4, 0.03, 1.8), g * 0.55, 0.8)
    tr.add(sweep(300, 3000, N(0.7)) * np.linspace(0, 1, N(0.7)) * 0.5, 3.0)
    return reverb(tr.b, 0.1, 0.8), 4.2

def rocket_launch():
    tr = Track(8)
    rum = lp(noise(N(3.0)), 220) * np.linspace(0.05, 1, N(3.0)) ** 1.5
    tr.add(rum * 1.4, 0.0); tr.add(sweep(70, 1800, N(3.0)) * np.linspace(0.05, 1, N(3.0)) ** 2 * 0.6, 0.0)
    tr.add(hp(noise(N(3.0)), 3500) * np.linspace(0, 1, N(3.0)) ** 3 * 0.25, 0.0)
    tr.add(kick(1.0), 3.0); tr.add(sub808(50, 1.6, 0.06, 2.0), 3.0, 1.0)
    chord(tr, bell, ['C6', 'G6', 'C7', 'E7'], 3.0, 2.0, 0.5, 0.06)
    return reverb(tr.b, 0.25, 1.8), 5.2

def glitch_pop():
    tr = Track(7)
    scale = ['E5', 'G5', 'A5', 'B5', 'D6', 'E6']
    for g in range(8):
        f = hz(scale[(g * 2) % 6]); t0 = g * 0.5
        for r in range(3): tr.add(pop(f * (1 + 0.12 * r)), t0 + r * 0.06, 0.8)
    x = tr.b
    x = 0.55 * x + 0.45 * crush(x, 5, 3)
    tr2 = Track(7); tr2.b = x
    tr2.add(sweep(500, 2800, N(0.5)) * dec(N(0.5), 0.25) * 0.3, 3.9)
    chord(tr2, bell, ['E6', 'B6'], 3.9, 1.2, 0.5, 0.05)
    return reverb(echo(tr2.b, 0.19, 0.35, 3), 0.2, 1.2), 6.0

# id, display name, mood, recipe
CATALOG = [
    ('glass_drop', 'Glass Drop', 'GENTLE', glass_drop), ('lofi_morning', 'Lo-Fi Morning', 'GENTLE', lofi_morning),
    ('moonlight', 'Moonlight', 'GENTLE', moonlight), ('cloud_nine', 'Cloud Nine', 'GENTLE', cloud_nine),
    ('dreamy_harp', 'Dreamy Harp', 'GENTLE', dreamy_harp), ('velvet_keys', 'Velvet Keys', 'GENTLE', velvet_keys),
    ('mellow_drip', 'Mellow Drip', 'GENTLE', mellow_drip), ('kawaii_bells', 'Kawaii Bells', 'GENTLE', kawaii_bells),
    ('golden_hour', 'Golden Hour', 'GENTLE', golden_hour),
    ('bubble_pop', 'Bubble Pop', 'TRENDING', bubble_pop), ('crystal_ping', 'Crystal Ping', 'TRENDING', crystal_ping),
    ('soft_marimba', 'Soft Marimba', 'TRENDING', soft_marimba), ('sunrise_synth', 'Sunrise Synth', 'TRENDING', sunrise_synth),
    ('starlight_arp', 'Starlight Arp', 'TRENDING', starlight_arp), ('hyper_pluck', 'Hyper Pluck', 'TRENDING', hyper_pluck),
    ('glitch_pop', 'Glitch Pop', 'TRENDING', glitch_pop),
    ('neon_pulse', 'Neon Pulse', 'ENERGETIC', neon_pulse), ('retro_wave', 'Retro Wave', 'ENERGETIC', retro_wave),
    ('pixel_quest', 'Pixel Quest', 'ENERGETIC', pixel_quest), ('trap_tick', 'Trap Tick', 'ENERGETIC', trap_tick),
    ('digital_heartbeat', 'Digital Heartbeat', 'ENERGETIC', digital_heartbeat), ('pulse_drive', 'Pulse Drive', 'ENERGETIC', pulse_drive),
    ('power_up', 'Power Up', 'STRONG', power_up), ('bass_drop', 'Bass Drop Alert', 'STRONG', bass_drop),
    ('future_bass_rise', 'Future Bass Rise', 'STRONG', future_bass_rise), ('wake_up_wave', 'Wake-Up Wave', 'STRONG', wake_up_wave),
    ('hard_reset', 'Hard Reset', 'STRONG', hard_reset), ('rocket_launch', 'Rocket Launch', 'STRONG', rocket_launch),
]
TARGET_DB = {'GENTLE': -21, 'TRENDING': -19, 'ENERGETIC': -17, 'STRONG': -15}
OFFSET_DB = {'hard_reset': -3.0}   # square waves read louder than they measure
