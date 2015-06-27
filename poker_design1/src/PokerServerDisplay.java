/*
 * PokerServerDisplay.java
 *
 * George Ferguson, ferguson@cs.rochester.edu,  6 Oct 1998
 * Time-stamp: <Sun Oct 31 11:01:55 EST 1999 ferguson>
 */

import java.awt.*;
import java.awt.event.*;

public class PokerServerDisplay extends Thread implements ActionListener {
    // Members
    protected PokerFrame frame;
    protected PokerGame game;
    protected Button controlButton;
    // Methods
    public PokerServerDisplay(PokerGame g, String displaySize) {
	game = g;
	frame = new PokerFrame(displaySize, g.getPlayers(), g.getPot());
	frame.setTitle("IPP Poker Server");
	controlButton = new Button("Start");
	controlButton.addActionListener(this);
	frame.addButton(controlButton);
    }
    public void run() {
	while (true) {
	    frame.update();
	    suspend();
	}
    }
    public void setTitle(String s) {
	frame.setTitle(s);
    }
    public void addPlayer(Player p) {
	frame.addPlayer(p);
    }
    public void displayMsg(String msg) {
	frame.displayMsg(msg);
    }
    public void setStatus(String msg) {
	frame.setStatus(msg);
    }
    public void setPaused(boolean b) {
	controlButton.setEnabled(b);
    }
    public void addCommonCard(Card c) {
	frame.addCommonCard(c);
    }
    public void clearCommonCards() {
	frame.clearCommonCards();
    }
    // Button callback
    public void actionPerformed(ActionEvent evt) {
	String arg = evt.getActionCommand();
	if (arg.equals("Start")) {
	    controlButton.setLabel("Next");
	}
	controlButton.setEnabled(false);
	game.unpause();
    }
}
