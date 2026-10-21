# Sound generator

Every bundled alarm sound in Aggrey DayFlow is **original**: it is synthesized from mathematics (oscillators, FM synthesis,
filters, reverb) by the scripts in this folder. No recordings, samples or third-party audio are used, so there are no
sample licences to track. The sounds are part of this project and share its licence.

- `engine.py`: the small synthesizer (oscillators, envelopes, filters, reverb, drums).
- `recipes.py`: one function per sound, plus the catalog (id, display name, mood).
- `render.py`: renders every recipe, normalises loudness per mood, writes `app/src/main/res/raw/<id>.ogg`, and prints a
  quality table (clipping, silence, loudness, abrupt endings).

Regenerate (needs Python 3, `pip install numpy scipy`, and `ffmpeg`):

    python tools/sound-generator/render.py

The four "Classic" sounds (`chime`, `bells`, `beep`, `rising`) came from an earlier version of the same generator.
After changing the catalog, update `domain/Sounds.kt` and `alarm/SoundResources.kt` to match; `SoundCatalogTest` fails if the
catalog and the audio files disagree.
