/*
 * FullHouse.java
 *
 * George Ferguson, ferguson@cs.rochester.edu,  8 Sep 1998
 * Time-stamp: <Thu Jan 19 14:28:35 EST 2006 ferguson>
 */

import java.util.*;

public class FullHouse extends HandType {
    // Members
    private int ranks[];
    // Constructor
    public FullHouse(int r0, int r1) {
	ranks = new int[2];
    	ranks[0] = r0;
    	ranks[1] = r1;
    }
    public FullHouse(String s) throws HandTypeFormatException {
	StringTokenizer tokens = new StringTokenizer(s, " \t");
	ranks = new int[2];
	for (int i=0; i < 2; i++) {
	    try {
		String t = tokens.nextToken();
		if ((ranks[i] = Card.rankFromString(t)) < 0) {
		    throw new HandTypeFormatException("Bad rank in FullHouse: " + t);
		}
	    } catch (NoSuchElementException ex) {
		throw new HandTypeFormatException("Not enough ranks for FullHouse: " + s);
	    }
	}
    }
    // Pseudo-constructor
    public static HandType fromCards(CardVector cards) {
	cards.sortDecreasingAceHigh();
	int rank0 = cards.nth(0).rank();
	if (cards.nth(1).rank() == rank0 &&
	    cards.nth(2).rank() == rank0 &&
	    cards.nth(4).rank() == cards.nth(3).rank()) {
	    return new FullHouse(rank0, cards.nth(3).rank());
	}
	int rank2 = cards.nth(2).rank();
	if (cards.nth(1).rank() == rank0 &&
	    cards.nth(3).rank() == rank2 &&
	    cards.nth(4).rank() == rank2) {
	    return new FullHouse(rank2, rank0);
	}
	return null;
    }
    // Comparison
    public int compare(StraightFlush other) { return -1; }
    public int compare(FourOfAKind other) { return -1; }
    public int compare(FullHouse other) {
    	return Card.compareRankArraysAceHigh(ranks, other.ranks);
    }
    public int compare(Flush other) { return 1; }
    public int compare(Straight other) { return 1; }
    public int compare(ThreeOfAKind other) { return 1; }
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
		 cards.nth(4).rank() == ranks[1]) ||
		(cards.nth(0).rank() == ranks[1] &&
		 cards.nth(1).rank() == ranks[1] &&
		 cards.nth(2).rank() == ranks[0] &&
		 cards.nth(3).rank() == ranks[0] &&
		 cards.nth(4).rank() == ranks[0]));
    }
    // Printing
    public String toString() {
	return "FULLHOUSE " + Card.rankToString(ranks[0]) +
	    " " + Card.rankToString(ranks[1]);
    }
}
