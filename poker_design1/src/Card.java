/*
 * Card.java
 *
 * George Ferguson, ferguson@cs.rochester.edu, 17 Sep 1998
 * Time-stamp: <Thu Oct  8 16:35:56 EDT 1998 ferguson>
 */

public class Card {
    // Members
    private int _rank;
    private int _suit;
    private boolean faceup;
    public static final String[] suits = { "S", "H", "D", "C" };
    public static final String[] ranks = { "A", "2", "3", "4", "5", "6", "7",
					   "8", "9", "T", "J", "Q", "K" };
    // Constructor
    public Card(int r, int s) {
	_rank = r;
	_suit = s;
	faceup = true;
    }
    public Card(int r, int s, boolean f) {
	_rank = r;
	_suit = s;
	faceup = f;
    }
    // Methods
    public int rank() {
	return _rank;
    }
    public int suit() {
	return _suit;
    }
    public boolean isFaceUp() {
	return faceup;
    }
    public boolean isFaceDown() {
	return !faceup;
    }
    public void setFaceUp(boolean f) {
	faceup = f;
    }
    public boolean equals(Card c) {
	return (_rank == c._rank && _suit == c._suit);
    }
    // Printing
    public String toString() {
	if (_rank > 0 && _rank <= ranks.length &&
	    _suit > 0 && _suit <= suits.length) {
	    return ranks[_rank-1] + suits[_suit-1];
	} else {
	    return "??";
	}
    }
    public static String rankToString(int rank) {
	if (rank > 0 && rank <= ranks.length) {
	    return ranks[rank-1];
	} else {
	    return "?";
	}
    }
    public static String suitToString(int suit) {
	if (suit > 0 && suit <= suits.length) {
	    return suits[suit-1];
	} else {
	    return "?";
	}
    }
    public static Card fromString(String s) {
	return fromString(s, true);
    }
    public static Card fromString(String s, boolean f) {
	int rank = rankFromString(s.substring(0, 1));
	int suit = suitFromString(s.substring(1, 2));
	return new Card(rank, suit, f);
    }
    public static int rankFromString(String s) {
	for (int i = 0; i < ranks.length; i++) {
	    if (ranks[i].equalsIgnoreCase(s)) {
		return i+1;
	    }
	}
	return 0;
    }
    public static int suitFromString(String s) {
	for (int i = 0; i < suits.length; i++) {
	    if (suits[i].equalsIgnoreCase(s)) {
		return i+1;
	    }
	}
	return 0;
    }
    // Compare ranks, returning -1 if A<B, 0 if A==B, or 1 if A>B
    public static int compareRanksAceHigh(int a, int b) {
	if (a == 1) {
	    if (b == 1) {
		return 0;
	    } else {
		return 1;
	    }
	} else if (b == 1) {
	    return -1;
	} else if (a < b) {
	    return -1;
	} else if (a > b) {
	    return 1;
	} else {
	    return 0;
	}
    }
    public static int compareRanksAceLow(int a, int b) {
	if (a < b) {
	    return -1;
	} else if (a > b) {
	    return 1;
	} else {
	    return 0;
	}
    }
    // Compares arrays of ranks A and B pairwise by element
    public static int compareRankArraysAceHigh(int a[], int b[]) {
	if (a.length != b.length) {
	    //System.err.println("compareIntArrays: not same length!");
	    //return compareInts(a.length, b.length);
	    throw new IllegalArgumentException("Arrays are not the same length");
	}
	for (int i=0; i < a.length; i++) {
	    int result = compareRanksAceHigh(a[i], b[i]);
	    if (result != 0) {
		return result;
	    }
	}
	return 0;
    }
    public static int compareRankArraysAceLow(int a[], int b[]) {
	if (a.length != b.length) {
	    throw new IllegalArgumentException("Arrays are not the same length");
	}
	for (int i=0; i < a.length; i++) {
	    int result = compareRanksAceLow(a[i], b[i]);
	    if (result != 0) {
		return result;
	    }
	}
	return 0;
    }
}
