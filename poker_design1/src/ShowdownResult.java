/*
 * ShowdownResult.java
 *
 * George Ferguson, ferguson@cs.rochester.edu, 31 Oct 1999
 * Time-stamp: <Thu Jan 19 14:27:45 EST 2006 ferguson>
 *
 * This class just bundles up the information gathered in the showdown.
 */

public class ShowdownResult {
    public PlayerVector winners;
    public HandType bestHand;
    public ShowdownResult(PlayerVector w, HandType b) {
	winners = w;
	bestHand = b;
    }
}
