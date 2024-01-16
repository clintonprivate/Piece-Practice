/*
 * One thing that this class is going to do is that its going to get
 * it's level attributes and pass it to a python script which will
 * return two images one of treble clef and/or bass clef in base64
 * as well as one base64 of the melody that was generated by the
 * Python script. Then Java will continue from there.
 */

package com.example.piecepractice.piece_practice;

import java.awt.CardLayout;
import javax.sound.midi.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;

import javax.sound.midi.*;
import javax.sound.sampled.*;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToLongFunction; 

public class PianoExercise extends JPanel {
	private CardLayout cardLayout;
	private BufferedImage bufferedImage;
	private JPanel sheetMusic;
	private double currentNotePosition = 0;
	private int beatsPerMinute = 60;
	private List<String> allNotes;
	private static String levelAttributes = "";
	private static char levelPart;
	private JLabel partText;
	private int amountOfCorrectNotes;
	private boolean listening = false;
	private static HelperMethods helperMethods = new HelperMethods();
	private boolean userStartedPlaying = false;
	List<Integer> correctNoteTimestamps;
	List<String[]> userInputs = new ArrayList<>();
	private static boolean isPlaytimeLevel = false;
	
	public PianoExercise(CardLayout layout) {
		this.cardLayout = layout;
		this.setLayout(null);
		if (levelAttributes.equals("") == false) {
			initializeComponents('l');
		}
    }
	
	private void initializeComponents(char levelpart) {
		// Clear previous elements
		this.removeAll();
		
		// Generate a random melody
		levelPart = levelpart;
		String[] melody = generateRandomMelody();
		String sheetMusicBase64 = melody[0];
		String midiBase64 = melody[1];
		Sequence midi = helperMethods.convertBase64ToMidi(midiBase64);
		correctNoteTimestamps = extractNoteTimestamps(midi);
		amountOfCorrectNotes = 0;
		currentNote = 0;
		currentNotePosition = 0;
		userStartedPlaying = false;
		userInputs.clear();
		
		byte[] imageBytes = Base64.getDecoder().decode(sheetMusicBase64);
        ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
        try {
			bufferedImage = ImageIO.read(bis);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
        // Current level part text
        partText = new JLabel("Loading...");
        if (levelPart == 'a') {
        	partText = new JLabel("Part A");
        }
        else if (levelPart == 'b') {
        	partText = new JLabel("Part B");
        }
		Font font3 = partText.getFont();
		partText.setFont(new Font(font3.getFontName(), Font.PLAIN, 27));
		partText.setBounds(608, 20, 150, 50);
		this.add(partText);
        
		// Sheet music image
		sheetMusic = new JPanel() {
			@Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int panelWidth = getWidth();
                int panelHeight = getHeight();
                Image scaledImage = bufferedImage.getScaledInstance(panelWidth, panelHeight, Image.SCALE_SMOOTH);
                g.drawImage(scaledImage, 0, 0, this);
            }
        };
		sheetMusic.setBackground(Color.red);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		sheetMusic.setBounds(0, 220, screenSize.width, 200);
		this.add(sheetMusic);
		
		// Get all the correct notes to follow along and validate user's playing
		allNotes = getAllNotesFromMidi(midi);
		
		helperMethods.startMetronome(beatsPerMinute);
		
		// Update exercise
		revalidate();
    	repaint();
		
		// Listen for MIDI input
		if (!listening) {
			listening = true;
			listenForMidiInput();
		}
    }
	
	private static String[] generateRandomMelody() {
	    String[] javaArray = null;
	    try {
	    	String scriptToUse = isPlaytimeLevel ? "songsyoulike.py" : "generatemelody.py";
	        String[] command = {
	            "python",
	            "src/main/resources/python/generate_melody/" + scriptToUse,
	            levelAttributes
	        };
	        ProcessBuilder processBuilder = new ProcessBuilder(command);
	        Process process = processBuilder.start();
	        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        String line;
	        StringBuilder output = new StringBuilder();
	        while ((line = reader.readLine()) != null) {
	            output.append(line).append("\n");
	        }
	        int exitCode = process.waitFor();
	        if (exitCode == 0) {
	            String pythonOutput = output.toString().replace("[", "").replace("]", "").trim();
	            javaArray = pythonOutput.split(",");
	            for (int i = 0; i < javaArray.length; i++) {
	                javaArray[i] = javaArray[i].replaceAll("'", "").trim();
	            }
	        } else {
	        	BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
	            StringBuilder errorOutput = new StringBuilder();
	            String errorLine;
	            while ((errorLine = errorReader.readLine()) != null) {
	                errorOutput.append(errorLine).append("\n");
	            }

	            System.err.println("Python script execution failed. Error output:\n" + errorOutput.toString());
	        }
	    } catch (IOException | InterruptedException e) {
	        e.printStackTrace();
	    }
	    return javaArray;
	}
	
