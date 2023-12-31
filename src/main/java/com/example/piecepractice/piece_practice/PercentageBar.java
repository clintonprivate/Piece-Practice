package com.example.piecepractice.piece_practice;

import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.JPanel;

public class PercentageBar extends JPanel {
	@Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = getWidth();
        int height = getHeight();

        // Define the area for the red half (left side)
        double currentLevel = getCurrentLevel();
        double levelAmount = getLevelAmount();
        double percent = currentLevel / levelAmount;
        double redWidth = width * percent;

        // Set the color to red and fill the left half
        g.setColor(Color.green);
        g.fillRect(0, 0, (int) redWidth, height);
    }

	public double getCurrentLevel() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/currentlevel.txt"));
            String line = reader.readLine();
            return Double.parseDouble(line.trim());
        } catch (IOException | NumberFormatException e) {
        }
        return 1;
    }
	
	public double getLevelAmount() {
		String filePath = "src/main/resources/levels.txt";
        int lineCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            while (reader.readLine() != null) {
                lineCount++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lineCount;
	}
}
