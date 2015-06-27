/*
 * PokerGame.java
 *
 * George Ferguson, ferguson@cs.rochester.edu, 28 Sep 1998
 * Time-stamp: <Thu Jan 19 14:29:02 EST 2006 ferguson>
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

abstract public class PokerGame extends Thread {
	//
	// Members
	//
	protected PlayerVector players;
	protected Deck deck;
	protected Pot pot;
	protected boolean cardsSpeak;
	protected int lowLimit, highLimit;
	protected int maxRaisesPerRound = 3;
	protected int currentBet;
	protected int numRaises;
	protected Player callee;
	protected static final int MAX_BOGUS_MESSAGES = 5;
	protected PokerServerDisplay display;
	protected BufferedReader stdin;
	int pauseLevel = 0;
	//
	// Constructor
	//
	public PokerGame(int low, int high, int raises, boolean cspeak,
			boolean useDisplay, String displaySize) {
		cardsSpeak = cspeak;
		lowLimit = low;
		highLimit = high;
		maxRaisesPerRound = raises;
		deck = new Deck();
		players = new PlayerVector();
		pot = new Pot();
		if (useDisplay) {
			display = new PokerServerDisplay(this, displaySize);
			display.start();
		}
		stdin = new BufferedReader(new InputStreamReader(System.in));
	}
	//
	// Methods to be implemented by subclasses
	//
	abstract public String gameTypeString();
	abstract public int maxCardsPerPlayer();
	abstract public void playAHand();
	abstract public HandType bestHandType(Player p);
	abstract public boolean testHandType(Player p, HandType type);
	//
	// Methods common to all games
	//
	@Override
	public void run() {
		// Pause right away to let players join
		pause("Ready to start game", 0);
		// Send start of game messages
		broadcast("NEWGAME " + gameTypeString());
		for (int i=0; i < players.size(); i++) {
			Player p = players.nth(i);
			broadcast("PLAYER " + p.getName() + " " + p.getCash());
		}
		// Play until all but one player is busted
		while (players.numPlayersNotBusted() > 1) {
			pause("Ready to start hand", 1);
			playAHand();
		}
		// Send final message announcing winner
		for (int i=0; i < players.size(); i++) {
			Player p = players.nth(i);
			if (!p.isBusted()) {
				broadcast("GAMEOVER " + p.getName() + " " + p.getCash());
			}
		}
	}
	public void addPlayer(Player p) {
		players.add(p);
		if (display != null) {
			display.addPlayer(p);
		}
	}
	public PlayerVector getPlayers() {
		return players;
	}
	public Pot getPot() {
		return pot;
	}
	protected void resetForHand() {
		players.resetForHand();
		players.incrButton();
		broadcast("BUTTON " + players.getPlayerOnButton().getName());
		pot.reset();
		deck.reset();
		deck.shuffle();
		// For debugging
		System.out.print("Starting hand: ");
		for (int i=0; i < players.size(); i++) {
			Player p = players.nth(i);
			System.out.print(p.getName() + "=" + p.getCash() + " ");
		}
		System.out.println("");
	}
	protected void ante(int amt) {
		broadcast("ANTE " + amt);
		for (int i=0; i < players.size(); i++) {
			Player p = players.nth(i);
			if (!p.isBusted()) {
				p.subtractCash(amt);
				pot.addCash(amt);
			}
		}
		updateDisplay();
	}
	//
	// Betting round methods
	//
	protected void bettingRound(int limit, boolean withBlind)
			throws OnePlayerLeftException {
		// Special case: most everyone is out or tapped
		if (players.numPlayersStillBetting() <= 1) {
			return;
		}
		// Reset what everyone owes
		players.resetForBettingRound();
		// Player on the button (or next in order) bets first
		players.setCurrentFromButton();
		// Nothing bet yet
		currentBet = 0;
		numRaises = 0;
		// Now do the betting
		if (withBlind) {
			// Blind (can only incr if player quits/EOF)
			while (!blind(players.getCurrentPlayer(), limit/2)) {
				players.incrCurrentPlayerForBetting();
			}
			players.incrCurrentPlayerForBetting();
			// Straddle
			while (!straddle(players.getCurrentPlayer(), limit)) {
				players.incrCurrentPlayerForBetting();
			}
			players.incrCurrentPlayerForBetting();
		}
		// Normal betting
		while (!players.allPaidUp(currentBet)) {
			bet(players.getCurrentPlayer(), limit);
			players.incrCurrentPlayerForBetting();
		}
		updateDisplay();
	}
	protected boolean blind(Player player, int amt)
			throws OnePlayerLeftException {
		pause("Ready to ask " + player.getName() + " for blind", 5);
		int cash = player.getCash();
		int numBogusMessages = 0;
		while (numBogusMessages < MAX_BOGUS_MESSAGES) {
			sendTo(player, "ACTION? BLIND " + amt);
			String msg = recvFrom(player);
			if (msg == null) {
				handlePlayerQuit(player);
				return false;
			} else {
				StringTokenizer tokens = new StringTokenizer(msg, " \t");
				try {
					String cmd = tokens.nextToken();
					if (cmd.equalsIgnoreCase("BLIND")) {
						int stated_amt = new Integer(tokens.nextToken()).intValue();
						if (cash < amt) {
							sendTo(player, "ERROR Insufficient funds for BLIND");
						} else if (stated_amt != amt) {
							sendTo(player, "ERROR BLIND amount should be " + amt + " not " + stated_amt);
						} else {
							acknowledge(player, msg);
							player.setHasPlayed(true);
							player.subtractCash(amt);
							player.setCurrentBet(amt);
							pot.addCash(amt);
							currentBet = amt;
							callee = player;
							return true;
						}
					} else if (cmd.equalsIgnoreCase("TAPOUT")) {
						int stated_amt = new Integer(tokens.nextToken()).intValue();
						if (cash >= amt) {
							sendTo(player, "ERROR Too rich to TAPOUT");
						} else if (stated_amt != cash) {
							sendTo(player, "ERROR TAPOUT amount must be all your cash");
						} else {
							acknowledge(player, msg);
							player.setHasPlayed(true);
							player.setTappedOut(true);
							player.setTapOutAmount(pot.getCash());
							callee = player;
							return true;
						}
					} else {
						sendTo(player, "ERROR Expected BLIND or TAPOUT");
					}
				} catch (NoSuchElementException ex) {
					sendTo(player, "ERROR not enough tokens in message: " + msg);
				}
			}
			// If we get here the message was bogus
			numBogusMessages += 1;
		}
		// If we get here, they sent too many bogus messages
		sendTo(player, "ERROR Too many bogus messages -- goodbye");
		handlePlayerQuit(player);
		return false;
	}
	protected boolean straddle(Player player, int amt)
			throws OnePlayerLeftException {
		pause("Ready to ask " + player.getName() + " for straddle", 5);
		int cash = player.getCash();
		int numBogusMessages = 0;
		while (numBogusMessages < MAX_BOGUS_MESSAGES) {
			sendTo(player, "ACTION? STRADDLE " + amt);
			String msg = recvFrom(player);
			if (msg == null) {
				handlePlayerQuit(player);
				return false;
			} else {
				StringTokenizer tokens = new StringTokenizer(msg, " \t");
				try {
					String cmd = tokens.nextToken();
					if (cmd.equalsIgnoreCase("STRADDLE")) {
						int stated_amt = new Integer(tokens.nextToken()).intValue();
						if (cash < amt) {
							sendTo(player, "ERROR Insufficient funds to STRADDLE");
						} else if (stated_amt != amt) {
							sendTo(player, "ERROR STRADDLE amount should be " + amt + " not " + stated_amt);
						} else {
							acknowledge(player, msg);
							player.setHasPlayed(true);
							player.subtractCash(amt);
							player.setCurrentBet(amt);
							pot.addCash(amt);
							currentBet = amt;
							callee = player;
							return true;
						}
					} else if (cmd.equalsIgnoreCase("TAPOUT")) {
						int stated_amt = new Integer(tokens.nextToken()).intValue();
						if (cash >= amt) {
							sendTo(player, "ERROR Too rich to TAPOUT");
						} else if (stated_amt != cash) {
							sendTo(player, "ERROR TAPOUT amount must be all your cash");
						} else {
							acknowledge(player, msg);
							player.subtractCash(cash);
							pot.addCash(cash);
							player.setHasPlayed(true);
							player.setTappedOut(true);
							player.setTapOutAmount(pot.getCash());
							currentBet = amt;
							callee = player;
							return true;
						}
					} else if (cmd.equalsIgnoreCase("FOLD")) {
						acknowledge(player, msg);
						players.foldPlayer(player);
						return false;
					} else {
						sendTo(player, "ERROR Expected STRADDLE, TAPOUT, or FOLD");
					}
				} catch (NoSuchElementException ex) {
					sendTo(player, "ERROR not enough tokens in message: " + msg);
				}
			}
			// If we get here the message was bogus
			numBogusMessages += 1;
		}
		// If we get here, they sent too many bogus messages
		sendTo(player, "ERROR Too many bogus messages -- goodbye");
		handlePlayerQuit(player);
		return false;
	}
	protected void bet(Player player, int limit) throws OnePlayerLeftException {
		pause("Ready to ask " + player.getName() + " ACTION?", 5);
		player.setHasPlayed(true);
		int cash = player.getCash();
		int numBogusMessages = 0;
		while (numBogusMessages < MAX_BOGUS_MESSAGES) {
			int amt = currentBet - player.getCurrentBet();
			sendTo(player, "ACTION? OWING " + amt);
			String msg = recvFrom(player);
			if (msg == null) {
				handlePlayerQuit(player);
				return;
			} else {
				StringTokenizer tokens = new StringTokenizer(msg, " \t");
				try {
					String cmd = tokens.nextToken();
					if (cmd.equalsIgnoreCase("FOLD")) {
						acknowledge(player, msg);
						players.foldPlayer(player);
						return;
					} else if (cmd.equalsIgnoreCase("CALL")) {
						int stated_amt = new Integer(tokens.nextToken()).intValue();
						if (amt == 0) {
							sendTo(player, "ERROR Can't CALL before OPEN");
						} else if (cash < amt) {
							sendTo(player, "ERROR Insufficient funds to CALL");
						} else if (stated_amt != amt) {
							sendTo(player, "ERROR CALL amount should be " + amt + " not " + stated_amt);
						} else {
							acknowledge(player, msg);
							player.subtractCash(amt);
							pot.addCash(amt);
							player.setCurrentBet(currentBet);
							return;
						}
					} else if (cmd.equalsIgnoreCase("RAISE")) {
						int stated_amt = new Integer(tokens.nextToken()).intValue();
						if (amt == 0) {
							sendTo(player, "ERROR Can't RAISE before OPEN");
						} else if (numRaises >= maxRaisesPerRound) {
							sendTo(player, "ERROR Maximum number of raises exceeded: " + maxRaisesPerRound);
						} else {
							amt += limit;
							if (cash < amt) {
								sendTo(player, "ERROR Insufficient funds to RAISE");
							} else if (stated_amt != amt) {
								sendTo(player, "ERROR RAISE amount should be " + amt + " not " + stated_amt);
							} else {
								acknowledge(player, msg);
								player.subtractCash(amt);
								pot.addCash(amt);
								currentBet += limit;
								player.setCurrentBet(currentBet);
								callee = player;
								numRaises += 1;
								return;
							}
						}
					} else if (cmd.equalsIgnoreCase("CHECK")) {
						if (amt != 0) {
							sendTo(player, "ERROR Can't CHECK after OPEN");
						} else {
							acknowledge(player, msg);
							return;
						}
					} else if (cmd.equalsIgnoreCase("TAPOUT")) {
						int stated_amt = new Integer(tokens.nextToken()).intValue();
						if (amt == 0) {
							sendTo(player, "ERROR Can't TAPOUT before OPEN");
						} else if (cash >= amt) {
							sendTo(player, "ERROR Too rich to TAPOUT");
						} else if (stated_amt != cash) {
							sendTo(player, "ERROR TAPOUT amount must be all your cash");
						} else {
							acknowledge(player, msg);
							amt = player.getCash();
							player.subtractCash(amt);
							pot.addCash(amt);
							player.setTappedOut(true);
							player.setTapOutAmount(pot.getCash());
							return;
						}
					} else if (cmd.equalsIgnoreCase("OPEN")) {
						int stated_amt = new Integer(tokens.nextToken()).intValue();
						if (amt != 0) {
							sendTo(player, "ERROR Can't OPEN after OPEN");
						} else if (stated_amt != limit) {
							sendTo(player, "ERROR OPEN amount should be " + limit + " not " + stated_amt);
						} else {
							acknowledge(player, msg);
							amt = currentBet = limit;
							player.subtractCash(amt);
							pot.addCash(amt);
							player.setCurrentBet(currentBet);
							callee = player;
							return;
						}
					} else {
						if (amt == 0) {
							sendTo(player, "ERROR Expected CHECK, OPEN, or FOLD");
						} else {
							sendTo(player, "ERROR Expected CALL, RAISE, TAPOUT, or FOLD");
						}
					}
				} catch (NoSuchElementException ex) {
					sendTo(player, "ERROR not enough tokens in message: " + msg);
				}
			}
			numBogusMessages += 1;
		}
		// If we get here, they sent too many bogus messages
		sendTo(player, "ERROR Too many bogus messages -- goodbye");
		handlePlayerQuit(player);
		return;
	}
	//
	// Showdown methods
	//
	protected ShowdownResult showdown(PlayerVector ps) {
		if (ps.numPlayersNotFoldedOrBusted() > 1) {
			try {
				if (cardsSpeak) {
					return showdownCardsSpeak(ps);
				} else {
					return showdownNotCardsSpeak(ps);
				}
			} catch (OnePlayerLeftException ex) {
			}
		}
		// If we get here, there must be only one player left, so they win
		PlayerVector winner = new PlayerVector();
		for (int i=0; i < ps.size(); i++) {
			Player p = ps.nth(i);
			if (!p.isFolded()) {
				winner.addElement(p);
				break;
			}
		}
		return new ShowdownResult(winner, null);
	}
	// In "cards speak", we compute the best hand(s)
	protected ShowdownResult showdownCardsSpeak(PlayerVector ps) {
		PlayerVector winners = new PlayerVector();
		HandType bestHand = null;
		for (int i = 0; i < ps.size(); i++) {
			Player p = ps.nth(i);
			if (!p.isFolded() && !p.isBusted()) {
				HandType thisHand = bestHandType(p);
				//System.err.println("showdown: " + p.getName() + " " + thisHand);
				if (bestHand == null) {
					// First must be best
					winners.addElement(p);
					bestHand = thisHand;
				} else {
					// Else compare to current best
					int result = thisHand.compare(bestHand);
					if (result == 1) {
						// Strictly better
						winners.removeAllElements();
						winners.addElement(p);
						bestHand = thisHand;
					} else if (result == 0) {
						// Tie
						winners.addElement(p);
					} else {
						// Worse - do nothing
					}
				}
			}
		}
		return new ShowdownResult(winners, bestHand);
	}
	// Without "cards speak", we need to talk to the players about their hands
	protected ShowdownResult showdownNotCardsSpeak(PlayerVector ps)
			throws OnePlayerLeftException {
		HandType shown;
		ps.setCurrentPlayer(callee);
		while (ps.getCurrentPlayer().isFolded()) {
			ps.incrCurrentPlayer();
		}
		while ((shown = showdownDoShow(ps.getCurrentPlayer())) == null) {
			ps.incrCurrentPlayer();
		}
		return showdownDoBeat(ps, ps.getCurrentPlayer(), shown);
	}
	protected HandType showdownDoShow(Player p) throws OnePlayerLeftException {
		HandType hand;
		pause("Ready to ask " + p.getName() + " SHOW?", 6);
		int numBogusMessages = 0;
		while (numBogusMessages < MAX_BOGUS_MESSAGES) {
			sendTo(p, "SHOW?");
			String msg = recvFrom(p);
			if (msg == null) {
				handlePlayerQuit(p);
				return null;
			} else if (!Utilities.startsWithIgnoreCase(msg, "SHOW")) {
				sendTo(callee, "ERROR Expected SHOW response");
			} else {
				msg = msg.substring(4).trim();
				try {
					hand = HandType.fromString(msg);
				} catch (HandTypeFormatException ex) {
					sendTo(callee, "ERROR Couldn't parse handtype: " + ex);
					hand = null;
				}
				if (hand == null) {
					// Sent message in exception handler, above
				} else if (!testHandType(p, hand)) {
					sendTo(callee, "ERROR Handtype shown not supported by cards");
				} else {
					// Acknowledge is done a bit differently here
					sendTo(callee, "OK " + msg);
					showCardsForPlayer(p, hand);
					return hand;
				}
			}
			// If we get here the message was bogus
			numBogusMessages += 1;
		}
		// If we get here, they sent too many bogus messages
		sendTo(p, "ERROR Too many bogus messages -- goodbye");
		handlePlayerQuit(p);
		return null;
	}
	protected ShowdownResult showdownDoBeat(PlayerVector ps,
			Player p, HandType bestHand)
					throws OnePlayerLeftException {
		// Keep track of winner(s)
		PlayerVector winners = new PlayerVector();
		// Starting with the player who just has just shown
		winners.addElement(p);
		// Now ask everyone else
		while (true) {
			// Compute next player to ask
			Player lastp = p;
			int i = ps.indexOf(p);
			do {
				i = (i + 1) % ps.size();
				p = ps.nth(i);
			} while (p != lastp && (winners.indexOf(p) != -1 ||
					p.isFolded() || p.isBusted() || p.isBeaten()));
			// Wrapped around -> we're done
			if (p == lastp) {
				break;
			}
			// Otherwise check this player
			bestHand = showdownDoBeatPlayer(p, winners, bestHand);
		}
		return new ShowdownResult(winners, bestHand);
	}
	protected HandType showdownDoBeatPlayer(Player p, PlayerVector winners, HandType bestHand)
			throws OnePlayerLeftException {
		// This method modifies the winners vector...
		pause("Ready to ask " + p.getName() + " BEAT?", 6);
		int numBogusMessages = 0;
		while (numBogusMessages < MAX_BOGUS_MESSAGES) {
			sendTo(p, "BEAT? " + bestHand);
			String msg = recvFrom(p);
			if (msg == null) {
				handlePlayerQuit(p);
				return bestHand;
			} else if (Utilities.startsWithIgnoreCase(msg, "NO")) {
				p.setBeaten(true);
				// Acknowledge is done a bit differently here
				sendTo(p, "OK " + msg);
				broadcastToOthers(p, "FROM " + p.getName() + " FOLD");
				return bestHand;
			} else if (Utilities.startsWithIgnoreCase(msg, "YES")) {
				HandType thisHand;
				try {
					thisHand = HandType.fromString(msg.substring(4));
				} catch (HandTypeFormatException ex) {
					sendTo(p, "ERROR Couldn't parse handtype: " + ex);
					thisHand = null;
				}
				if (thisHand == null) {
					// Sent message already in exception handler, above
				} else if (!testHandType(p, thisHand)) {
					sendTo(p, "ERROR Invalid handtype shown");
				} else {
					int result = thisHand.compare(bestHand);
					if (result < 0) {
						sendTo(p, "ERROR Hand shown doesn't beat call");
					} else {
						// Acknowledge is done a bit differently here
						sendTo(p, "OK " + msg);
						showCardsForPlayer(p, thisHand);
						if (result == 1) {
							// Strictly better
							winners.removeAllElements();
							winners.addElement(p);
							return thisHand;
						} else if (result == 0) {
							// Tie
							winners.addElement(p);
							return bestHand;
						}
					}
				}
			}
			// If we get here the message was bogus
			numBogusMessages += 1;
		}
		// If we get here, they sent too many bogus messages
		sendTo(p, "ERROR Too many bogus messages -- goodbye");
		handlePlayerQuit(p);
		return bestHand;
	}
	protected void showCardsForPlayer(Player p, HandType type) {
		// Unfortunately, Vector.toString() is final so we can't adjust it
		// for use with CardVector, so we're stuck with this.
		String msg = "SHOW " + p.getName();
		CardVector cards = p.getHand();
		for (int i=0; i < cards.size(); i++) {
			msg = msg + " " + cards.nth(i);
		}
		msg = msg + " " + type;
		broadcastToOthers(p, msg);
	}
	//
	// Payoff methods (divide pot among winners; watch for tapped-out players)
	//
	protected void payoff(PlayerVector ps, int amt, ShowdownResult result, int limitcheck) {
		if (limitcheck <= 0) {
			System.err.println("Yow! limitcheck in payoff()");
			return;
		}
		PlayerVector winners = result.winners;
		HandType bestHand = result.bestHand;
		PlayerVector remainingPlayers = (PlayerVector)(ps.clone());
		//System.err.println("payoff: distributing " + amt);
		// Number of winners
		int nwinners = winners.size();
		// Expectation per winner
		int[] expectations = new int[nwinners];
		// Sum all expectations
		int totalExpectations = 0;
		// Compute each players "expectation"
		for (int i=0; i < nwinners; i++) {
			Player p = winners.nth(i);
			// This is the pot amount unless they're tapped out
			expectations[i] = p.isTappedOut() ? p.getTapOutAmount() : amt;
			// Keep the running sum
			totalExpectations += expectations[i];
			//System.err.println("payoff: " + p.getName() + ": expectation=" + expectations[i] + ", total=" + totalExpectations);
		}
		// Now pay each player a share based on their expectations
		int remainder = amt;
		for (int i=0; i < nwinners; i++) {
			float share = (float)expectations[i] / (float)totalExpectations;
			int payout = Math.min(expectations[i],
					Math.round(amt * share));
			Player p = winners.nth(i);
			//System.err.println("payoff: " + p.getName() + ": share=" + share + ", payout=" + payout);
			p.addCash(payout);
			String msg = "WINNER " + p.getName() + " " + payout +
					((bestHand != null) ? (" " + bestHand) : "");
			broadcast(msg);

			remainder -= payout;
			remainingPlayers.remove(p);
		}
		// If there's anything left, need to give it to runners-up
		if (remainder > 0) {
			//System.err.println("payoff: recursing to distribute " + remainder);
			if (remainingPlayers.numPlayersNotBusted() <= 0) {
				System.err.println("yow! no players left to distribute remainder: " + remainder);
			} else {
				// Reset for showdown
				remainingPlayers.setBeaten(false);
				// Compute runnersup (ie, winners among remaining players)
				boolean cardsSpeak_saved = cardsSpeak;
				cardsSpeak = true;
				result = showdown(remainingPlayers);
				cardsSpeak = cardsSpeak_saved;
				// Recurse to handle payoff
				limitcheck -= 1;
				payoff(remainingPlayers, remainder, result, limitcheck);
			}
		}
	}
	//
	// End of hand methods
	//
	protected void cleanup() {
		for (int i = 0; i < players.size(); i++) {
			Player p = players.nth(i);
			if (p.getCash() < lowLimit/2) {
				p.setBusted(true);
				broadcast("BUSTED " + p.getName());
			}
		}
		if (display != null) {
			display.clearCommonCards();
			updateDisplay();
		}
		// Done at start of hand, but also done here for nicer display
		pot.reset();
		players.resetForHand();
	}
	protected void handlePlayerQuit(Player p) throws OnePlayerLeftException {
		broadcastToOthers(p, "QUIT " + p.getName());
		p.setCash(0);
		p.setBusted(true);
		players.foldPlayer(p);
	}
	//
	// Message passing methods
	//
	public void acknowledge(Player p, String msg) {
		sendTo(p, "OK " + msg);
		broadcastToOthers(p, "FROM " + p.getName() + " " + msg);
		if (display != null) {
			p.setLastMsg(msg);
		}
	}
	public void broadcast(String msg) {
		players.broadcast(msg);
		msg = ">ALL " + msg;
		System.out.println(msg);
		if (display != null) {
			display.displayMsg(msg);
		}
	}
	public void sendTo(Player p, String msg) {
		p.sendTo(msg);
		msg = ">" + p.getName() + " " + msg;
		System.out.println(msg);
		if (display != null) {
			display.displayMsg(msg);
		}
	}
	public void broadcastToOthers(Player p, String msg) {
		int n = players.size();
		for (int i = 0; i < n; i++) {
			Player x = players.nth(i);
			if (x != p) {
				sendTo(x, msg);
			}
		}
	}
	public String recvFrom(Player p) {
		String name = p.getName();
		if (display != null) {
			display.setStatus("Waiting for reply from " + name);
		}
		String msg = p.recvFrom();
		String msg2 = "<" + name + " " + msg;
		System.out.println(msg2);
		if (display != null) {
			display.displayMsg(msg2);
		}
		return msg;
	}
	//
	// Methods for synchronization with display thread
	//
	public void setPauseLevel(int n) {
		pauseLevel = n;
	}
	public synchronized void pause(String msg, int level) {
		if (level != 0) {
			return;
		}
		if (display != null) {
			display.setStatus(msg);
			display.setPaused(true);
			updateDisplay();
			try {
				//System.err.println("PokerGame.pause: waiting");
				wait();
				//System.err.println("PokerGame.pause: done");
			} catch (InterruptedException ex) {
			}
		} else {
			System.out.println(msg);
			System.out.print("Hit return to continue");
			try {
				//System.err.println("PokerGame.pause: reading stdin");
				msg = stdin.readLine();
				//System.err.println("PokerGame.pause: done");
			} catch (IOException ex) {
				System.err.println("io error: " + ex);
			}
		}
	}
	public synchronized void unpause() {
		//System.err.println("PokerGame.unpause: calling notifyAll");
		notifyAll();
		//System.err.println("PokerGame.unpause: done");
	}
	public void updateDisplay() {
		if (display != null) {
			//System.err.println("PokerGame.updateDisplay: resuming display thread");
			display.resume();
			//System.err.println("PokerGame.updateDisplay: done");
		}
	}
}
