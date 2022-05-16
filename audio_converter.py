from os import path
from pydub import AudioSegment

# Requirements:
# pip install pydub
# sudo apt install ffmpeg

# Files
src = "testAAC.aac"
dst = "test.wav"

# Convert from .aac to .wav
sound = AudioSegment.from_file(src, "aac")
sound.export(dst, format="wav")
