package com.example.piecepractice.piece_practice;

import java.awt.Color;
import java.awt.*;
import javax.swing.*;

public class App {
	public static void main(String[] args) {  
		// Create the window
		JFrame f = new JFrame();
		f.setTitle("Piece Practice");
		f.setSize(500,400);
		f.setExtendedState(JFrame.MAXIMIZED_BOTH);
		
		// Create CardLayout to change pages
        CardLayout cardLayout = new CardLayout();
        JPanel cardPanel = new JPanel(cardLayout);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        cardPanel.setBounds(0, 0, screenSize.width, screenSize.height);
        cardPanel.setBackground(Color.blue);
        f.add(cardPanel);
        
        // Add the pages
        ChooseSong chooseSong = new ChooseSong(cardLayout);
        cardPanel.add(chooseSong, "Choose Song");
        PianoExercise pianoExercise = new PianoExercise(cardLayout);
        cardPanel.add(pianoExercise, "Piano Exercise");
        Levels levels = new Levels(cardLayout, pianoExercise);
        cardPanel.add(levels, "Levels");
        
        // Start at the choose song page
        cardLayout.show(cardPanel, "Levels");
        
        // Adjust window settings
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLocationRelativeTo(null);
		f.setVisible(true);
		
		System.out.println(Math.ceil(88.2));
	}  
}  