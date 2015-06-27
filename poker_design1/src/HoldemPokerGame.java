/*
 * HoldemPokerGame.java
 *
 * George Ferguson, ferguson@cs.rochester.edu,	8 Sep 1998
 * Time-stamp: <Thu Jan 19 15:25:45 EST 2006 ferguson>
 */

public class HoldemPokerGame extends PokerGame {
    // Members
    private CardVector common;
    // Constructor
    public HoldemPokerGame(int low, int high, int raises, boolean cspeak,
			   boolean useDisplay, String displaySize) {
	super(low, high, raises, cspeak, useDisplay, displaySize);
	display.setTitle("IPP Poker Server - Texas Hold 'Em");
	common = new CardVector();
    }
    //
    // PokerGame Methods
    //
    public String gameTypeString() {
	return "HOLDEM " + lowLimit + " " + highLimit + " " +
	    maxRaisesPerRound;
    }
    public int maxCardsPerPlayer() {
	// Should be 2, but somehow that doesn't work
	return 3;
    }
    protected void resetForHand() {
	super.resetForHand();
	common.removeAllElements();
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
	    pause("Ready for flop", 3);
	    flop();
	    pause("Ready for second betting round", 3);
	    bettingRound(lowLimit, true);
	    pause("Ready for turn", 3);
	    turn();
	    pause("Ready for third betting round", 3);
	    bettingRound(highLimit, false);
	    pause("Ready for river", 3);
	    river();
	    pause("Ready for fourth betting round", 3);
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
    private void flop() {
	Card c1 = deck.deal1();
	Card c2 = deck.deal1();
	Card c3 = deck.deal1();
	common.add(c1);
	common.add(c2);
	common.add(c3);
	broadcast("FLOP " +
		  c1.toString() + " " +
		  c2.toString() + " " +
		  c3.toString());
	if (display != null) {
	    display.addCommonCard(c1);
	    display.addCommonCard(c2);
	    display.addCommonCard(c3);
	    updateDisplay();
	}
    }
    private void turn() {
	Card c = deck.deal1();
	common.add(c);
	broadcast("TURN " + c.toString());
	if (display != null) {
	    display.addCommonCard(c);
	    updateDisplay();
	}
    }
    private void river() {
	Card c = deck.deal1();
	common.add(c);
	broadcast("RIVER " + c.toString());
	if (display != null) {
	    display.addCommonCard(c);
	    updateDisplay();
	}
    }
    //
    // Hand evaluations and comparisons (have to use at least one hole card)
    //
    // Returns optimal HandType for player from hole cards and common cards
    public HandType bestHandType(Player p) {
	return bestHandType(p, common);
    }
    public static HandType bestHandType(Player p, CardVector common) {
	HandType type1 = bestHandTypeUsingOneHoleCard(p.getHand(), common);
	HandType type2 = bestHandTypeUsingTwoHoleCards(p.getHand(), common);
	if (type1 == null) {
	    return type2;
	} else if (type2 == null) {
	    return type1;
	} else if (type1.compare(type2) > 0) {
	    return type1;
	} else {
	    return type2;
	}
    }
    private static HandType bestHandTypeUsingTwoHoleCards(CardVector hole,
							  CardVector common) {
	CardVector hand = new CardVector();
	HandType best = null;
	// For each way of selecting two common cards *not* to use
	for (int i = 0; i < 5; i++) {
	    for (int j = 0; j < 5; j++) {
		if (i != j) {
		    // Reset hand
		    hand.removeAllElements();
		    // Add both hole cards
		    hand.addElement(hole.nth(0));
		    hand.addElement(hole.nth(1));
		    // Add other common cards
		    for (int k = 0; k < 5; k++) {
			if (k != i && k != j) {
			    hand.addElement(common.nth(k));
			}
		    }
		    // Evaluate hand
		    HandType type = HandType.fromCards(hand);
		    // Save best hand so far
		    if (best == null || type.compare(best) == 1) {
			best = type;
		    }
		}
	    }
	}
	return best;
    }
    private static HandType bestHandTypeUsingOneHoleCard(CardVector hole,
							 CardVector common) {
	CardVector hand = new CardVector();
	HandType best = null;
	// For each way of selecting one common card *not* to use
	for (int i = 0; i < 5; i++) {
	    // For each way of selecting one hole card to use
	    for (int j = 0; j < 2; j++) {
		// Reset hand
		hand.removeAllElements();
		// Add selected hole card
		hand.addElement(hole.nth(j));
		// Add other common cards
		for (int k = 0; k < 5; k++) {
		    if (k != i) {
			hand.addElement(common.nth(k));
		    }
		}
		// Evaluate hand
		HandType type = HandType.fromCards(hand);
		// Save best hand so far
		if (best == null || type.compare(best) == 1) {
		    best = type;
		}
	    }
	}
	return best;
    }
    // Tests if TYPE is valid for player (not necessarily optimal)
    public boolean testHandType(Player p, HandType type) {
	return testHandTypeUsingOneHoleCard(type, p.getHand(), common) ||
	    testHandTypeUsingTwoHoleCards(type, p.getHand(), common);
    }
    private static boolean testHandTypeUsingTwoHoleCards(HandType type,
							 CardVector hole,
							 CardVector common) {
	CardVector hand = new CardVector();
	// For each way of selecting two common cards *not* to use
	for (int i = 0; i < 5; i++) {
	    for (int j = 0; j < 5; j++) {
		if (i != j) {
		    // Reset hand
		    hand.removeAllElements();
		    // Add both hole cards
		    hand.addElement(hole.nth(0));
		    hand.addElement(hole.nth(1));
		    // Add other common cards
		    for (int k = 0; k < 5; k++) {
			if (k != i && k != j) {
			    hand.addElement(common.nth(k));
			}
		    }
		    // Evaluate hand
		    if (type.test(hand)) {
			return true;
		    }
		}
	    }
	}
	return false;
    }
    private static boolean testHandTypeUsingOneHoleCard(HandType type,
							CardVector hole,
							CardVector common) {
	CardVector hand = new CardVector();
	// For each way of selecting one common card *not* to use
	for (int i = 0; i < 5; i++) {
	    // For each way of selecting one hole card to use
	    for (int j = 0; j < 2; j++) {
		// Reset hand
		hand.removeAllElements();
		// Add selected hole card
		hand.addElement(hole.nth(j));
		// Add other common cards
		for (int k = 0; k < 5; k++) {
		    if (k != i) {
			hand.addElement(common.nth(k));
		    }
		}
		// Evaluate hand
		if (type.test(hand)) {
		    return true;
		}
	    }
	}
	return false;
    }
}