	private void listenForMidiInput() {
		new Thread(new Runnable() {
			@Override
			public void run() {
			    try {
			        final Synthesizer synth = MidiSystem.getSynthesizer();
			        synth.open();
			        Transmitter transmitter = MidiSystem.getTransmitter();
			        Receiver receiver = new Receiver() {
			            @Override
			            public void send(MidiMessage message, long timeStamp) {
			                if (message instanceof ShortMessage) {
			                    ShortMessage sm = (ShortMessage) message;
			                    int note = sm.getData1();
			                    int velocity = sm.getData2();
			                    if (sm.getCommand() == ShortMessage.NOTE_ON && velocity > 0 && currentNote <= allNotes.size() - 1) {
			                    	String playedNote = helperMethods.getNoteName(note);
			                    	userInputs.add(new String[]{playedNote, Long.toString(timeStamp)});
			                    	if (userStartedPlaying == false) {
			                    		userStartedPlaying = true;
			                    		startTrackingRhythm(timeStamp);
			                    	}
			                    }
			                }
			            }

						@Override
			            public void close() {
			                // Close resources if needed
			                synth.close();
			            }
			        };
			        transmitter.setReceiver(receiver);
			        System.out.println("Listening for MIDI input...");
			        
			        // Sleep to keep the program running without consuming excessive CPU
			        // Try to improve efficiency here to stop computer from overheating
			        Thread.sleep(Long.MAX_VALUE);
			        
			    } catch (MidiUnavailableException | InterruptedException e) {
			        e.printStackTrace();
			    }
			}
		}).start();
	}

	private int currentNote = 0;
	
