/* @author Anton Kibalnik
The Table captures the bahavior of the entire poker table. Various lists of players represent 
who is in the game, who still has to call, who is all in, etc. The next method alerts the dealer 
to perform the next action necessary to advance the game. The dealer is responsible for keeping 
track of the state of each player, dealing out hands and cards, ranking players hands at the end 
of the round, awarding winning players chips, collecting chips, and calculating the best hand 
of each player given what cards or on the board.
*/

package poker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

@SuppressWarnings("serial")
public class Table implements java.io.Serializable {
	
	private static final int size = 9;
	private Deck deck;
	private ArrayList<Card> board, burn;
	private int small, dealer, pot, toPot, toCall;
	private String chat;
	private boolean ranked, awarded;
	private ArrayList<Player> seats, players, in, dealTo, inHand, hasToCall, allIn, mustShow, winners;
	
	public Table() {
		
		deck = new Deck();
		deck.shuffle();
		board = new ArrayList<Card>(5);
		burn = new ArrayList<Card>(3);
		small = 10;
		dealer = pot = toPot = toCall = 0;
		chat = "";
		ranked = awarded = false;
		
		seats = new ArrayList<Player>(size); for(int i=0; i<size; i++) seats.add(null);
		players = new ArrayList<Player>(size);
		in = new ArrayList<Player>(size);
		dealTo = new ArrayList<Player>(size);
		inHand = new ArrayList<Player>(size);
		hasToCall = new ArrayList<Player>(size);
		allIn = new ArrayList<Player>(size);
		mustShow = new ArrayList<Player>(size);
		winners = new ArrayList<Player>(size);
		
	}
	
	public ArrayList<Card> board() { return board; }
	public ArrayList<Card> burn() { return burn; }
	public int small() { return small; }
	public int dealer() { return dealer; }
	public int pot() { return pot; }
	public int toPot() { return toPot; }
	public int toCall() { return toCall; }
	public String chat() { return chat; }
	public boolean ranked() { return ranked; }
	public boolean awarded() { return awarded; }
	public boolean isFull() { return players.size()==9; }
	public int size() {	return players.size(); }
	
	//why not make them public? or seats(int i) ...get(i)
	public ArrayList<Player> seats() { return seats; }
	public ArrayList<Player> players() { return players; }
	public ArrayList<Player> in() { return in; }
	public ArrayList<Player> dealTo() { return dealTo; }
	public ArrayList<Player> inHand() { return inHand; }
	public ArrayList<Player> hasToCall() { return hasToCall; }
	public ArrayList<Player> allIn() { return allIn; }
	public ArrayList<Player> mustShow() { return mustShow; }
	public ArrayList<Player> winners() { return winners; }
	
	public void post(String message) { chat += message; }
	public void toPot(int chips) { toPot += chips; }
	
	public void setCall(int raise, int start) {
		
		toCall = raise;
		hasToCall.clear();
		hasToCall.addAll(inHand);
		hasToCall.removeAll(allIn);
		Collections.rotate(hasToCall, -start);
		
	}
	
	//DEALER METHODS----------------
	
	public void next() {
		
		if(inHand.size()==0) {
			
			ranked = awarded = false;
			dealTo.clear();
			dealTo.addAll(players);
			dealTo.retainAll(in);
			if(dealTo.size()>1) dealToPlayers();
			
		} else if(inHand.size()==1) {
			
			collectChips();
			award();
			
		} else {
			
			if(board.size()!=5) dealToBoard(board.size()==0 ? 3 : 1);
			else if(!ranked) rank();
			else award();
			
		}
		
	}

	private void dealToPlayers() {
				
		while(!in.contains(seats.get(dealer=(dealer+1)%size)));
		Collections.rotate(dealTo, -(dealTo.indexOf(seats.get(dealer))+1));
		dealTo.get(0).postSmall();
		dealTo.get(1).postBig();
		for(int i=0; i<2; i++) for(Player p : dealTo) p.deal(deck.top());
		inHand.addAll(dealTo);
		setCall(2*small,2);
		if(hasToCall.size()>0) hasToCall.get(0).yourTurn(); else next();
		
	}
	
