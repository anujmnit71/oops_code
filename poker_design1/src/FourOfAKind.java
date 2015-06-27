/*
 * FourOfAKind.java
 *
 * George Ferguson, ferguson@cs.rochester.edu,  8 Sep 1998
 * Time-stamp: <Thu Jan 19 14:28:38 EST 2006 ferguson>
 */

import java.util.*;

public class FourOfAKind extends HandType {
    // Members
    private int[] ranks;
    // Constructor
    public FourOfAKind(int r0, int r1) {
	ranks = new int[2];
    	ranks[0] = r0;
    	ranks[1] = r1;
    }
    public FourOfAKind(String s) throws HandTypeFormatException {
	StringTokenizer tokens = new StringTokenizer(s, " \t");
	ranks = new int[2];
	for (int i=0; i < 2; i++) {
	    try {
		String t = tokens.nextToken();
		if ((ranks[i] = Card.rankFromString(t)) < 0) {
		    throw new HandTypeFormatException("Bad rank in FourOfAKind: " + t);
		}
	    } catch (NoSuchElementException ex) {
		throw new HandTypeFormatException("Not enough ranks for FourOfAKind: " + s);
	    }
	}
    }
    // Pseudo-constructor
    public static HandType fromCards(CardVector cards) {
	cards.sortDecreasingAceHigh();
	int rank0 = cards.nth(0).rank();
	if (cards.nth(1).rank() == rank0 &&
	    cards.nth(2).rank() == rank0 &&
	    cards.nth(3).rank() == rank0) {
	    return new FourOfAKind(rank0, cards.nth(4).rank());
	}
	int rank1 = cards.nth(1).rank();
	if (cards.nth(2).rank() == rank1 &&
	    cards.nth(3).rank() == rank1 &&
	    cards.nth(4).rank() == rank1) {
	    return new FourOfAKind(rank1, cards.nth(0).rank());
	}
	return null;
    }
    // Comparison
    public int compare(StraightFlush other) { return -1; }
    public int compare(FourOfAKind other) {
    	return Card.compareRankArraysAceHigh(ranks, other.ranks);
    }
    public int compare(FullHouse other) { return 1; }
    public int compare(Flush other) { return 1; }
    public int compare(Straight other) { return 1; }
    public int compare(ThreeOfAKind other) { return 1; }
    public int compare(TwoPair other) { return 1; }
    public int compare(OnePair other) { return 1; }
    public int compare(HighCard other) { return 1; }
    // Testing
    public boolean test(CardVector cards) {
	cards.sortDecreasingAceHigh();
	if ((cards.nth(0).rank() == ranks[0] &&
	     cards.nth(1).rank() == ranks[0] &&
	     cards.nth(2).rank() == ranks[0] &&
	     cards.nth(3).rank() == ranks[0] &&
	     cards.nth(4).rank() == ranks[1]) ||
	    (cards.nth(1).rank() == ranks[0] &&
	     cards.nth(2).rank() == ranks[0] &&
	     cards.nth(3).rank() == ranks[0] &&
	     cards.nth(4).rank() == ranks[0] &&
	     cards.nth(0).rank() == ranks[1])) {
	    return true;
	}
	return false;
    }
    // Printing
    public String toString() {
	return "FOUROFAKIND " + Card.rankToString(ranks[0]) +
	    " " + Card.rankToString(ranks[1]);
    }
}
