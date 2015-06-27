/* @author Anton Kibalnik
A Player is representation of a poker player. The class containts data defining the state of 
the player along with actions that can be performed by the player or actions on him by other 
players or the table.
*/

package poker;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class Player implements Comparable<Player>, java.io.Serializable {
	
	private String name;
	private int chips, toPot, inPot, sidePot;
	private Table table;
	private ArrayList<Card> cards;
	private boolean turn;
	private Hand hand;
	private String action;
	
	public Player(String n) {
		
		name = n;
		chips = 1000;
		toPot = inPot = sidePot = 0;
		table = null;
		cards = new ArrayList<Card>(2);
		turn = false;
		hand = null;
		action = " ";
		
	}
	
	public String name() { return name; }
	public int chips() { return chips; }
	public int toPot() { return toPot; }
	public int inPot() { return inPot; }
	public int sidePot() { return sidePot; }
	public Table table() { return table; }
	public ArrayList<Card> cards() { return cards; }
	public boolean turn() { return turn; }
	public Hand hand() { return hand; }
	public String action() { return action;	}
	public int compareTo(Player p) { return hand.compareTo(p.hand()); }
	
	//actions ON
	public void postSmall() { bet(table.small()); action = "Posted Small"; }
	public void postBig() { bet(2*table.small()); action = "Posted Big"; }
	public void deal(Card c) { cards.add(c); }
	public void yourTurn() { turn = true; action = " "; }
	public void clearToPot() { toPot=0; }
	public void setHand(Hand h) { hand = h; }
	public void setSidePot(int s) { sidePot=s; }
	public void giveChips(int c) { chips+=c; }
	public void returnCards() {	cards.clear(); inPot=0; }
	
	//actions BY
	public void join(Table t) {
		
		if(table!=null || t.isFull()) return;
		table = t;
		table.seats().set(table.seats().indexOf(null), this);
		table.players().add(table.seats().indexOf(this), this);
		
	}
	
	public void leaveTable() {
		
		if(table==null) return;
		if(table.inHand().contains(this)) fold();
		sitOut(true);
		table.players().remove(this);
		table.seats().set(table.seats().indexOf(this), null);
		table = null;
		
	}
	
	public void sitOut(boolean out) {
		
		if(out) table.in().remove(this);
		else if(chips!=0) { table.in().add(this); if(table.inHand().size()==0) table.next(); }
		
	}
	
	public void fold() {
		
		returnCards();
		table.hasToCall().remove(this);
		table.inHand().remove(this);
		
		if(turn) {
			
			turn = false;
			if(table.inHand().size()==1 || table.hasToCall().size()==0) table.next();
			else table.hasToCall().get(0).yourTurn();
			
		}
		
	}
	
	public void call() { bet(table.toCall()-toPot); action = toPot==0 ? "Checked" : "Called"; }

	public void raiseTo(int raise) {
		
		table.setCall(raise>chips+toPot ? chips+toPot : raise, table.inHand().indexOf(this)+1);
		bet(table.toCall()-toPot);
		action = "Raised";
		
	}
	
	private void bet(int bet) {
				
		if(chips>bet) {
			
			chips-=bet;
			toPot+=bet;
			inPot+=bet;
			table.toPot(bet);
			
		} else {
			
			toPot+=chips;
			inPot+=chips;
			table.toPot(chips);
			chips=0;
			table.allIn().add(this);
			
		}

		table.hasToCall().remove(this);
		
		if(turn) {
			
			turn = false;
			if(table.hasToCall().size()==0) table.next();
			else table.hasToCall().get(0).yourTurn();
			
		}
				
	}
	
}