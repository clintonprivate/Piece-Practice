package com.example.piecepractice.piece_practice;

import javax.swing.*;
import java.awt.*;

public class LevelButton extends JButton {
	private boolean allowColorChange;
	
    public LevelButton(String label, boolean changeColor) {
        super(label);
        setContentAreaFilled(false); // Make the button transparent
        setFocusPainted(false); // Remove the default focus indication
        setPreferredSize(new Dimension(100, 100)); // Set preferred size for a circular button
        allowColorChange = changeColor;
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (getModel().isArmed() && allowColorChange) {
            g.setColor(Color.lightGray); // Change color when the button is pressed
        } else {
            g.setColor(getBackground());
        }

        g.fillOval(0, 0, getSize().width - 1, getSize().height - 1);
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        g.setColor(getBackground());
        g.drawOval(0, 0, getSize().width - 1, getSize().height - 1);
    }
}
