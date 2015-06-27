/*
 * Deck.java
 *
 * George Ferguson, ferguson@cs.rochester.edu,	8 Sep 1998
 * Time-stamp: <Fri Sep 18 10:01:06 EDT 1998 ferguson>
 */

public class Deck {
	// Members
	private Card[] cards;
	private int nextCard;
	// Constructor
	public Deck() {
		cards = new Card[52];
		fill();
		shuffle();
		reset();
	}
	// Methods
	public void fill() {
		for (int s = 1; s <= 4; s++) {
			for (int r = 1; r <= 13; r++) {
				int i = 13*(s-1) + r - 1;
				cards[i] = new Card(r, s);
			}
		}
	}
	public void shuffle() {
		for (int n = 0; n < 20; n++) {
			for (int i = 0; i < 52; i++) {
				int r = RandomInt.random(51);
				Card temp = cards[i];
				cards[i] = cards[r];
				cards[r] = temp;
			}
		}
	}
	public void reset() {
		nextCard = 0;
	}
	public Card deal1() {
		if (nextCard >= 52) {
			System.err.println("YOW! Attempt to deal more than 52 cards.");
			return null;
		} else {
			return cards[nextCard++];
		}
	}
	public boolean empty() {
		return nextCard >= 52;
	}
	// Printing
	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		int n = cards.length;
		for (int i = 0; i < n; i++) {
			buf.append(cards[i].toString());
			if (i < n-1) {
				buf.append(" ");
			}
		}
		return buf.toString();
	}
	// Testing
	public static void main(String argv[]) {
		Deck d = new Deck();
		System.out.println(d);
	}
}
