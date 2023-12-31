package com.example.piecepractice.piece_practice;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChooseSong extends JPanel {
	private CardLayout cardLayout;
	
	public ChooseSong(CardLayout layout) {
		this.cardLayout = layout;
		this.setLayout(null);
		initializeComponents();
    }
	
	private void initializeComponents() {
		// Musescore link text
		JLabel l = new JLabel("Musescore Link: ");
		Font font = l.getFont();
        l.setFont(new Font(font.getFontName(), Font.PLAIN, 20));
        l.setForeground(Color.GREEN);
		l.setBounds(230,300,600,40);
		this.add(l);
		
		// Musescore link input bar
		JTextPane t = new JTextPane();
        t.setFont(new Font(font.getFontName(), Font.PLAIN, 20));
        t.setBounds(400,300,600,40);
        Insets insets = new Insets(7, 10, 0, 0);
        t.setMargin(insets);
        t.setText("https://musescore.com/user/4421861/scores/12811249");
		this.add(t);
		
		// Practice button
		JButton b=new JButton("Practice");
		b.setBackground(Color.green);
		b.setForeground(Color.white);
		b.setBounds(1000,300,100, 40); 
		b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(getParent(), "Levels");
            }
        });
		this.add(b);
    }
}
