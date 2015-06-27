/*
 * CardVector.java
 *
 * George Ferguson, ferguson@cs.rochester.edu,	8 Sep 1998
 * Time-stamp: <Thu Jan 19 14:29:13 EST 2006 ferguson>
 */

import java.util.Vector;

public class CardVector extends Vector {
    // Constants
    private static final int INCR_ACEHIGH = 1;
    private static final int INCR_ACELOW = 2;
    private static final int DECR_ACEHIGH = 3;
    private static final int DECR_ACELOW = 4;
    // Methods
    public void add(Card x) {
	addElement(x);
    }
    public Card nth(int i) {
	return (Card)elementAt(i);
    }
    public void reset() {
	removeAllElements();
    }
    // Sorting
    public void sortIncreasingAceLow() {
	sort(INCR_ACELOW);
    }
    public void sortIncreasingAceHigh() {
	sort(INCR_ACEHIGH);
    }
    public void sortDecreasingAceLow() {
	sort(DECR_ACELOW);
    }
    public void sortDecreasingAceHigh() {
	sort(DECR_ACEHIGH);
    }
    private void sort(int how) {
	// This is a shell sort--so sue me.
	int n = size();
	int incr = n / 2;
	while (incr >= 1) {
	    for (int i = incr; i < n; i++) {
		Card temp = (Card)elementAt(i);
		int j = i;
		while (j >= incr &&
		       compare(how, temp, (Card)elementAt(j-incr)) < 0) {
		    setElementAt(elementAt(j - incr), j);
		    j -= incr;
		}
		setElementAt(temp, j);
	    }
	    incr /= 2;
	}
    }
    private int compare(int how, Card a, Card b) {
	int arank = a.rank();
	int brank = b.rank();
	// For descreasing comparison, reverse parameters
	if (how == DECR_ACELOW || how == DECR_ACEHIGH) {
	    int tmp = arank;
	    arank = brank;
	    brank = tmp;
	}
	// Then compare ranks using appropriate function
	if (how == INCR_ACEHIGH || how == DECR_ACEHIGH) {
	    return Card.compareRanksAceHigh(arank, brank);
	} else {
	    return Card.compareRanksAceLow(arank, brank);
	}
    }
    // Predicates
    public boolean testSameSuit() {
	int suit0 = ((Card)elementAt(0)).suit();
    	for (int i=1; i < size(); i++) {
    	    if (((Card)elementAt(i)).suit() != suit0) {
    	    	return false;
    	    }
	}
    	return true;
    }
    public boolean testSameRank() {
	int rank0 = ((Card)elementAt(0)).rank();
    	for (int i=1; i < size(); i++) {
    	    if (((Card)elementAt(i)).rank() != rank0) {
    	    	return false;
    	    }
	}
    	return true;
    }
    public boolean testInSequenceIncreasing() {
	return testInSequenceIncreasing(0);
    }
    public boolean testInSequenceIncreasing(int start) {
    	for (int i=start+1; i < size(); i++) {
	    if (((Card)elementAt(i)).rank() !=
		((Card)elementAt(i-1)).rank() + 1) {
    	    	return false;
    	    }
	}
    	return true;
    }
    public boolean testInSequenceDecreasing() {
	return testInSequenceDecreasing(0);
    }
    public boolean testInSequenceDecreasing(int start) {
    	for (int i=start+1; i < size(); i++) {
	    if (((Card)elementAt(i)).rank() !=
		((Card)elementAt(i-1)).rank() - 1) {
    	    	return false;
    	    }
	}
	return true;
    }
    public Card findMatchingCard(Card c) {
    	for (int i=0; i < size(); i++) {
	    Card cc = (Card)elementAt(i);
	    if (c.rank() == cc.rank() && c.suit() == cc.suit()) {
		return cc;
	    }
	}
	return null;
    }
}
