/* @author Anton Kibalnik
A playing card consists of a 13 possible ranks together with 4 possible suits, and can be implemented in several
ways. One can represent a card with single integer between 0 and 51 which maps to a specific card in the deck.
This is the minimal solution. Another approach is to use two separate integers mapping to the rank and suit,
respectively. The implementation here makes use of Enumerables. Although possibly not the most efficient in terms
of memory, this design choice keeps higher level design in mind by providing something that is more readable and
restrictive (i.e. does away with handling incorrect integer ranges, etc).

Cards are comparable by their rank.
*/

package poker;

@SuppressWarnings("serial")
public class Card implements Comparable<Card>, java.io.Serializable {
	
	private Rank rank;
	private Suit suit;
	private String name, image;
	
	public Card(Rank r, Suit s) {
				
		rank = r;
		suit = s;
		name = rank+" of "+suit;
		image = s.ordinal()+"_"+r.ordinal()+".gif";
		
	}
		
	public Rank rank() { return rank; }
	public Suit suit() { return suit; }
	public String name() {	return name; }
	public String image() {	return image; }
	public int compareTo(Card c) { return rank.compareTo(c.rank()); }

}