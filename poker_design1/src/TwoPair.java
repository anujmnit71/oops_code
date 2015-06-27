/*
 * TwoPair.java
 *
 * George Ferguson, ferguson@cs.rochester.edu,  8 Sep 1998
 * Time-stamp: <Thu Jan 19 14:27:17 EST 2006 ferguson>
 */

import java.util.*;

public class TwoPair extends HandType {
    // Members
    private int[] ranks;
    // Constructor
    public TwoPair(int r0, int r1, int r2) {
	ranks = new int[3];
    	ranks[0] = r0;
    	ranks[1] = r1;
    	ranks[2] = r2;
    }
    public TwoPair(String s) throws HandTypeFormatException {
	StringTokenizer tokens = new StringTokenizer(s, " \t");
	ranks = new int[3];
	for (int i=0; i < 3; i++) {
	    try {
		String t = tokens.nextToken();
		if ((ranks[i] = Card.rankFromString(t)) < 0) {
		    throw new HandTypeFormatException("Bad rank in TwoPair: " + t);
		}
	    } catch (NoSuchElementException ex) {
		throw new HandTypeFormatException("Not enough ranks for TwoPair: " + s);
	    }
	}
    }
    // Pseudo-constructor
    public static HandType fromCards(CardVector cards) {
	cards.sortDecreasingAceHigh();
	if (cards.nth(0).rank() == cards.nth(1).rank() &&
	    cards.nth(2).rank() == cards.nth(3).rank()) {
	    return new TwoPair(cards.nth(0).rank(),
			       cards.nth(2).rank(),
			       cards.nth(4).rank());
	}
	if (cards.nth(0).rank() == cards.nth(1).rank() &&
	    cards.nth(3).rank() == cards.nth(4).rank()) {
	    return new TwoPair(cards.nth(0).rank(),
			       cards.nth(3).rank(),
			       cards.nth(2).rank());
	}
	if (cards.nth(1).rank() == cards.nth(2).rank() &&
	    cards.nth(3).rank() == cards.nth(4).rank()) {
	    return new TwoPair(cards.nth(1).rank(),
			       cards.nth(3).rank(),
			       cards.nth(0).rank());
	}
	return null;
    }
    // Comparison
    public int compare(StraightFlush other) { return -1; }
    public int compare(FourOfAKind other) { return -1; }
    public int compare(FullHouse other) { return -1; }
    public int compare(Flush other) { return -1; }
    public int compare(Straight other) { return -1; }
    public int compare(ThreeOfAKind other) { return -1; }
    public int compare(TwoPair other) {
    	return Card.compareRankArraysAceHigh(ranks, other.ranks);
    }
    public int compare(OnePair other) { return 1; }
    public int compare(HighCard other) { return 1; }
    // Testing
    public boolean test(CardVector cards) {
	cards.sortDecreasingAceHigh();
	return ((cards.nth(0).rank() == ranks[0] &&
		 cards.nth(1).rank() == ranks[0] &&
		 cards.nth(2).rank() == ranks[1] &&
		 cards.nth(3).rank() == ranks[1] &&
		 cards.nth(4).rank() == ranks[2]) ||
		(cards.nth(0).rank() == ranks[0] &&
		 cards.nth(1).rank() == ranks[0] &&
		 cards.nth(2).rank() == ranks[2] &&
		 cards.nth(3).rank() == ranks[1] &&
		 cards.nth(4).rank() == ranks[1]) ||
		(cards.nth(0).rank() == ranks[2] &&
		 cards.nth(1).rank() == ranks[0] &&
		 cards.nth(2).rank() == ranks[0] &&
		 cards.nth(3).rank() == ranks[1] &&
		 cards.nth(4).rank() == ranks[1]));
    }
    // Printing
    public String toString() {
	return "TWOPAIR " + Card.rankToString(ranks[0]) +
	    " " + Card.rankToString(ranks[1]) +
	    " " + Card.rankToString(ranks[2]);
    }
}
