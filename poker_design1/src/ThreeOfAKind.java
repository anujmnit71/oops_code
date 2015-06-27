/*
 * ThreeOfAKind.java
 *
 * George Ferguson, ferguson@cs.rochester.edu,  8 Sep 1998
 * Time-stamp: <Thu Jan 19 14:27:28 EST 2006 ferguson>
 */

import java.util.*;

public class ThreeOfAKind extends HandType {
    // Members
    private int ranks[];
    // Constructor
    public ThreeOfAKind(int r0, int r1, int r2) {
	ranks = new int[3];
    	ranks[0] = r0;
    	ranks[1] = r1;
    	ranks[2] = r2;
    }
    public ThreeOfAKind(String s) throws HandTypeFormatException {
	StringTokenizer tokens = new StringTokenizer(s, " \t");
	ranks = new int[3];
	for (int i=0; i < 3; i++) {
	    try {
		String t = tokens.nextToken();
		if ((ranks[i] = Card.rankFromString(t)) < 0) {
		    throw new HandTypeFormatException("Bad rank in ThreeOfAKind: " + t);
		}
	    } catch (NoSuchElementException ex) {
		throw new HandTypeFormatException("Not enough ranks for ThreeOfAKind: " + s);
	    }
	}
    }
    // Pseudo-constructor
    public static HandType fromCards(CardVector cards) {
	cards.sortDecreasingAceHigh();
	int rank0 = cards.nth(0).rank();
	if (cards.nth(1).rank() == rank0 &&
	    cards.nth(2).rank() == rank0) {
	    return new ThreeOfAKind(rank0, cards.nth(3).rank(), cards.nth(4).rank());
	}
	int rank1 = cards.nth(1).rank();
	if (cards.nth(2).rank() == rank1 &&
	    cards.nth(3).rank() == rank1) {
	    return new ThreeOfAKind(rank1, rank0, cards.nth(4).rank());
	}
	int rank2 = cards.nth(2).rank();
	if (cards.nth(3).rank() == rank2 &&
	    cards.nth(4).rank() == rank2) {
	    return new ThreeOfAKind(rank2, rank0, rank1);
	}
	return null;
    }
    // Comparison
    public int compare(StraightFlush other) { return -1; }
    public int compare(FourOfAKind other) { return -1; }
    public int compare(FullHouse other) { return -1; }
    public int compare(Flush other) { return -1; }
    public int compare(Straight other) { return -1; }
    public int compare(ThreeOfAKind other) {
    	return Card.compareRankArraysAceHigh(ranks, other.ranks);
    }
    public int compare(TwoPair other) { return 1; }
    public int compare(OnePair other) { return 1; }
    public int compare(HighCard other) { return 1; }
    // Testing
    public boolean test(CardVector cards) {
	cards.sortDecreasingAceHigh();
	return ((cards.nth(0).rank() == ranks[0] &&
		 cards.nth(1).rank() == ranks[0] &&
		 cards.nth(2).rank() == ranks[0] &&
		 cards.nth(3).rank() == ranks[1] &&
		 cards.nth(4).rank() == ranks[2]) ||
		(cards.nth(1).rank() == ranks[0] &&
		 cards.nth(2).rank() == ranks[0] &&
		 cards.nth(3).rank() == ranks[0] &&
		 cards.nth(0).rank() == ranks[1] &&
		 cards.nth(4).rank() == ranks[2]) ||
		(cards.nth(2).rank() == ranks[0] &&
		 cards.nth(3).rank() == ranks[0] &&
		 cards.nth(4).rank() == ranks[0] &&
		 cards.nth(0).rank() == ranks[1] &&
		 cards.nth(1).rank() == ranks[2]));
    }
    // Printing
    public String toString() {
	return "THREEOFAKIND " + Card.rankToString(ranks[0]) +
	    " " + Card.rankToString(ranks[1]) +
	    " " + Card.rankToString(ranks[2]);
    }
}
