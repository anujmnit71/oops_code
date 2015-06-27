/*
 * HighCard.java
 *
 * George Ferguson, ferguson@cs.rochester.edu,  8 Sep 1998
 * Time-stamp: <Thu Jan 19 14:28:14 EST 2006 ferguson>
 */

import java.util.*;

public class HighCard extends HandType {
    // Members
    private int[] ranks;
    // Constructor
    public HighCard(int r0, int r1, int r2, int r3, int r4) {
	ranks = new int[5];
    	ranks[0] = r0;
    	ranks[1] = r1;
    	ranks[2] = r2;
    	ranks[3] = r3;
    	ranks[4] = r4;
    }
    public HighCard(String s) throws HandTypeFormatException {
	StringTokenizer tokens = new StringTokenizer(s, " \t");
	ranks = new int[5];
	for (int i=0; i < 5; i++) {
	    try {
		String t = tokens.nextToken();
		if ((ranks[i] = Card.rankFromString(t)) < 0) {
		    throw new HandTypeFormatException("Bad rank in HighCard: " + t);
		}
	    } catch (NoSuchElementException ex) {
		throw new HandTypeFormatException("Not enough ranks for HighCard: " + s);
	    }
	}
    }
    // Pseudo-constructor
    public static HandType fromCards(CardVector cards) {
	cards.sortDecreasingAceHigh();
	return new HighCard(cards.nth(0).rank(),
			    cards.nth(1).rank(),
			    cards.nth(2).rank(),
			    cards.nth(3).rank(),
			    cards.nth(4).rank());
    }
    // Comparison
    public int compare(StraightFlush other) { return -1; }
    public int compare(FourOfAKind other) { return -1; }
    public int compare(FullHouse other) { return -1; }
    public int compare(Flush other) { return -1; }
    public int compare(Straight other) { return -1; }
    public int compare(ThreeOfAKind other) { return -1; }
    public int compare(TwoPair other) { return -1; }
    public int compare(OnePair other) { return -1; }
    public int compare(HighCard other) {
	return Card.compareRankArraysAceHigh(ranks, other.ranks);
    }
    // Testing
    public boolean test(CardVector cards) {
	cards.sortDecreasingAceHigh();
	return (cards.nth(0).rank() == ranks[0] &&
		cards.nth(1).rank() == ranks[1] &&
		cards.nth(2).rank() == ranks[2] &&
		cards.nth(3).rank() == ranks[3] &&
		cards.nth(4).rank() == ranks[4]);
    }
    // Printing
    public String toString() {
	return "HIGHCARD " + Card.rankToString(ranks[0]) +
	    " " + Card.rankToString(ranks[1]) +
	    " " + Card.rankToString(ranks[2]) +
	    " " + Card.rankToString(ranks[3]) +
	    " " + Card.rankToString(ranks[4]);
    }
}
