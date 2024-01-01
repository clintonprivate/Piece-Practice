import io
import sys
import random
import base64
from PIL import Image
from music21 import stream, note, duration, clef, environment, key, pitch

# Declare the attributes we are working with
levelAttributes = sys.argv[1]
environment.set('musescoreDirectPNGPath', "C:\\Program Files\\MuseScore 4\\bin\\MuseScore4.exe")
basedirectory = "src/main/resources/python/generate_melody/"
amountOfMeasures = 4
beatsPerMeasure = 4
amountOfBeats = amountOfMeasures * beatsPerMeasure
durationDictionary = {
    "| Whole note |":        [4],
    "| Half note |":         [4, 2],
    "| Quarter note |":      [4, 2, 1],
    "| Eighth note |":       [4, 2, 1, 0.5],
    "| Sixteenth note |":    [4, 2, 1, 0.5, 0.25],
    "| With ties |":         [4, 2, 1, 0.5, 0.25],
    "| With dotted notes |": [4, 2, 1, 0.5, 0.25]
}

intervalDictionary = {
    "| Intervals of 1 |": [1],
    "| Intervals of 2 |": [1, 2],
    "| Intervals of 3 |": [1, 2, 3],
    "| Intervals of 4 |": [1, 2, 3, 4],
    "| Intervals of 5 |": [1, 2, 3, 4, 5],
    "| Intervals of 6 |": [1, 2, 3, 4, 5, 6],
    "| Intervals of 7 |": [1, 2, 3, 4, 5, 6, 7],
    "| Intervals of 8 |": [1, 2, 3, 4, 5, 6, 7, 8],
}

def setDurationChoices():
    durationChoices = []
    for key in durationDictionary:
        if key in levelAttributes:
            durationChoices = durationDictionary[key]
    return durationChoices

def setIntervalChoices():
    intervalChoices = []
    for key in intervalDictionary:
        if key in levelAttributes:
            intervalChoices = intervalDictionary[key]
    return intervalChoices

# Generate notes with the level attributes
def generate_random_notes():
    notes_stream = stream.Score()
    d_major = key.KeySignature(2)
    staff = stream.Part()
    staff.append(d_major)

    # D major scale: D, E, F#, G, A, B, C#
    d_major_scale = ["C#4", "D4", "E4", "F#4", "G4", "A4", "B4",
                     "C#5", "D5", "E5", "F#5", "G5", "A5", "B5",]
    
    lastNoteGenerated = random.choice(d_major_scale)
    entireMelodyDuration = 0

    # Take ties setting into account during note generation
    if "| With ties |" in levelAttributes:
        while entireMelodyDuration < amountOfBeats:
            lastNotePosition = d_major_scale.index(lastNoteGenerated)
            newNotePosition = lastNotePosition + random.choice([1, -1]) * random.choice(intervalChoices)
            while newNotePosition < 0 or newNotePosition > len(d_major_scale) - 1:
                newNotePosition = lastNotePosition + random.choice([1, -1]) * random.choice(intervalChoices)
            new_note = note.Note(d_major_scale[newNotePosition])
            randomDuration = random.choice(durationChoices)
            while entireMelodyDuration + randomDuration > amountOfBeats:
                randomDuration = random.choice(durationChoices)
            new_note.duration = duration.Duration(randomDuration)
            staff.append(new_note)
            lastNoteGenerated = d_major_scale[newNotePosition]
            lastNotePosition = newNotePosition
            entireMelodyDuration += randomDuration
    else:
        visualLength = 0
        maxVisualLength = 128
        for i in range(4):
            if visualLength >= maxVisualLength:
                break
            measureDuration = 0
            while measureDuration < 4:
                lastNotePosition = d_major_scale.index(lastNoteGenerated)
                newNotePosition = lastNotePosition + random.choice([1, -1]) * random.choice(intervalChoices)
                while newNotePosition < 0 or newNotePosition > len(d_major_scale) - 1:
                    newNotePosition = lastNotePosition + random.choice([1, -1]) * random.choice(intervalChoices)
                new_note = note.Note(d_major_scale[newNotePosition])
                randomDuration = random.choice(durationChoices)
                while measureDuration + randomDuration > 4:
                    randomDuration = random.choice(durationChoices)
                new_note.duration = duration.Duration(randomDuration)
                staff.append(new_note)
                lastNoteGenerated = d_major_scale[newNotePosition]
                lastNotePosition = newNotePosition
                measureDuration += randomDuration
                visualLength += 1 / randomDuration

    notes_stream.append(staff)
    
    return notes_stream

# Creating a stream and adding the notes sequence to it
durationChoices = setDurationChoices()
intervalChoices = setIntervalChoices()
music_stream = generate_random_notes()

# Converting the stream to a sheet music image
bytes_data1 = music_stream.write('musicxml.png', fp=(basedirectory + "output.png")).read_bytes()
bytes_data2 = music_stream.write('midi', fp=(basedirectory + "output.mid"))
img_stream = io.BytesIO(bytes_data1)

# Crop height to the single bar
img = Image.open(img_stream)
width, height = img.size
box = (300, 500, width - 150, 900)
img = img.crop(box)

# Return the image as a string
returnToJava = []
img_stream = io.BytesIO()
img.save(img_stream, format='PNG')
img_stream.seek(0)
base64_image = base64.b64encode(img_stream.read()).decode('utf-8')
returnToJava.append(base64_image)

# Return the MIDI as a string
with open(basedirectory + "output.mid", 'rb') as file:
    midi_bytes = file.read()
base64_midi = base64.b64encode(midi_bytes).decode('utf-8')
returnToJava.append(base64_midi)

# Print returnToJava contents for Java
print(returnToJava)
