/*
 * PokerClient.java
 *
 * George Ferguson, ferguson@cs.rochester.edu, 24 Sep 1998
 * Time-stamp: <Thu Jan 19 15:14:54 EST 2006 ferguson>
 */

import java.awt.Button;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

public class PokerClient extends Thread implements ActionListener, HandTypeSelectorInterface, DrawSelectorInterface {
	//
	// Members
	//
	private String serverHost;
	private int serverPort;
	private BufferedReader in;
	private PrintWriter out;
	private static final String USAGE =
			"usage: ipphuman [-server host[:port]] [-size large|small] name [buyin]";
	protected PlayerVector players;
	protected Pot pot;
	private CardVector common;
	private PokerFrame frame;
	protected Player myself;
	protected String gametype;
	protected int lowLimit, highLimit, currentLimit;
	protected int maxRaisesPerRound = 3;
	protected int numRaises;
	private int actionAmt;
	private Hashtable buttons;
	private static String buttonLabels[] = { "BLIND", "STRADDLE", "CHECK",
		"OPEN", "CALL", "RAISE",
		"TAPOUT", "FOLD",
		"YES", "NO" };
	private HandTypeSelector hts;
	private String htsprefix;
	private DrawSelector ds;
	private String dsprefix;
	//
	// Constructor
	//
	public PokerClient(String displaySize, String name, int buyin) {
		players = new PlayerVector();
		pot = new Pot();
		common = new CardVector();
		frame = new PokerFrame(displaySize, players, pot);
		frame.setTitle("IPP Poker Client - - " + name);
		Font buttonFont;
		if (displaySize.equalsIgnoreCase("small")) {
			buttonFont = new Font("SansSerif", Font.BOLD, 8);
		} else {
			buttonFont = new Font("SansSerif", Font.BOLD, 14);
		}
		buttons = new Hashtable();
		for (int i=0; i < buttonLabels.length; i++) {
			Button b = new Button(buttonLabels[i]);
			b.addActionListener(this);
			b.setEnabled(false);
			b.setFont(buttonFont);
			buttons.put(buttonLabels[i], b);
			frame.addButton(b);
		}
		// Can't know maxcards until NEWGAME is received... oh well
		myself = new Player(name, buyin, 7);
	}
	//
	// Main for client
	//
	public static void main(String[] argv) {
		String host = "localhost";
		int port = 9898;
		String name = "unnamed";
		int buyin = 1000;
		String displaySize = "large";
		int argc = argv.length;
		int i;
		for (i=0; i < argc && argv[i].startsWith("-"); i++) {
			if (argv[i].equals("-server")) {
				host = argv[++i];
				int j = host.indexOf(":");
				if (j > -1) {
					port = new Integer(host.substring(j+1)).intValue();
					host = host.substring(0, j);
				}
			} else if (argv[i].equalsIgnoreCase("-size")) {
				displaySize = argv[++i];
			} else {
				System.err.println("unknown option: " + argv[i]);
				System.err.println(USAGE);
				System.exit(-1);
			}
		}
		if (i < argc) {
			name = argv[i++];
			// Clean up whitespace
			int j;
			while ((j = name.indexOf(" ")) > -1) {
				name = name.substring(0, j) + "_" + name.substring(j+1);
			}
		} else {
			System.err.println(USAGE);
			System.exit(-1);
		}
		if (i < argc) {
			buyin = new Integer(argv[i++]).intValue();
		}
		// Create the client for this run of main()
		PokerClient client = new PokerClient(displaySize, name, buyin);
		client.serverHost = host;
		client.serverPort = port;
		// Need to lower priority (from default 5) for unknown reasons,
		// otherwise display does not refresh properly.
		client.setPriority(3);
		client.start();
	}
	//
	// Methods
	//
	@Override
	public void run() {
		try {
			// Connect to server
			Socket sock = new Socket(serverHost, serverPort);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);
			// Testing code:
			//in = new BufferedReader(new InputStreamReader(System.in));
			//out = System.out;
			// Read lines from server until EOF
			while (true) {
				String line = in.readLine();
				if (line == null) {
					System.exit(0);
				} else {
					receive(line);
				}
			}
		} catch (IOException ex) {
			System.err.println("IO error: " + ex);
			System.exit(-1);
		}
	}
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
				gametype = tokens.nextToken();
				frame.setTitle("IPP Poker Client - " + gametype + " - " +
						myself.getName());
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
					// Should get maxcards from type of game
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
				resetForHand();
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
				// Read my cards
				int n = 0;
				while (tokens.hasMoreTokens()) {
					myself.addCard(Card.fromString(tokens.nextToken()));
					// Keep track of how many were dealt
					n += 1;
				}
				// Give everyone else that many cards facedown
				for (int i=0; i < players.size(); i++) {
					Player p = players.nth(i);
					if (p != myself && !p.isBusted()) {
						for (int j=0; j < n; j++) {
							p.addCard(Card.fromString("??", false));
						}
					}
				}
			} else if (cmd.equalsIgnoreCase("FLOP")) {
				for (int i=0; i < 3; i++) {
					frame.addCommonCard(Card.fromString(tokens.nextToken()));
				}
				resetForBettingRound();
			} else if (cmd.equalsIgnoreCase("TURN")) {
				frame.addCommonCard(Card.fromString(tokens.nextToken()));
				currentLimit = highLimit;
				resetForBettingRound();
			} else if (cmd.equalsIgnoreCase("RIVER")) {
				frame.addCommonCard(Card.fromString(tokens.nextToken()));
				resetForBettingRound();
			} else if (cmd.equalsIgnoreCase("UP")) {
				myself.addCard(Card.fromString(tokens.nextToken()), true);
				resetForBettingRound();
			} else if (cmd.equalsIgnoreCase("DOWN")) {
				// Show me my down card, please
				myself.addCard(Card.fromString(tokens.nextToken()), true);
				resetForBettingRound();
			} else if (cmd.equalsIgnoreCase("ACTION?")) {
				frame.setStatus(msg);
				String what = tokens.nextToken();
				actionAmt = new Integer(tokens.nextToken()).intValue();
				int mycash = myself.getCash();
				if (what.equalsIgnoreCase("BLIND")) {
					if (actionAmt <= mycash) {
						((Button)buttons.get("BLIND")).setEnabled(true);
					} else {
						((Button)buttons.get("TAPOUT")).setEnabled(true);
					}
				} else if (what.equalsIgnoreCase("STRADDLE")) {
					((Button)buttons.get("FOLD")).setEnabled(true);
					if (actionAmt <= mycash) {
						((Button)buttons.get("STRADDLE")).setEnabled(true);
					} else {
						((Button)buttons.get("TAPOUT")).setEnabled(true);
					}
				} else if (actionAmt == 0) {
					((Button)buttons.get("CHECK")).setEnabled(true);
					if (mycash >= currentLimit) {
						((Button)buttons.get("OPEN")).setEnabled(true);
					}
					((Button)buttons.get("FOLD")).setEnabled(true);
				} else {
					((Button)buttons.get("FOLD")).setEnabled(true);
					if (actionAmt > mycash) {
						((Button)buttons.get("TAPOUT")).setEnabled(true);
					} else {
						((Button)buttons.get("CALL")).setEnabled(true);
						if (actionAmt + currentLimit <= mycash &&
								numRaises < maxRaisesPerRound) {
							((Button)buttons.get("RAISE")).setEnabled(true);
						}
					}
				}
			} else if (cmd.equalsIgnoreCase("DRAW?")) {
				players.setLastMsg("");
				frame.setStatus(cmd);
				doDrawSelector("DRAW");
			} else if (cmd.equalsIgnoreCase("DRAWN")) {
				int n = new Integer(tokens.nextToken()).intValue();
				for (int i=0; i < n; i++) {
					Card c = Card.fromString(tokens.nextToken());
					myself.addCard(c);
				}
			} else if (cmd.equalsIgnoreCase("SHOW?")) {
				players.setLastMsg("");
				frame.setStatus(cmd);
				doHandTypeSelector("SHOW");
			} else if (cmd.equalsIgnoreCase("SHOW")) {
				String name = tokens.nextToken();
				Player p = players.find(name);
				p.removeAllCards();
				p.addCard(Card.fromString(tokens.nextToken()));
				p.addCard(Card.fromString(tokens.nextToken()));
				players.setLastMsg("");
				p.setLastMsg(msg.substring(11));
			} else if (cmd.equalsIgnoreCase("BEAT?")) {
				frame.setStatus(msg);
				((Button)buttons.get("YES")).setEnabled(true);
				((Button)buttons.get("NO")).setEnabled(true);
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
		} else if (submsg.equalsIgnoreCase("UP")) {
			p.addCard(Card.fromString(tokens.nextToken()), true);
			// Don't update message display
			return;
		} else if (submsg.equalsIgnoreCase("DOWN")) {
			p.addCard(Card.fromString("??"), false);
			// Don't update message display
			return;
		}
		// Update message display
		while (tokens.hasMoreTokens()) {
			submsg = submsg + " " + tokens.nextToken();
		}
		p.setLastMsg(submsg);
	}
	public void resetForHand() {
		players.resetForHand();
		frame.clearCommonCards();
		currentLimit = lowLimit;
		numRaises = 0;
	}
	public void resetForBettingRound() {
		players.resetForBettingRound();
		numRaises = 0;
	}
	@Override
	public void actionPerformed(ActionEvent evt) {
		String arg = evt.getActionCommand();
		//System.out.println("Button: " + arg);
		if (arg.equals("YES")) {
			// Answer yes to BEAT? so need to get hand
			doHandTypeSelector("YES");
		} else {
			// Otherwise our answer is based on the button label
			String msg;
			if (arg.equals("BLIND") || arg.equals("STRADDLE") ||
					arg.equals("CALL")) {
				msg = arg + " " + actionAmt;
			} else if (arg.equals("OPEN")) {
				msg = arg + " " + currentLimit;
			} else if (arg.equals("RAISE")) {
				msg = arg + " " + (actionAmt+currentLimit);
			} else if (arg.equals("TAPOUT")) {
				msg = arg + " " + myself.getCash();
			} else {
				msg = arg;
			}
			send(msg);
		}
		// Turn off all buttons
		Enumeration elements = buttons.elements();
		while (elements.hasMoreElements()) {
			((Button)(elements.nextElement())).setEnabled(false);
		}
		// Clear status display
		frame.setStatus("");
	}
	private void doHandTypeSelector(String prefix) {
		// Save prefix for later use in reply
		htsprefix = prefix;
		if (hts == null) {
			hts = new HandTypeSelector(frame, this);
			Point ploc = frame.getLocationOnScreen();
			Dimension psize = frame.getSize();
			Dimension child = hts.getSize();
			hts.setLocation(ploc.x + (psize.width-child.width)/2,
					ploc.y + psize.height/2 + 210);
		}
		// This will block then invoke handTypeSelectorCB() when done
		hts.show();
	}
	@Override
	public void handTypeSelectorCB(HandType type) {
		send(htsprefix + " " + type);
		frame.setStatus("");
	}
	private void doDrawSelector(String prefix) {
		// Save prefix for later use in reply
		dsprefix = prefix;
		if (ds == null) {
			ds = new DrawSelector(frame, this);
			Point ploc = frame.getLocationOnScreen();
			Dimension psize = frame.getSize();
			Dimension child = ds.getSize();
			ds.setLocation(ploc.x + (psize.width-child.width)/2,
					ploc.y + psize.height/2 + 210);
		}
		// This will block then invoke drawSelectorCB() when done
		ds.show();
	}
	@Override
	public void drawSelectorCB(String str) {
		// Need to remove cards from my hand (hack pending better selector)
		StringTokenizer tokens = new StringTokenizer(str);
		try {
			int n = new Integer(tokens.nextToken()).intValue();
			for (int i=0; i < n; i++) {
				Card c = Card.fromString(tokens.nextToken());
				Card cc = myself.findMatchingCard(c);
				if (cc == null) {
					System.err.println("yow: bogus card in DRAw message: " + c);
				} else {
					myself.removeCard(cc);
				}
			}
		} catch (NoSuchElementException ex) {
			System.err.println("yow: bogus DRAW message from selector");
		}
		// Now we're ready to send the message
		send(dsprefix + " " + str);
		frame.setStatus("");
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
		out.println(msg);
		// Record it in the message window
		frame.displayMsg(">" + msg);
	}
}
