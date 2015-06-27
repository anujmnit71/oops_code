/* @author Anton Kibalnik
A Deck consists of an array of the 52 distinct playing cards along with a pointer to the top card.
The top() method returns the top card and increments the pointer.
The shuffle() method implements the Fisher-Yates (aka Knuth) shuffle algorithm.
It also resets the pointer to 0.
*/

package poker;

import java.util.Random;

@SuppressWarnings("serial")
public class Deck implements java.io.Serializable {
	
	private static final int size = 52;
	private int top;
	private Card[] deck;
	
	public Deck() {
		
		top = 0;
		deck = new Card[size];
		for(Suit s : Suit.values()) for(Rank r : Rank.values()) deck[4*r.ordinal()+s.ordinal()] = new Card(r,s);
		
	}
	
	public Card top() { return deck[top++]; }
	
	public void shuffle() {
		
		top = 0;
		int j; Card c; Random r = new Random();
		for(int i=51; i>0; i--) {
			j = r.nextInt(i+1);
			c = deck[j]; deck[j] = deck[i]; deck[i] = c;
		}
	
	}

}