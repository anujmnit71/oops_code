/*
 * OnePair.java
 *
 * George Ferguson, ferguson@cs.rochester.edu,  8 Sep 1998
 * Time-stamp: <Thu Jan 19 14:28:02 EST 2006 ferguson>
 */

import java.util.*;

public class OnePair extends HandType {
    // Members
    private int[] ranks;
    // Constructor
    public OnePair(int r0, int r1, int r2, int r3) {
	ranks = new int[4];
    	ranks[0] = r0;
    	ranks[1] = r1;
    	ranks[2] = r2;
    	ranks[3] = r3;
    }
    public OnePair(String s) throws HandTypeFormatException {
	StringTokenizer tokens = new StringTokenizer(s, " \t");
	ranks = new int[4];
	for (int i=0; i < 4; i++) {
	    try {
		String t = tokens.nextToken();
		if ((ranks[i] = Card.rankFromString(t)) < 0) {
		    throw new HandTypeFormatException("Bad rank in OnePair: " + t);
		}
	    } catch (NoSuchElementException ex) {
		throw new HandTypeFormatException("Not enough ranks for OnePair");
	    }
	}
    }
    // Pseudo-constructor
    public static HandType fromCards(CardVector cards) {
	//System.err.println("OnePair.fromCards: " + cards);
	cards.sortDecreasingAceHigh();
	//System.err.println("sorted: " + cards);
	if (cards.nth(0).rank() == cards.nth(1).rank()) {
	    //System.err.println("case 0: " + cards.nth(0).rank());
	    return new OnePair(cards.nth(0).rank(),
			       cards.nth(2).rank(),
			       cards.nth(3).rank(),
			       cards.nth(4).rank());
	}
	if (cards.nth(1).rank() == cards.nth(2).rank()) {
	    //System.err.println("case 1: " + cards.nth(1).rank());
	    return new OnePair(cards.nth(1).rank(),
			       cards.nth(0).rank(),
			       cards.nth(3).rank(),
			       cards.nth(4).rank());
	}
	if (cards.nth(2).rank() == cards.nth(3).rank()) {
	    //System.err.println("case 2: " + cards.nth(2).rank());
	    return new OnePair(cards.nth(2).rank(),
			       cards.nth(0).rank(),
			       cards.nth(1).rank(),
			       cards.nth(4).rank());
	}
	if (cards.nth(3).rank() == cards.nth(4).rank()) {
	    //System.err.println("case 3: " + cards.nth(3).rank());
	    return new OnePair(cards.nth(3).rank(),
			       cards.nth(0).rank(),
			       cards.nth(1).rank(),
			       cards.nth(2).rank());
	}
	//System.err.println("no pair");
	return null;
    }
    // Comparison
    public int compare(StraightFlush other) { return -1; }
    public int compare(FourOfAKind other) { return -1; }
    public int compare(FullHouse other) { return -1; }
    public int compare(Flush other) { return -1; }
    public int compare(Straight other) { return -1; }
    public int compare(ThreeOfAKind other) { return -1; }
    public int compare(TwoPair other) { return -1; }
    public int compare(OnePair other) {
    	return Card.compareRankArraysAceHigh(ranks, other.ranks);
    }
    public int compare(HighCard other) { return 1; }
    // Testing
    public boolean test(CardVector cards) {
	cards.sortDecreasingAceHigh();
	return ((cards.nth(0).rank() == ranks[0] &&
		 cards.nth(1).rank() == ranks[0] &&
		 cards.nth(2).rank() == ranks[1] &&
		 cards.nth(3).rank() == ranks[2] &&
		 cards.nth(4).rank() == ranks[3]) ||
		(cards.nth(0).rank() == ranks[1] &&
		 cards.nth(1).rank() == ranks[0] &&
		 cards.nth(2).rank() == ranks[0] &&
		 cards.nth(3).rank() == ranks[2] &&
		 cards.nth(4).rank() == ranks[3]) ||
		(cards.nth(0).rank() == ranks[1] &&
		 cards.nth(1).rank() == ranks[2] &&
		 cards.nth(2).rank() == ranks[0] &&
		 cards.nth(3).rank() == ranks[0] &&
		 cards.nth(4).rank() == ranks[3]) ||
		(cards.nth(0).rank() == ranks[1] &&
		 cards.nth(1).rank() == ranks[2] &&
		 cards.nth(2).rank() == ranks[3] &&
		 cards.nth(3).rank() == ranks[0] &&
		 cards.nth(4).rank() == ranks[0]));
    }
    // Printing
    public String toString() {
	return "ONEPAIR " + Card.rankToString(ranks[0]) +
	    " " + Card.rankToString(ranks[1]) +
	    " " + Card.rankToString(ranks[2]) +
	    " " + Card.rankToString(ranks[3]);
    }
}
