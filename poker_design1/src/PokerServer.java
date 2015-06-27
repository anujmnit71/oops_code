/*
 * PokerServer.java
 *
 * George Ferguson, ferguson@cs.rochester.edu,  8 Sep 1998
 * Time-stamp: <Sun Oct 31 11:29:57 EST 1999 ferguson>
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class PokerServer extends Thread {
    // Members
    private PokerGame game;
    private int serverPort;
    private static final String USAGE =
	"usage: ippd [-port port] [-nodisplay] [-size large|small]\n            [-cardsspeak] [GAME [parms]]";
    // Constructor
    public PokerServer(int port, PokerGame g) {
	serverPort = port;
	game = g;
    }
    // Main for server
    public static void main(String[] argv) {
	int port = 9898;
	int numPlayers = 0;
	boolean useDisplay = true;
	boolean cardsSpeak = false;
	String displaySize = "large";
	int argc = argv.length;
	int i;
	// Process command-line options
	for (i=0; i < argc && argv[i].startsWith("-"); i++) {
	    if (argv[i].equalsIgnoreCase("-port")) {
		port = new Integer(argv[++i]).intValue();
	    } else if (argv[i].equalsIgnoreCase("-nodisplay")) {
		useDisplay = false;
	    } else if (argv[i].equalsIgnoreCase("-cardsspeak")) {
		cardsSpeak = true;
	    } else if (argv[i].equalsIgnoreCase("-size")) {
		displaySize = argv[++i];
	    } else {
		System.err.println("unknown option: " + argv[i]);
		System.err.println(USAGE);
		System.exit(-1);
	    }
	}
	// Next arg (if any) says what game to play
	String gamename = "holdem";
	if (i < argc) {
	    gamename = argv[i++];
	}
	// Now create the appropriate game (possibly using more cmd-line args)
	PokerGame game = null;
	if (gamename.equalsIgnoreCase("holdem")) {
	    int low = 10;
	    int high = 20;
	    int raises = 3;
	    if (i < argc) {
		low = new Integer(argv[i++]).intValue();
	    }
	    if (i < argc) {
		high = new Integer(argv[i++]).intValue();
	    }
	    if (i < argc) {
		raises = new Integer(argv[i++]).intValue();
	    }
	    game = new HoldemPokerGame(low, high, raises, cardsSpeak,
				       useDisplay, displaySize);
	} else if (gamename.equalsIgnoreCase("draw")) {
	    int low = 10;
	    int high = 20;
	    int raises = 3;
	    if (i < argc) {
		low = new Integer(argv[i++]).intValue();
	    }
	    if (i < argc) {
		high = new Integer(argv[i++]).intValue();
	    }
	    if (i < argc) {
		raises = new Integer(argv[i++]).intValue();
	    }
	    game = new DrawPokerGame(low, high, raises, cardsSpeak,
				     useDisplay, displaySize);
	} else if (gamename.equalsIgnoreCase("stud")) {
	    int numcards = 7;
	    int low = 10;
	    int high = 20;
	    int raises = 3;
	    if (i < argc) {
		numcards = new Integer(argv[i++]).intValue();
	    }
	    if (i < argc) {
		low = new Integer(argv[i++]).intValue();
	    }
	    if (i < argc) {
		high = new Integer(argv[i++]).intValue();
	    }
	    if (i < argc) {
		raises = new Integer(argv[i++]).intValue();
	    }
	    if (numcards == 5) {
		game = new FiveCardStudPokerGame(low, high, raises, cardsSpeak,
						 useDisplay, displaySize);
	    } else if (numcards == 7) {
		game = new SevenCardStudPokerGame(low, high, raises, cardsSpeak,
						  useDisplay, displaySize);
	    } else {
		System.err.println("illegal number of cards for stud: " + argv[i]);
		System.exit(-1);
	    }
	} else {
	    System.err.println("unknown game type: " + gamename);
	    System.exit(-1);
	}
	// Need to lower priority for unknown reasons
	game.setPriority(3);
	game.start();
	// Now create the server to handle connections
	PokerServer server = new PokerServer(port, game);
	// And start it
	server.start();
    }
    public void run() {
	try {
	    // Establish server socket
	    //System.err.println("PokerServer.run: creating server socket");
	    ServerSocket serverSock = new ServerSocket(serverPort, 5);
	    // Then do forever...
	    while (true) {
		// Wait for next client connection
		//System.err.println("PokerServer.run: accepting connections");
		Socket clientSock = serverSock.accept();
		// Create a new thread to handle this player
		System.err.println("accepted connection from " + 
				   clientSock.getInetAddress() + ":" +
				   clientSock.getPort());
		PlayerHandler ph = new PlayerHandler(game, clientSock);
		//ph.setPriority(3);
		ph.start();
	    }
	} catch (IOException ex) {
	    System.err.println("server io error: " + ex);
	    System.exit(-1);
	}
    }
}

// May need to get these messages into the display...

class PlayerHandler extends Thread {
    PokerGame game;
    BufferedReader in;
    PrintWriter out;
    public PlayerHandler(PokerGame g, Socket sock) {
	game = g;
	try {
	    in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
	    out = new PrintWriter(sock.getOutputStream(), true);
	} catch (IOException ex) {
	    System.err.println("error creating client streams: " + ex);
	}
	/* Testing
	in = new BufferedReader(new InputStreamReader(System.in));
	out = new PrintWriter(System.out, true);
	*/
    }
    public void run() {
	//System.err.println("PlayerHandler.run: starting");
	String name = "unknown";
	// Need to implement limit on number of bogus messages...
	while (true) {
	    out.println("IPP 2.0.0 send BUYIN message to join game");
	    try {
		String msg = in.readLine();
		try {
		    StringTokenizer tokens = new StringTokenizer(msg, " \t");
		    String cmd = tokens.nextToken();
		    if (cmd.equalsIgnoreCase("BUYIN")) {
			// Get name and buyin from message
			name = tokens.nextToken();
			int cash = new Integer(tokens.nextToken()).intValue();
			// Create player and add to game
			Player p = new Player(name, cash, game.maxCardsPerPlayer(), in, out);
			game.addPlayer(p);
			// Acknowledge player
			p.sendTo("WELCOME " + name);
			// Now exit this thread (streams to be used in game)
			break;
		    } else {
			out.println("ERROR expected BUYIN message: ");
			System.err.println("ERROR expected BUYIN message: ");
		    }
		} catch (NoSuchElementException ex) {
		    out.println("ERROR not enough tokens: " + msg);
		    System.err.println("ERROR not enough tokens: " + msg);
		}
	    } catch (EOFException ex) {
		System.err.println("EOF from client before BUYIN");
		return;
	    } catch (IOException ex) {
		System.err.println("server handshake io error: " + ex);
		return;
	    }
	}
    }
}
