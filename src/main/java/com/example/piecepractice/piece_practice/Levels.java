package com.example.piecepractice.piece_practice;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Levels extends JPanel {
	private CardLayout cardLayout;
	private PianoExercise pianoExercise;
	
	public Levels(CardLayout layout, PianoExercise exercise) {
		this.cardLayout = layout;
		this.setLayout(null);
		pianoExercise = exercise;
		initializeComponents();
    }
	
	private double levelAmount;
	private double maxPages;
	private double currentPage;
	private double currentLevel;
	
	private void initializeComponents() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		
		// Levels title
		JLabel title = new JLabel("Levels");
		title.setForeground(Color.green);
		Font font3 = title.getFont();
		title.setFont(new Font(font3.getFontName(), Font.PLAIN, 27));
		title.setBounds(608, 20, 100, 50);
		this.add(title);
		
		// Percentage bar
		PercentageBar percentageBar = new PercentageBar();
		percentageBar.setBackground(Color.gray);
		percentageBar.setBounds(50, 100, screenSize.width - 100, 10);
		levelAmount = percentageBar.getLevelAmount();
		currentLevel = percentageBar.getCurrentLevel();
		maxPages = Math.ceil(levelAmount / 5);
		this.add(percentageBar);
		
		// Create circle level buttons
		double buttonAmount = 0;
		if (levelAmount <= 5) {
			buttonAmount = levelAmount;
		}
		else if (levelAmount > 5) {
			buttonAmount = 5;
		}
		for (int i = 0; i < buttonAmount; i++) {
			LevelButton b;
			double level = i + 1;
			if (i + 1 <= currentLevel) {
				b = new LevelButton("Level " + String.valueOf(i + 1), true);
				b.setBackground(Color.green);
			}
			else {
				b = new LevelButton("Level " + String.valueOf(i + 1), false);
				b.setBackground(Color.gray);
			}
			b.setForeground(Color.white);
			b.setBounds(135 + 230 * i, 280, 100, 100);
			b.addActionListener(new ActionListener() {
	            @Override
	            public void actionPerformed(ActionEvent e) {
	                playExercise((int) level);
	            }
	        });
			this.add(b);
        }
		
		// Previous button
		JButton previousButton = new JButton("<");
		previousButton.setBackground(Color.black);
		previousButton.setForeground(Color.green);
		Font font = previousButton.getFont();
		previousButton.setFont(new Font(font.getFontName(), Font.PLAIN, 27));
		previousButton.setBounds(10, 305, 50, 50);
		previousButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goToPreviousLevelsPage();
            }
        });
		this.add(previousButton);
		
		// Next button
		JButton nextButton = new JButton(">");
		nextButton.setBackground(Color.black);
		nextButton.setForeground(Color.green);
		Font font2 = nextButton.getFont();
		nextButton.setFont(new Font(font.getFontName(), Font.PLAIN, 27));
		nextButton.setBounds(screenSize.width - 50 - 10, 304, 50, 50);
		nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                goToNextLevelsPage();
            }
        });
		this.add(nextButton);
		
    }

	protected void goToNextLevelsPage() {
		// Loop through all LevelButtons deleting them then recreating new ones based on how many are remaining.
		if (currentPage < maxPages) {
			currentPage++;
			Component[] components = this.getComponents();
	        for (Component component : components) {
	            if (component instanceof LevelButton) {
	            	this.remove(component);
	            }
	        }
	        this.repaint();
	        
	        // Create circle level buttons
	        double remainingLevels = levelAmount - (currentPage - 1) * 5;
			double buttonAmount = remainingLevels >= 5 ? 5 : remainingLevels;
			double firstLevelOnThisPage = 5 * currentPage - 4;
			for (int i = 0; i < buttonAmount; i++) {
				LevelButton b;
				if (firstLevelOnThisPage + i <= currentLevel) {
					b = new LevelButton("Level " + String.valueOf((int) firstLevelOnThisPage + i), true);
					b.setBackground(Color.green);
				}
				else {
					b = new LevelButton("Level " + String.valueOf((int) firstLevelOnThisPage + i), false);
					b.setBackground(Color.gray);
				}
				b.setForeground(Color.white);
				b.setBounds(135 + 230 * i, 280, 100, 100);
				this.add(b);
	        }
		}
	}
	
	protected void goToPreviousLevelsPage() {
		// Loop through all LevelButtons deleting them then recreating new ones based on how many are remaining.
		if (currentPage > 1) {
			currentPage--;
			Component[] components = this.getComponents();
	        for (Component component : components) {
	            if (component instanceof LevelButton) {
	            	this.remove(component);
	            }
	        }
	        this.repaint();
	        
	        // Create circle level buttons
	        double remainingLevels = levelAmount - (currentPage - 1) * 5;
			double buttonAmount = remainingLevels >= 5 ? 5 : remainingLevels;
			double firstLevelOnThisPage = 5 * currentPage - 4;
			for (int i = 0; i < buttonAmount; i++) {
				LevelButton b;
				if (firstLevelOnThisPage + i <= currentLevel) {
					b = new LevelButton("Level " + String.valueOf((int) firstLevelOnThisPage + i), true);
					b.setBackground(Color.green);
				}
				else {
					b = new LevelButton("Level " + String.valueOf((int) firstLevelOnThisPage + i), false);
					b.setBackground(Color.gray);
				}
				b.setForeground(Color.white);
				b.setBounds(135 + 230 * i, 280, 100, 100);
				this.add(b);
	        }
		}
	}
	
	protected void playExercise(int level) {

		if (level <= currentLevel) {
			// Take them to the card "Piano Exercise" and
			// pass it the level attributes as an argument
			pianoExercise.setLevel(level);
			cardLayout.show(getParent(), "Piano Exercise");
		}
	}
	

	public void update() {
		this.removeAll();
		initializeComponents();
		revalidate();
		repaint();
	}
}
