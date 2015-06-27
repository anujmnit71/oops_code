/*
 * SevenCardStudPokerGame.java
 *
 * George Ferguson, ferguson@cs.rochester.edu, 29 Oct 1999
 * Time-stamp: <Thu Jan 19 14:27:49 EST 2006 ferguson>
 */

public class SevenCardStudPokerGame extends PokerGame {
    // Constructor
    public SevenCardStudPokerGame(int low, int high, int raises, boolean cspeak,
				  boolean useDisplay, String displaySize) {
	super(low, high, raises, cspeak, useDisplay, displaySize);
	display.setTitle("IPP Poker Server - Seven Card Stud");
    }
    //
    // PokerGame Methods
    //
    public String gameTypeString() {
	return "STUD 7 " + lowLimit + " " + highLimit + " " +
	    maxRaisesPerRound;
    }
    public int maxCardsPerPlayer() {
	return 7;
    }
    public void playAHand() {
	resetForHand();
	pause("Ready to collect ante", 2);
	ante(lowLimit/2);
	pause("Ready to deal", 2);
	deal();
	pause("Ready for first up card", 2);
	upcard();
	try {
	    pause("Ready for first betting round", 3);
	    bettingRound(lowLimit, true);
	    pause("Ready for second up card", 3);
	    upcard();
	    bettingRound(lowLimit, true);
	    pause("Ready for third up card", 3);
	    upcard();
	    bettingRound(highLimit, false);
	    pause("Ready for fourth up card", 3);
	    upcard();
	    bettingRound(highLimit, false);
	    pause("Ready for final down card", 3);
	    downcard();
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
		Card card1 = deck.deal1();
		Card card2 = deck.deal1();
		p.addCard(card1);
		p.addCard(card2);
		sendTo(p, "DEAL " + card1.toString() + " " + card2.toString());
	    }
	}
	updateDisplay();
    }
    private void upcard() {
	for (int i=0; i < players.size(); i++) {
	    Player p = players.nth(i);
	    if (!p.isBusted() && !p.isFolded()) {
		Card c = deck.deal1();
		c.setFaceUp(true);
		p.addCard(c);
		sendTo(p, "UP " + c.toString());
		broadcastToOthers(p, "FROM " + p.getName() + " UP " + c.toString());
	    }
	}
	updateDisplay();
    }
    private void downcard() {
	for (int i=0; i < players.size(); i++) {
	    Player p = players.nth(i);
	    if (!p.isBusted() && !p.isFolded()) {
		Card c = deck.deal1();
		p.addCard(c);
		sendTo(p, "DOWN " + c.toString());
		broadcastToOthers(p, "FROM " + p.getName() + " DOWN");
	    }
	}
	updateDisplay();
    }
    //
    // Hand evaluations and comparisons (best 5 out of seven)
    //
    // Returns optimal HandType for player from cards
    public HandType bestHandType(Player p) {
	CardVector hand = p.getHand();
	CardVector scratch = new CardVector();
	HandType best = null;
	// For each way of selecting two cards *not* to use
	for (int i = 0; i < 7; i++) {
	    for (int j = 0; j < 7; j++) {
		if (i != j) {
		    // Reset scratch hand
		    scratch.removeAllElements();
		    // Add all the other cards
		    for (int k = 0; k < 7; k++) {
			if (k != i && k != j) {
			    scratch.addElement(hand.nth(k));
			}
		    }
		    // Evaluate hand
		    HandType type = HandType.fromCards(scratch);
		    // Save best hand so far
		    if (best == null || type.compare(best) == 1) {
			best = type;
		    }
		}
	    }
	}
	return best;
    }
    // Tests if TYPE is valid for player (not necessarily optimal)
    public boolean testHandType(Player p, HandType type) {
	CardVector hand = p.getHand();
	CardVector scratch = new CardVector();
	// For each way of selecting two cards *not* to use
	for (int i = 0; i < 7; i++) {
	    for (int j = 0; j < 7; j++) {
		if (i != j) {
		    // Reset hand
		    scratch.removeAllElements();
		    // Add all the other cards
		    for (int k = 0; k < 7; k++) {
			if (k != i && k != j) {
			    scratch.addElement(hand.nth(k));
			}
		    }
		    // Evaluate hand
		    if (type.test(scratch)) {
			return true;
		    }
		}
	    }
	}
	return false;
    }
}
