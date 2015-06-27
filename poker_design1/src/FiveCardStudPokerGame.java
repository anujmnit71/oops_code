/*
 * FiveCardStudPokerGame.java
 *
 * George Ferguson, ferguson@cs.rochester.edu, 29 Oct 1999
 * Time-stamp: <Thu Jan 19 14:28:46 EST 2006 ferguson>
 */

public class FiveCardStudPokerGame extends PokerGame {
	// Constructor
	public FiveCardStudPokerGame(int low, int high, int raises, boolean cspeak,
			boolean useDisplay, String displaySize) {
		super(low, high, raises, cspeak, useDisplay, displaySize);
		display.setTitle("IPP Poker Server - Five Card Stud");
	}
	//
	// PokerGame Methods
	//
	@Override
	public String gameTypeString() {
		return "STUD 5 " + lowLimit + " " + highLimit + " " +
				maxRaisesPerRound;
	}
	@Override
	public int maxCardsPerPlayer() {
		return 5;
	}
	@Override
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
			if (!p.isBusted() && !p.isFolded()) {
				Card c = deck.deal1();
				p.addCard(c);
				sendTo(p, "DEAL " + c.toString());
			}
		}
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
	//
	// Hand evaluations and comparisons (simple with 5-card hand)
	//
	// Returns optimal HandType for player from hole cards and common cards
	@Override
	public HandType bestHandType(Player p) {
		return HandType.fromCards(p.getHand());
	}
	// Tests if TYPE is valid for player (not necessarily optimal)
	@Override
	public boolean testHandType(Player p, HandType type) {
		return type.test(p.getHand());
	}
}
