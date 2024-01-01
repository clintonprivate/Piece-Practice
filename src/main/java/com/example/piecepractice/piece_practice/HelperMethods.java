package com.example.piecepractice.piece_practice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class HelperMethods {
	public int currentBeat = 0;
	private boolean metronomeStarted = false;
	
	public void incrementLevel() {
		try {
			String filePath = "src/main/resources/currentlevel.txt";
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line = reader.readLine();
            int number = Integer.parseInt(line);
            reader.close();
            number++;
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write(String.valueOf(number));
            writer.close();
        } catch (IOException e) {
        }
	}
	
	public String getLevelAttributes(int level) {

		try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/levels.txt"))) {
            String line;
            int currentLine = 0;

            while ((line = reader.readLine()) != null) {
                if (currentLine == level - 1) {
                    return line;
                }
                currentLine++;
            }
        } catch (IOException e) {
        }
		return null;
	}
	
	public void startMetronome(int beatsPerMinute) {
		if (metronomeStarted == false) {
			metronomeStarted = true;
			int millisecondsPerBeat = 60000 / beatsPerMinute;
		    Thread metronomeThread = new Thread(new Runnable() {
		        @Override
		        public void run() {
		            try {
		                while (true) {
		                	if (currentBeat % 4 == 0) {
		                		playTickSound(1);
		                	}
		                	else {
		                		playTickSound(2);
		                	}
		                	currentBeat++;
		                    Thread.sleep(millisecondsPerBeat);
		                }
		            } catch (InterruptedException e) {
		                e.printStackTrace();
		            }
		        }
		    });

		    metronomeThread.start();
		}
	}

	protected void playTickSound(int tickType) {

		try {
            AudioInputStream audioInputStream = null;
            if (tickType == 1) {
            	audioInputStream = AudioSystem.getAudioInputStream(getClass().getResourceAsStream("/sounds/metronome1.wav"));
            }
            else if (tickType == 2) {
            	audioInputStream = AudioSystem.getAudioInputStream(getClass().getResourceAsStream("/sounds/metronome2.wav"));
            }

            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	public Sequence convertBase64ToMidi(String midiBase64) {
        // Convert Base64 string to byte array
        byte[] midiByteArray = java.util.Base64.getDecoder().decode(midiBase64);

        try {
            // Create a Sequence from the byte array
            return MidiSystem.getSequence(new java.io.ByteArrayInputStream(midiByteArray));
        } catch (InvalidMidiDataException | java.io.IOException e) {
            e.printStackTrace();
        }

        // Return null if conversion fails
        return null;
    }
	
	public static String getNoteName(int note) {
        String[] noteNames = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        int octave = (note / 12) - 1;
        int noteIndex = note % 12;
        return noteNames[noteIndex] + octave;
    }
}