	private void dealToBoard(int n) {
		
		collectChips();
		burn.add(deck.top());
		for (int i=0; i<n; i++) { board.add(deck.top()); }
		for(Player p : inHand) p.setHand(bestHand(board, p.cards()));
		setCall(0,0);
		if(hasToCall.size()>1) hasToCall.get(0).yourTurn(); else next();
		
	}
	
	private void rank() {
		//only rank players that have to show?
		collectChips();
		mustShow.clear();
		mustShow.addAll(inHand);
		Collections.sort(inHand, Collections.reverseOrder());
		int wins = 0, rank;
		do { while(inHand.size()>++wins && inHand.get(wins-1).compareTo(inHand.get(wins))==0); } while(allIn.contains(inHand.get(wins-1)) && inHand.get(wins-1).sidePot()!=pot);
		winners.addAll(inHand.subList(0, wins));
		for(int i=0; i<mustShow.size(); i++) {
			rank = 9;
			for(Player p : mustShow.subList(0,i)) if(inHand.indexOf(p)<rank) rank = inHand.indexOf(p);
			if(inHand.indexOf(mustShow.get(i))>rank && inHand.indexOf(mustShow.get(i))>=wins) mustShow.remove(i--);
		}
		ranked = true;
		
	}
	
	private void award() {
		
		int n = 0;
		do {
			
			winners.clear();
			winners.add(inHand.get(n));
			while(inHand.size()>++n && inHand.get(n-1).compareTo(inHand.get(n))==0) winners.add(inHand.get(n));
			for(Player p : winners) if(p.sidePot()==0) p.setSidePot(pot);
			Collections.sort(winners, new Comparator<Player>() {
				public int compare(Player p1, Player p2) { return p1.sidePot()-p2.sidePot(); }
			});
			for(int i=0; i<winners.size(); i++) for(int j=i; j<winners.size(); j++) winners.get(j).giveChips((winners.get(i).sidePot()-(i==0? 0 : winners.get(i-1).sidePot()))/(winners.size()-i));

		} while(winners.get(winners.size()-1).sidePot()!=pot);
		
		pot = 0;
		for(Player p : inHand) { p.setSidePot(0); p.returnCards(); }
		for(Player p : allIn) if(p.chips()==0) in.remove(p);
		winners.clear();
		mustShow.clear();
		allIn.clear();
		inHand.clear();
		board.clear();
		burn.clear();
		deck.shuffle();
		awarded = true;
		
	}

	private void collectChips() {
		
		int sidePot = 0;
		for(Player a : allIn) {
			sidePot = pot;
			for(Player p : dealTo) sidePot += p.toPot()<a.toPot() ? p.toPot() : a.toPot();
			a.setSidePot(sidePot);
		}
		
		if(inHand.size()>1) {
			
			int first = 0, second = 0, takeBack = 0;
			for(int i=0; i<dealTo.size(); i++) if(dealTo.get(i).toPot()>=dealTo.get(first).toPot()) first = i;
			second = (first+1)%inHand.size();
			for(int i=0; i<dealTo.size(); i++) if(i!=first && dealTo.get(i).toPot()>=dealTo.get(second).toPot()) second = i;
			if((takeBack=dealTo.get(first).toPot()-dealTo.get(second).toPot())!=0) { dealTo.get(first).giveChips(takeBack); toPot-=takeBack; }
			
		}

		for(Player p : dealTo) p.clearToPot();
		pot+=toPot;
		toPot=0;
		
	}

	private Hand bestHand(ArrayList<Card> board, ArrayList<Card> two) {
		
		Hand current, best;
		ArrayList<Card> all = new ArrayList<Card>(7);
		all.addAll(board);
		all.addAll(two);
		best = new Hand(all.subList(0,5));
		
		if(all.size()>5) {
			ArrayList<Card> chunk = new ArrayList<Card>(7);
			for(int i=0; i<all.size(); i++) {
				chunk.clear(); chunk.addAll(all); chunk.remove(i);
				if(chunk.size()>5) {
					ArrayList<Card> chunk2 = new ArrayList<Card>(7);
					for(int j=0; j<chunk.size(); j++) {
						chunk2.clear();	chunk2.addAll(chunk); chunk2.remove(j);
						current = new Hand(chunk2);
						if(current.compareTo(best)>0) best = current;
					}
				} else {
					current = new Hand(chunk);
					if(current.compareTo(best)>0) best = current;
				}
			}
		}

		return best;
		
	}
	
}