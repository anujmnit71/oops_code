/* author: Anton Kibalnik
A Hand is a collection of five cards, which must be passed to the constructor. The HandType is evaluated at construction, 
and can then be accessed. The cards are sorted by rank, and then rearranged at evaluation by highest kinds first for 
comparison purposes. The implementation here is particularly elegant... A score 't' of 0 to 9 is calculated which then 
maps to a HandType. Straights (+4), flushes (+5), or both (+8) are checked for first, separately. If needed, the hand is 
then checked for containing kinds (pairs, trips, or quads). Finally, t = kind[2] + 3*kind[3] + 2*(kind[2]*kind[3]) + 7*kind[4]: 
+1 for each pair, +3 for a trip, +2 for a pair AND trip, and +7 for a quad.

Hands are compared by type first; if the types are equal, we must then do a card by card comparison.
*/

package poker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("serial")
public class Hand implements Comparable<Hand>, java.io.Serializable {
	
	private ArrayList<Card> h;
	private HandType type;
	
	public Hand(List<Card> cards) {
		
		h = new ArrayList<Card>(5);
		h.addAll(cards.subList(0,5));
		Collections.sort(h, Collections.reverseOrder());
		
		int[] diff = new int[4]; int t = 0;
		for(int i=0; i<4; i++) diff[i] = h.get(i).compareTo(h.get(i+1));
		
		if((diff[0]==1 || diff[0]==9) && diff[1]==1 && diff[2]==1 && diff[3]==1) { t+=4; if(diff[0]==9) h.add(h.remove(0)); }
		if(h.get(0).suit()==h.get(1).suit() && h.get(0).suit()==h.get(2).suit() && h.get(0).suit()==h.get(3).suit() && h.get(0).suit()==h.get(4).suit()) t+=5;
		if(t==9 && h.get(4).rank()!=Rank.Ten) t--;
			
		if(t==0) {
			
			int kind1 = 1, kind2 = 0;
			int[] kind = new int[5];
			
			for(int i=0; i<4; i++) {
				if(diff[i]==0) {
					kind1++;
					if(i==3 || diff[i+1]!=0) {
						kind[kind1]++;
						Collections.rotate(h.subList(kind2*(5-kind1-kind2), i+2), kind1+kind2*(3-kind1)-i-2);
						kind2 = kind1; kind1 = 1;
					}
				}
			}
			
			t = kind[2] + 3*kind[3] + 2*(kind[2]*kind[3]) + 7*kind[4];
						
		}
		
		type = HandType.values()[t];
		
	}
	
	public HandType type() { return type; }
	public Card card(int i) { return h.get(i); }

	public int compareTo(Hand h2) {
		
		int diff;
		if((diff=type.compareTo(h2.type()))!=0) return diff;
		for(int i=0; i<5; i++) if((diff=h.get(i).compareTo(h2.card(i)))!=0) return diff;
		return 0;
		
	}
	
}