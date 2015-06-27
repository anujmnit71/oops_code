/*
 * DrawPokerGame.java
 *
 * George Ferguson, ferguson@cs.rochester.edu, 29 Oct 1999
 * Time-stamp: <Thu Jan 19 14:28:50 EST 2006 ferguson>
 */

import java.util.*;

public class DrawPokerGame extends PokerGame {
    // Constructor
    public DrawPokerGame(int low, int high, int raises, boolean cspeak,
			 boolean useDisplay, String displaySize) {
	super(low, high, raises, cspeak, useDisplay, displaySize);
	display.setTitle("IPP Poker Server - Draw Poker");
    }
    //
    // PokerGame Methods
    //
    public String gameTypeString() {
	return "DRAW " + lowLimit + " " + highLimit + " " +
	    maxRaisesPerRound;
    }
    public int maxCardsPerPlayer() {
	return 5;
    }
    public void playAHand() {
	resetForHand();
	pause("Ready to collect ante", 2);
	ante(lowLimit/2);
	pause("Ready to deal", 2);
	deal();
	try {
	    pause("Ready for first betting round", 3);
	    bettingRound(lowLimit, true);
	    pause("Ready for draw", 3);
	    draw();
	    pause("Ready for second betting round", 3);
	    bettingRound(highLimit, false);
	} catch (OnePlayerLeftException ex) {
	    // Nothing to do, will handle in showdown()
	}
	pause("Ready for showdown", 4);
	ShowdownResult result = showdown(players);
	pause("Ready for payoff", 4);
	payoff(players, pot.getCash(), result, 25);
	pause("Ready to clean up", 7);
	cleanup();
    }
    private void deal() {
	for (int i=0; i < players.size(); i++) {
	    Player p = players.nth(i);
	    if (!p.isBusted()) {
		String msg = "DEAL";
		for (int j=0; j < 5; j++) {
		    Card c = deck.deal1();
		    p.addCard(c);
		    msg = msg + " " + c.toString();
		}
		sendTo(p, msg);
	    }
	}
	updateDisplay();
    }
    private void draw() throws OnePlayerLeftException {
	for (int i = 0; i < players.size(); i++) {
	    Player p = players.nth(i);
	    if (!p.isFolded() && !p.isBusted()) {
		drawPlayer(p);
	    }
	}
    }
    private void drawPlayer(Player p) throws OnePlayerLeftException {
	//System.err.println("drawing for " + p.getName());
	pause("Ready to ask " + p.getName() + " DRAW?", 6);
	int numBogusMessages = 0;
	while (numBogusMessages < MAX_BOGUS_MESSAGES) {
	    sendTo(p, "DRAW?");
	    String msg = recvFrom(p);
	    if (msg == null) {
		handlePlayerQuit(p);
		return;
	    } else if (!Utilities.startsWithIgnoreCase(msg, "DRAW")) {
		sendTo(p, "ERROR Expected DRAW response");
	    } else {
		msg = msg.substring(4).trim();
		StringTokenizer tokens = new StringTokenizer(msg);
		try {
		    // How many cards are being drawn
		    int n = new Integer(tokens.nextToken()).intValue();
		    //System.err.println("drawing " + n + " cards");
		    if (n < 0 || n > 4) {
			sendTo(p, "ERROR bad number of cards to draw: " + n);
		    } else {
			// Check that they have them
			boolean bogus = false;
			CardVector discards = new CardVector();
			for (int i=0; i < n; i++) {
			    Card c = Card.fromString(tokens.nextToken());
			    //System.err.println("checking card " + c);
			    Card cc = p.findMatchingCard(c);
			    if (cc == null) {
				sendTo(p, "ERROR card not in hand: " + c.toString());
				bogus = true;
			    } else {
				discards.add(cc);
			    }
			}
			if (!bogus) {
			    // If ok, remove the cards
			    for (int i=0; i < n; i++) {
				Card c = discards.nth(i);
				//System.err.println("removing card " + c);
				p.removeCard(c);
			    }
			    // Generate new cards
			    String reply = "DRAWN " + n;
			    for (int i=0; i < n; i++) {
				Card c = deck.deal1();
				//System.err.println("adding card " + c);
				p.addCard(c);
				reply = reply + " " + c.toString();
			    }
			    // Tell the player
			    sendTo(p, reply);
			    // Tell the others (just the count)
			    broadcastToOthers(p, "FROM " + p.getName() + " DRAW " + n);
			    // And we're done drawing for this player
			    //System.err.println("done drawing");
			    return;
			}
		    }
		} catch (NoSuchElementException ex) {
		    System.err.println("not enough tokens in message: " + msg);
		}
	    }
	    // If we get here the message was bogus
	    numBogusMessages += 1;
	}
	// If we get here, they sent too many bogus messages
	sendTo(p, "ERROR Too many bogus messages -- goodbye");
	handlePlayerQuit(p);
	return;
    }
    //
    // Hand evaluations and comparisons (simple with 5-card hand)
    //
    // Returns optimal HandType for player from hole cards and common cards
    public HandType bestHandType(Player p) {
	return HandType.fromCards(p.getHand());
    }
    // Tests if TYPE is valid for player (not necessarily optimal)
    public boolean testHandType(Player p, HandType type) {
	return type.test(p.getHand());
    }
}
