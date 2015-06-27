/*
 * Player.java
 *
 * George Ferguson, ferguson@cs.rochester.edu,  8 Sep 1998
 * Time-stamp: <Thu Nov  4 14:56:08 EST 1999 ferguson>
 */

import java.io.*;

public class Player {
    // Members
    private String name;
    private CardVector cards;
    private int cash;
    private int currentBet;
    private boolean folded;
    private boolean played;
    private boolean tappedOut;
    private int tapOutAmount;
    private boolean busted = false;
    private boolean beaten = false;
    private String lastMsg;
    private BufferedReader in;
    private PrintWriter out;
    private PlayerDisplay display;
    // Constructor
    public Player(String n, int buyin, int maxcards) {
	this(n, buyin, maxcards,
	     new BufferedReader(new InputStreamReader(System.in)),
	     new PrintWriter(System.out));
    }
    public Player(String n, int buyin, int maxcards, BufferedReader instream, PrintWriter outstream) {
    	name = n;
    	cash = buyin;
	cards = new CardVector();
	in = instream;
	out = outstream;
	display = new PlayerDisplay(this, maxcards);
    }
    // Methods
    public String getName() {
	return name;
    }
    public PlayerDisplay getDisplay() {
	return display;
    }
    public void setHand(CardVector c) {
	cards = c;
    }
    public CardVector getHand() {
	return cards;
    }
    public void addCard(Card c) {
	cards.add(c);
	display.addCard(c);
    }
    public void addCard(Card c, boolean faceup) {
	c.setFaceUp(faceup);
	addCard(c);
    }
    public void removeCard(Card c) {
	cards.removeElement(c);
	display.removeCard(c);
    }
    public void removeAllCards() {
	cards.removeAllElements();
	display.removeAllCards();
    }
    public void resetForHand() {
	if (cards != null) {
	    cards.reset();
	}
	removeAllCards();
    	setFolded(false);
	setTappedOut(false);
	setBeaten(false);
	setLastMsg("");
    }
    public void resetForBettingRound() {
    	setCurrentBet(0);
	setHasPlayed(false);
	setLastMsg("");
    }
    public int getCash() {
    	return cash;
    }
    public boolean hasCash(int amt) {
    	return cash >= amt;
    }
    public void addCash(int amt) {
    	setCash(cash + amt);
    }
    public void subtractCash(int amt) {
    	setCash(cash - amt);
    }
    public void setCash(int amt) {
	cash = amt;
	display.setCash(amt);
    }
    public int getCurrentBet() {
	return currentBet;
    }
    public void setCurrentBet(int amt) {
        currentBet = amt;
    }
    public boolean isFolded() {
        return folded;
    }
    public void setFolded(boolean b) {
    	folded = b;
	display.setFolded(b);
    }
    public boolean hasPlayed() {
        return played;
    }
    public void setHasPlayed(boolean b) {
    	played = b;
    }
    public boolean isTappedOut() {
        return tappedOut;
    }
    public void setTappedOut(boolean b) {
    	tappedOut = b;
    }
    public void setTapOutAmount(int amt) {
    	tapOutAmount = amt;
    }
    public int getTapOutAmount() {
    	return tapOutAmount;
    }
    public boolean isBusted() {
        return busted;
    }
    public void setBusted(boolean b) {
    	busted = b;
    }
    public boolean isBeaten() {
        return beaten;
    }
    public void setBeaten(boolean b) {
    	beaten = b;
    }
    public void setLastMsg(String msg) {
	lastMsg = msg;
	display.setLastMsg(msg);
    }
    public String getLastMsg() {
	return lastMsg;
    }
    public void sendTo(String msg) {
	//System.err.println("SEND " + name + ": " + msg);
	out.println(msg);
    }
    public String recvFrom() {
	//System.err.print("RECV " + name + ": ");
	try {
	    return in.readLine();
	} catch (EOFException ex) {
	    System.err.println("EOF from " + name);
	} catch (IOException ex) {
	    System.err.println("IO error: \"" + ex + "\" from " + name);
	}
	return null;
    }
    public Card findMatchingCard(Card c) {
	return cards.findMatchingCard(c);
    }
}
