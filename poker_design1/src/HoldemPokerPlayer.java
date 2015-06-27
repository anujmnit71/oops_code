/*
 * HoldemPokerPlayer.java
 *
 * George Ferguson, ferguson@cs.rochester.edu, 24 Sep 1998
 * Time-stamp: <Thu Jan 19 15:45:38 EST 2006 ferguson>
 *
 * Based on HoldemPokerClient code, but decides what to do rather than
 * letting the human decide. Not (yet) very smart.
 * That is, "smart" mode is the same as default. Sorry.
 */

import java.util.*;
import java.awt.*;

public class HoldemPokerPlayer extends PokerPlayer {
    // Members
    private PlayerVector players;
    private Pot pot;
    private CardVector common;
    private PokerFrame frame;
    private Player myself;
    private int lowLimit, highLimit, currentLimit;
    private int maxRaisesPerRound = 3;
    private int numRaises;
    private int actionAmt;
    private String mode;
    // Constructor
    public HoldemPokerPlayer(String displaySize, String name, int buyin,
			     String m) {
	players = new PlayerVector();
	pot = new Pot();
	common = new CardVector();
	frame = new PokerFrame(displaySize, players, pot);
	frame.setTitle("IPP Poker Player - Texas Hold 'Em - " + name);
	Font buttonFont;
	if (displaySize.equalsIgnoreCase("small")) {
	    buttonFont = new Font("SansSerif", Font.BOLD, 8);
	} else {
	    buttonFont = new Font("SansSerif", Font.BOLD, 14);
	}
	myself = new Player(name, buyin, 7);
	mode = m;
    }
    // Methods
    public void receive(String msg) {
	frame.displayMsg("<" + msg);
	if (msg.equals("")) {
	    return;
	}
	StringTokenizer tokens = new StringTokenizer(msg, " \t");
	try {
	    String cmd = tokens.nextToken();
	    if (cmd.equalsIgnoreCase("IPP")) {
		send("BUYIN " + myself.getName() + " " + myself.getCash());
	    } else if (cmd.equalsIgnoreCase("WELCOME")) {
		// Ignore
	    } else if (cmd.equalsIgnoreCase("NEWGAME")) {
		String gametype = tokens.nextToken();
		lowLimit = new Integer(tokens.nextToken()).intValue();
		highLimit = new Integer(tokens.nextToken()).intValue();
		maxRaisesPerRound = new Integer(tokens.nextToken()).intValue();
		currentLimit = lowLimit;
	    } else if (cmd.equalsIgnoreCase("PLAYER")) {
		String name = tokens.nextToken();
		if (name.equals(myself.getName())) {
		    players.add(myself);
		    frame.addPlayer(myself);
		} else {
		    int cash = new Integer(tokens.nextToken()).intValue();
		    Player p = new Player(name, cash, 7);
		    players.add(p);
		    frame.addPlayer(p);
		}
	    } else if (cmd.equalsIgnoreCase("BUTTON")) {
		String name = tokens.nextToken();
		Player p = players.find(name);
		if (p != null) {
		    players.setPlayerOnButton(p);
		}
		players.resetForHand();
		common.removeAllElements();
		frame.clearCommonCards();
		currentLimit = lowLimit;
		numRaises = 0;
	    } else if (cmd.equalsIgnoreCase("ANTE")) {
		int amt = new Integer(tokens.nextToken()).intValue();
		for (int i=0; i < players.size(); i++) {
		    Player p = players.nth(i);
		    if (!p.isBusted()) {
			p.subtractCash(amt);
			pot.addCash(amt);
		    }
		}
		frame.update();
	    } else if (cmd.equalsIgnoreCase("DEAL")) {
		for (int i=0; i < 2; i++) {
		    myself.addCard(Card.fromString(tokens.nextToken()));
		}
		for (int i=0; i < players.size(); i++) {
		    Player p = players.nth(i);
		    if (p != myself && !p.isBusted()) {
			p.addCard(Card.fromString("??", false));
			p.addCard(Card.fromString("??", false));
		    }
		}
	    } else if (cmd.equalsIgnoreCase("FLOP")) {
		for (int i=0; i < 3; i++) {
		    Card c = Card.fromString(tokens.nextToken());
		    common.add(c);
		    frame.addCommonCard(c);
		}
		players.resetForBettingRound();
		numRaises = 0;
	    } else if (cmd.equalsIgnoreCase("TURN")) {
		Card c = Card.fromString(tokens.nextToken());
		common.add(c);
		frame.addCommonCard(c);
		currentLimit = highLimit;
		players.resetForBettingRound();
		numRaises = 0;
	    } else if (cmd.equalsIgnoreCase("RIVER")) {
		Card c = Card.fromString(tokens.nextToken());
		common.add(c);
		frame.addCommonCard(c);
		players.resetForBettingRound();
		numRaises = 0;
	    } else if (cmd.equalsIgnoreCase("ACTION?")) {
		frame.setStatus(msg);
		String what = tokens.nextToken();
		actionAmt = new Integer(tokens.nextToken()).intValue();
		int mycash = myself.getCash();
		if (what.equalsIgnoreCase("BLIND")) {
		    // BLIND or TAPOUT possible
		    if (actionAmt <= mycash) {
			send("BLIND " + actionAmt);
		    } else {
			send("TAPOUT " + mycash);
		    }
		} else if (what.equalsIgnoreCase("STRADDLE")) {
		    // STRADDLE, FOLD or TAPOUT possible
		    if (mode.equals("random") &&
			RandomInt.random(100) < 50) {
			send("FOLD");
		    } else {
			if (actionAmt <= mycash) {
			    send("STRADDLE " + actionAmt);
			} else {
			    send("TAPOUT " + mycash);
			}
		    }
		} else if (actionAmt == 0) {
		    // CHECK, OPEN or FOLD possible
		    if (mode.equals("aggresive")) {
			send("OPEN");
		    } else if (mode.equals("random")) {
			int r = RandomInt.random(100);
			if (r < 33) {
			    send("OPEN " + currentLimit);
			} else if (r < 66) {
			    send("CHECK");
			} else {
			    send("FOLD");
			}
		    } else {
			send("CHECK");
		    }
		} else {
		    // CALL, RAISE, TAPOUT, or FOLD possible
		    int raiseAmt = actionAmt + currentLimit;
		    boolean canRaise = (raiseAmt <= mycash) && (numRaises < maxRaisesPerRound);
		    boolean canCall = (actionAmt <= mycash);
		    if (mode.equals("aggresive")) {
			if (canRaise) {
			    send("RAISE " + raiseAmt);
			} else if (canCall) {
			    send("CALL " + actionAmt);
			} else {
			    send("TAPOUT " + mycash);
			}
		    } else if (mode.equals("random")) {
			if (canRaise) {
			    int r = RandomInt.random(100);
			    if (r < 33) {
				send("CALL " + actionAmt);
			    } else if (r < 66) {
				send("RAISE " + raiseAmt);
			    } else {
				send("FOLD");
			    }
			} else if (canCall) {
			    int r = RandomInt.random(100);
			    if (r < 50) {
				send("CALL " + actionAmt);
			    } else {
				send("FOLD");
			    }
			} else {
			    int r = RandomInt.random(100);
			    if (r < 50) {
				send("TAPOUT " + mycash);
			    } else {
				send("FOLD");
			    }
			}
		    } else {
			// Else default mode
			if (canCall) {
			    send("CALL " + actionAmt);
			} else {
			    send("TAPOUT " + mycash);
			}
		    }
		}
	    } else if (cmd.equalsIgnoreCase("SHOW?")) {
		players.setLastMsg("");
		frame.setStatus(cmd);
		HandType hand = HoldemPokerGame.bestHandType(myself, common);
		send("SHOW " + hand);
	    } else if (cmd.equalsIgnoreCase("BEAT?")) {
		msg = msg.substring(5).trim();
		try {
		    HandType hand = HandType.fromString(msg);
		    HandType myhand = HoldemPokerGame.bestHandType(myself, common);
		    if (myhand.compare(hand) < 0) {
			send("NO");
		    } else {
			send("YES " + myhand);
		    }
		} catch (HandTypeFormatException ex) {
		    System.err.println("Yow! Doesn't look like a hand type to me: " + msg);
		    send("NO");
		}
	    } else if (cmd.equalsIgnoreCase("SHOW")) {
		String name = tokens.nextToken();
		Player p = players.find(name);
		p.removeAllCards();
		p.addCard(Card.fromString(tokens.nextToken()));
		p.addCard(Card.fromString(tokens.nextToken()));
		players.setLastMsg("");
		p.setLastMsg(msg.substring(11));
	    } else if (cmd.equalsIgnoreCase("WINNER")) {
		String name = tokens.nextToken();
		int amt = new Integer(tokens.nextToken()).intValue();
		Player p = players.find(name);
		if (p != null) {
		    p.addCash(amt);
		}
		pot.reset();
		frame.update();
	    } else if (cmd.equalsIgnoreCase("BUSTED")) {
		String name = tokens.nextToken();
		players.find(name).setBusted(true);
	    } else if (cmd.equalsIgnoreCase("QUIT")) {
		String name = tokens.nextToken();
		Player p = players.find(name);
		p.setCash(0);
		p.setBusted(true);
		try {
		    players.foldPlayer(p);
		} catch (OnePlayerLeftException ex) {
		}
	    } else if (cmd.equalsIgnoreCase("GAMEOVER")) {
		String name = tokens.nextToken();
		doMessageDialog("Game Over", "Winner: " + name);
	    } else if (cmd.equalsIgnoreCase("OK")) {
		handlePlayerMessage(myself, tokens);
		frame.setStatus("");
	    } else if (cmd.equalsIgnoreCase("FROM")) {
		String name = tokens.nextToken();
		Player p = players.find(name);
		handlePlayerMessage(p, tokens);
	    } else if (cmd.equalsIgnoreCase("ERROR")) {
		doErrorDialog(msg);
	    } else {
		System.out.println("unexpected message: " + msg);
	    }
	} catch (NoSuchElementException ex) {
	    System.err.println("not enough tokens in message: " + msg);
	}
    }
    private void handlePlayerMessage(Player p, StringTokenizer tokens) {
	String submsg = tokens.nextToken();
	if (submsg.equalsIgnoreCase("BLIND") ||
	    submsg.equalsIgnoreCase("STRADDLE") ||
	    submsg.equalsIgnoreCase("OPEN") ||
	    submsg.equalsIgnoreCase("CALL") ||
	    submsg.equalsIgnoreCase("RAISE") ||
	    submsg.equalsIgnoreCase("TAPOUT")) {
	    // Update pot and player
	    int amt = new Integer(tokens.nextToken()).intValue();
	    p.subtractCash(amt);
	    pot.addCash(amt);
	    frame.update();
	    submsg = submsg + " " + amt;
	    // Keep track of number of raises
	    if (submsg.equalsIgnoreCase("RAISE")) {
		numRaises += 1;
	    }
	} else if (submsg.equalsIgnoreCase("FOLD")) {
	    p.setFolded(true);
	}
	// Update message display
	while (tokens.hasMoreTokens()) {
	    submsg = submsg + " " + tokens.nextToken();
	}
	p.setLastMsg(submsg);
    }
    private void doErrorDialog(String msg) {
	doMessageDialog("IPP Error", msg);
    }
    private void doMessageDialog(String title, String msg) {
	MessageDialog err = new MessageDialog(frame, title, msg);
	Point ploc = frame.getLocationOnScreen();
	Dimension psize = frame.getSize();
	Dimension child = err.getSize();
	err.setLocation(ploc.x + (psize.width-child.width)/2,
			ploc.y + psize.height/2 + 210);
	// This will block
	err.show();
    }
    public void send(String msg) {
	// Actually send it
	super.send(msg);
	// Record it in the message window
	frame.displayMsg(">" + msg);
    }
}
