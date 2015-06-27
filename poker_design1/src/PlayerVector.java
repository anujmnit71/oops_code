/*
 * PlayerVector.java
 *
 * George Ferguson, ferguson@cs.rochester.edu,  8 Sep 1998
 * Time-stamp: <Fri Nov  5 13:00:31 EST 1999 ferguson>
 */

import java.util.Vector;

public class PlayerVector extends Vector {
    // Members
    private int nplayers = 0;
    private int button = -1;
    private int current = -1;
    // Methods
    public void set(Player x, int i) {
	setElementAt(x, i);
    }
    public void add(Player x) {
	addElement(x);
	nplayers += 1;
    }
    public void remove(Player x) {
	removeElement(x);
	nplayers -= 1;
    }
    public Player nth(int i) {
	return (Player)elementAt(i);
    }
    public void incrButton() {
    	button = (button + 1) % nplayers;
    }
    public void setButton(int n) {
    	button = n;
    }
    public Player getPlayerOnButton() {
	if (button != -1) {
	    return (Player)elementAt(button);
	} else {
	    return null;
	}
    }
    public void setPlayerOnButton(Player p) {
	int n = indexOf(p);
	if (n != -1) {
	    button = n;
	}
    }
    public void setCurrentFromButton() {
    	current = button;
	Player p = nth(current);
    	if (p.isFolded() || p.isBusted() || p.isTappedOut()) {
    	    incrCurrentPlayerForBetting();
    	}
    }
    public Player getCurrentPlayer() {
	if (current > -1) {
	    return nth(current);
	} else {
	    return null;
	}
    }
    public void setCurrentPlayer(Player p) {
	int n = indexOf(p);
	if (n != -1) {
	    current = n;
	}
    }
    public void setNoCurrentPlayer() {
	current = -1;
    }
    public void incrCurrentPlayer() {
	if (nplayers <= 1) {
	    return;
	}
	Player p;
	do {
    	    current = (current + 1) % nplayers;
	    p = nth(current);
	} while (p.isFolded() || p.isBusted());
    }
    public void incrCurrentPlayerForBetting() {
	if (nplayers <= 1) {
	    return;
	}
	Player p;
	do {
    	    current = (current + 1) % nplayers;
	    p = nth(current);
	} while (p.isFolded() || p.isBusted() || p.isTappedOut());
    }
    public Player find(String name) {
    	for (int i=0; i < size(); i++) {
	    Player p = nth(i);
    	    if (name.equals(p.getName())) {
		return p;
	    }
    	}
	return null;
    }
    public void setLastMsg(String msg) {
	for (int i=0; i < size(); i++) {
	    nth(i).setLastMsg(msg);
	}
    }
    public void setBeaten(boolean b) {
	for (int i=0; i < size(); i++) {
	    nth(i).setBeaten(b);
	}
    }
    public void resetForBettingRound() {
    	for (int i=0; i < nplayers; i++) {
    	    nth(i).resetForBettingRound();
    	}
    }
    public void resetForHand() {
    	for (int i=0; i < nplayers; i++) {
    	    nth(i).resetForHand();
    	}
    }
    public boolean allPaidUp(int amt) {
    	for (int i=0; i < nplayers; i++) {
	    Player p = nth(i);
	    if (!p.isBusted() && !p.isFolded() && !p.isTappedOut() &&
		(!p.hasPlayed() || p.getCurrentBet() < amt)) {
	    	return false;
	     }
	}
	return true;
    }
    public int numPlayersStillBetting() {
	int n = 0;
    	for (int i=0; i < nplayers; i++) {
	    Player p = nth(i);
	    if (!p.isBusted() && !p.isFolded() && !p.isTappedOut()) {
		n += 1;
	     }
	}
	return n;
    }
    public int numPlayersNotFoldedOrBusted() {
	int n = 0;
    	for (int i=0; i < nplayers; i++) {
	    Player p = nth(i);
	    if (!(p.isFolded() || p.isBusted())) {
		n += 1;
	     }
	}
	return n;
    }
    public void foldPlayer(Player p) throws OnePlayerLeftException {
	p.setFolded(true);
	if (numPlayersNotFoldedOrBusted() == 1) {
	    throw new OnePlayerLeftException();
	}
    }
    public int numPlayersNotBusted() {
	int n = 0;
    	for (int i=0; i < nplayers; i++) {
	    if (!nth(i).isBusted()) {
		n += 1;
	     }
	}
	return n;
    }
    public void broadcast(String msg) {
    	for (int i=0; i < nplayers; i++) {
    	    nth(i).sendTo(msg);
    	}
    }
    public void broadcastToOthers(Player me, String msg) {
    	for (int i=0; i < nplayers; i++) {
    	    Player p = nth(i);
	    if (p != me) {
		p.sendTo(msg);
	    }
    	}
    }
}
