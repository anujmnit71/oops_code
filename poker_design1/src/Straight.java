/*
 * Straight.java
 *
 * George Ferguson, ferguson@cs.rochester.edu,  8 Sep 1998
 * Time-stamp: <Thu Jan 19 14:27:40 EST 2006 ferguson>
 */

public class Straight extends HandType {
    // Members
    protected int rank;
    // Constructor
    public Straight(int r) {
    	rank = r;
    }
    public Straight(String s) throws HandTypeFormatException {
	if ((rank = Card.rankFromString(s)) < 0) {
	    throw new HandTypeFormatException("Bad rank in Straight: " + s);
	}
    }
    // Pseudo-constructor
    public static HandType fromCards(CardVector cards) {
	//System.err.println("Straight.fromCards: " + cards);
	cards.sortDecreasingAceHigh();
	//System.err.println("sorted ace high: " + cards);
	if (cards.testInSequenceDecreasing() ||
	    (cards.nth(0).rank() == 1 && cards.nth(1).rank() == 13 &&
	     cards.testInSequenceDecreasing(1))) {
	    //System.err.println("yes (ace high)");
	    return new Straight(cards.nth(0).rank());
	}
	cards.sortDecreasingAceLow();
	//System.err.println("sorted ace low: " + cards);
	if (cards.testInSequenceDecreasing()) {
	    //System.err.println("yes (ace low)");
	    return new Straight(cards.nth(0).rank());
	}
	//System.err.println("no straight");
	return null;
    }
    // Comparison
    public int compare(StraightFlush other) { return -1; }
    public int compare(FourOfAKind other) { return -1; }
    public int compare(FullHouse other) { return -1; }
    public int compare(Flush other) { return -1; }
    public int compare(Straight other) {
    	return Card.compareRanksAceHigh(rank, other.rank);
    }
    public int compare(ThreeOfAKind other) { return 1; }
    public int compare(TwoPair other) { return 1; }
    public int compare(OnePair other) { return 1; }
    public int compare(HighCard other) { return 1; }
    // Testing
    public boolean test(CardVector cards) {
	cards.sortDecreasingAceHigh();
	if ((cards.testInSequenceDecreasing() ||
	     cards.nth(0).rank() == 1 && cards.nth(1).rank() == 13 &&
	     cards.testInSequenceDecreasing(1)) &&
	    cards.nth(0).rank() == rank) {
	    return true;
	}
	cards.sortDecreasingAceLow();
	if (cards.testInSequenceDecreasing() && cards.nth(0).rank() == rank) {
	    return true;
	}
	return false;
    }
    // Printing
    public String toString() {
	return "STRAIGHT " + Card.rankToString(rank);
    }
}
