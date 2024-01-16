import io
import os
import random
import base64
from PIL import Image
from music21 import *

basedirectory = ""
songsDirectory = "C:\\Users\\pc\\eclipse-workspace\\piece-practice\\src\\main\\resources\\sounds\\songsyoulike"
snippetCriteria = "| D major | Treble clef | Single note | Intervals of 1 | Sixteenth note |"
d_major_scale = ["B3",
                 "C4", "C#4", "D4", "E4", "F#4", "G4", "A4", "B4",
                 "C5", "C#5", "D5", "E5", "F#5", "G5", "A5", "B5",]

keySignatureDictionary = {
    "| C major |": 0,
    "| D major |": 2,
    "| E major |": 4,
}

def pickRandomSong():
    files = [f for f in os.listdir(songsDirectory) if os.path.isfile(os.path.join(songsDirectory, f))]
    randomFile = random.choice(files)
    filePath = "songsyoulike/" + randomFile
    return filePath

def omitBassClef(score):
    score.parts[1].activeSite.remove(score.parts[1])
    return score

def find_longest_matching_interval(maxInterval, score):
    allPortions = []
    currentPortion = []
    previousPitch = -1
    # Loop through the notes in treble clef
    for index, measure in enumerate(score.parts[0].getElementsByClass('Measure')):
        for note in measure.notes:
            noteName = str(note.pitch)
            pitch = d_major_scale.index(noteName)
            interval = abs(pitch - previousPitch)
            if (interval > maxInterval and previousPitch > -1):
                allPortions.append(currentPortion)
                currentPortion = []
            currentPortion.append(note)
            previousPitch = pitch
        if index == len(score.parts[0].getElementsByClass('Measure')) - 1:
            allPortions.append(currentPortion)
    longestMatchingPortion = max(allPortions, key=len)
    return longestMatchingPortion

def transposeMelody(score):
    for keyName in keySignatureDictionary:
        if keyName in snippetCriteria:
            score.parts[0].keySignature = key.KeySignature(keySignatureDictionary[keyName])
    return score

def seperateSnippet(snippet):
    new_score = stream.Score()
    treble_part = stream.Part()
    for note in snippet:
        treble_part.append(note)
    new_score.append(treble_part)
    new_score = transposeMelody(new_score)
    return new_score

def extractPlayableSnippet(inputFile):
    # Remove bass clef if necessary for simplicity
    score = converter.parse(inputFile)
    if ("| Treble and bass clef |" in snippetCriteria) == False:
        score = omitBassClef(score)
    
    # Find longest portion of the song with max intervals of 1
    snippet = find_longest_matching_interval(1, score)

    # Extract the snippet from the whole melody
    score = seperateSnippet(snippet)

    # Create sheet musicc image and crop height to the single bar
    bytes_data1 = score.write('musicxml.png', fp=(basedirectory + "output.png")).read_bytes()
    bytes_data2 = score.write('midi', fp=(basedirectory + "output.mid"))
    img_stream = io.BytesIO(bytes_data1)
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

extractPlayableSnippet("src/main/resources/sounds/" + pickRandomSong())
