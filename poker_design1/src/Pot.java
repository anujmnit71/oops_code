/*
 * Pot.java
 *
 * George Ferguson, ferguson@cs.rochester.edu,  8 Sep 1998
 * Time-stamp: <Mon Oct  5 16:56:16 EDT 1998 ferguson>
 */

public class Pot {
    // Members
    private int cash = 0;
    // Methods
    public void reset() {
    	cash = 0;
	display();
    }
    public int getCash() {
    	return cash;
    }
    public void addCash(int amt) {
    	cash += amt;
	display();
    }
    public void setCash(int amt) {
    	cash = amt;
	display();
    }
    private void display() {
	//System.out.println("display pot: $" + cash);
    }
}
