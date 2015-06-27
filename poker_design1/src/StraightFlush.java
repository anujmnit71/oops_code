/*
 * StraightFlush.java
 *
 * George Ferguson, ferguson@cs.rochester.edu,  8 Sep 1998
 * Time-stamp: <Thu Jan 19 14:27:35 EST 2006 ferguson>
 */

public class StraightFlush extends HandType {
    // Members
    private int rank;
    // Constructor
    public StraightFlush(int r) {
    	rank = r;
    }
    public StraightFlush(String s) throws HandTypeFormatException {
	if ((rank = Card.rankFromString(s)) < 0) {
	    throw new HandTypeFormatException("Bad rank in StraightFlush: " + s);
	}
    }
    // Comparison
    public int compare(StraightFlush other) {
    	return Card.compareRanksAceHigh(rank, other.rank);
    }
    public int compare(FourOfAKind other) { return 1; }
    public int compare(FullHouse other) { return 1; }
    public int compare(Flush other) { return 1; }
    public int compare(Straight other) { return 1; }
    public int compare(ThreeOfAKind other) { return 1; }
    public int compare(TwoPair other) { return 1; }
    public int compare(OnePair other) { return 1; }
    public int compare(HighCard other) { return 1; }
    // Pseudo-constructor from CardVector
    public static HandType fromCards(CardVector cards) {
	if (cards.testSameSuit()) {
	    Straight st = (Straight)Straight.fromCards(cards);
	    if (st != null) {
		return new StraightFlush(st.rank);
	    }
	}
	return null;
    }
    // Test
    public boolean test(CardVector cards) {
	if (cards.testSameSuit()) {
	    Straight st = new Straight(rank);
	    return st.test(cards);
	}
	return false;
    }
    // Printing
    public String toString() {
	return "STRAIGHTFLUSH " + Card.rankToString(rank);
    }
}