	private void startTrackingRhythm(long firstBeat) {
		// Loop through the correct timestamps on beat comparing the user's rhythm to the correct one.
		new Thread(new Runnable() {
			@Override
			public void run() {
				int lastCorrect = 0;
				int offset = 0;
				boolean previousCorrect = true;
				for (int correct : correctNoteTimestamps) {
					try {
						// Calculate the wait duration based on the distance to the next note.
						int precisionTolerance = 1000;
						int waitFor = (correct - lastCorrect) / 10;
						Thread.sleep(waitFor + precisionTolerance - (previousCorrect ? offset : 0));
						
						// Check if the next user timestamp is correct
						long userTimestamp = Long.valueOf(userInputs.get(currentNote)[1]);
						String playedNote = userInputs.get(currentNote)[0];
						int comparable = (int) (userTimestamp - firstBeat) / 100;
						System.out.println(comparable + " vs " + correct);
					    if (comparable >= correct - precisionTolerance && comparable <= correct + precisionTolerance) {
							System.out.println("Rhythm correct");
							paintNextNote(playedNote);
							previousCorrect = true;
						}
						else {
							System.out.println("Rhythm wrong");
							paintNextNote("X");
							previousCorrect = false;
						}
						
						// Update last correct to be our current correct timestamp
						offset = comparable - correct;
						lastCorrect = correct;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}
	
	private void paintNextNote(String playedNote) {
		int averageNoteDistance = 138;
		int startDistance = 390;
		int width = bufferedImage.getWidth();
	    int height = bufferedImage.getHeight();
	    double averageNoteWidth = 50;
	    double startX = currentNotePosition;
	    double endX = 0;
	    if (currentNote == 0) {
	    	endX = currentNotePosition + startDistance;
	    }
	    else if (currentNote == allNotes.size() - 1) {
	    	endX = width;
	    	switchOrEndExercise();
	    }
	    else {
	    	double note1 = getNotePositionAfter(startX + averageNoteWidth);
	    	double note2 = getNotePositionAfter(note1 + averageNoteWidth);
	    	endX = note2 - Math.round((note2 - note1)/2) + 10;
	    }
	    
	    // Create a new BufferedImage with the same dimensions and type as the original image
	    BufferedImage modifiedImage = new BufferedImage(width, height, bufferedImage.getType());

	    // Get the graphics context from the modified image
	    Graphics2D g = modifiedImage.createGraphics();

	    // Draw the original image onto the modified image
	    g.drawImage(bufferedImage, 0, 0, null);
	    
	    // Set the color to green for non-white pixels within the specified x range
	    Color color = Color.GREEN;
	    if (playedNote.equals(allNotes.get(currentNote))) {
	    	color = Color.GREEN;
	    	amountOfCorrectNotes++;
	    }
	    else {
	    	color = Color.RED;
	    }
	    for (int y = 0; y < height; y++) {
	        for (int x = (int) startX; x < endX && x < width; x++) {
	            int pixel = bufferedImage.getRGB(x, y);
	            if (pixel != Color.WHITE.getRGB()) {
	                modifiedImage.setRGB(x, y, color.getRGB());
	            }
	        }
	    }
	    
	    // Dispose the graphics context
	    currentNote++;
	    currentNotePosition = endX;
	    g.dispose();

	    // Update the bufferedImage with the modified image
	    bufferedImage = modifiedImage;

	    // Repaint the JPanel to display the updated image
	    repaint();
	}
	
	public int getNotePositionAfter(double xCoordinate) {
		// Load the image
    	int clusterThreshold = 0;
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        
        // Ensure the x coordinate is within the image bounds
        if (xCoordinate >= width) {
            return -1;
        }

        int consecutiveChanges = 0;
        int previousPixelCount = 0;

        for (int x = (int) xCoordinate; x < width; x++) {
            int pixelsCount = 0;

            // Scan vertically from the current x coordinate
            for (int y = 0; y < height; y++) {
                int pixel = bufferedImage.getRGB(x, y);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = (pixel) & 0xff;

                // Check if the pixel is not white (assuming RGB format)
                if (red + green + blue < 765) {
                    pixelsCount++;
                }
            }

            // Check for a rapid change in pixel count
            if (Math.abs(previousPixelCount - pixelsCount) > clusterThreshold) {
                consecutiveChanges++;
                if (consecutiveChanges > 5) { // Adjust the number of consecutive changes needed
                    return x - consecutiveChanges; // Return the start of the cluster
                }
            } else {
                consecutiveChanges = 0;
            }

            previousPixelCount = pixelsCount;
        }

        // If no cluster is found, return -1
        return -1;
    }
	
	private void switchOrEndExercise() {
		Thread wait = new Thread(() -> {
            try {
                Thread.sleep(3000);
                if (levelPart == 'a') {
                	if (amountOfCorrectNotes == allNotes.size()) {
                		initializeComponents(('b'));
                	}
                	else {
                		initializeComponents(('a'));
                	}
                }
                else if (levelPart == 'b') {
                	if (amountOfCorrectNotes == allNotes.size()) {
                		PercentageBar percentageBar = new PercentageBar();
                		int latestLevel = (int) percentageBar.getCurrentLevel();
                		if (levelAttributes.equals(helperMethods.getLevelAttributes(latestLevel))) {
                			helperMethods.incrementLevel();
                		}
                		JPanel cards = (JPanel) getParent();
                		Component[] components = cards.getComponents();
                        for (Component component : components) {
                        	if (component instanceof Levels) {
                        		((Levels) component).update();
                        	}
                        }
                		cardLayout.show(getParent(), "Levels");
                	}
                	else {
                		initializeComponents(('b'));
                	}
                }
            } catch (InterruptedException e) {
            }
        });
        wait.start();
	}
	
	static class NoteEvent implements Comparable<NoteEvent> {
        int note;
        long tick;
        
        public NoteEvent(int note, long tick) {
            this.note = note;
            this.tick = tick;
        }

        @Override
        public int compareTo(NoteEvent other) {
            return Long.compare(this.tick, other.tick);
        }
    }
	
	public static java.util.List<String> getAllNotesFromMidi(Sequence sequence) {
		java.util.List<NoteEvent> noteEvents = new java.util.ArrayList<>();

        // Iterate through tracks in the MIDI sequence
        Track[] tracks = sequence.getTracks();
        for (Track track : tracks) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();

                // Check if the message is a note-on event
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage) message;
                    if (sm.getCommand() == ShortMessage.NOTE_ON) {
                        int note = sm.getData1();
                        long tick = event.getTick();
                        noteEvents.add(new NoteEvent(note, tick));
                    }
                }
            }
        }

        // Sort note events based on timing
        noteEvents.sort(NoteEvent::compareTo);

        // Convert note events to note names
        java.util.List<String> noteNames = new java.util.ArrayList<>();
        for (NoteEvent noteEvent : noteEvents) {
            String noteName = helperMethods.getNoteName(noteEvent.note);
            noteNames.add(noteName);
        }
        return noteNames;
    }
	
	public void setLevel(boolean playtimeLevel, int level) {
		levelAttributes = helperMethods.getLevelAttributes(level);
		isPlaytimeLevel = playtimeLevel;
		initializeComponents('a');
	}

	private List<Integer> extractNoteTimestamps(Sequence sequence) {
		// A sequence consists of tracks; each track contains MIDI events
        Track[] tracks = sequence.getTracks();
        List<Integer> timestamps = new ArrayList<>();

        // Iterate through tracks and extract timestamps of note events
        for (Track track : tracks) {
            for (int i = 0; i < track.size(); i++) {
                MidiEvent event = track.get(i);
                MidiMessage message = event.getMessage();

                // Check if the MIDI message is a Note On event (Note Off events can also be considered)
                if (message instanceof ShortMessage && ((ShortMessage) message).getCommand() == ShortMessage.NOTE_ON) {
                    int timestamp = (int) event.getTick();
                    timestamps.add(timestamp);
                }
            }
        }

        return timestamps;
	}
}

