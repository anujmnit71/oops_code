/*
 * PokerPlayer.java
 *
 * George Ferguson, ferguson@cs.rochester.edu, 24 Sep 1998
 * Time-stamp: <Fri Oct  9 15:21:35 EDT 1998 ferguson>
 *
 * Based on PokerClient code (in fact, copied with "player" for "client")
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

abstract public class PokerPlayer extends Thread {
	// Members
	private String serverHost;
	private int serverPort;
	private BufferedReader in;
	private PrintWriter out;
	private static final String USAGE =
			"usage: ippplayer [-server host[:port]] [-size large|small]\n                 [-mode default|random|agressive|smart] name [buyin]";
	// Methods
	abstract public void receive(String msg);
	// Main for player
	public static void main(String[] argv) {
		String host = "localhost";
		int port = 9898;
		String name = "unnamed";
		int buyin = 1000;
		String displaySize = "large";
		String mode = "default";
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
			} else if (argv[i].equalsIgnoreCase("-mode")) {
				mode = argv[++i];
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
		// Could use argv to decide what game to play
		PokerPlayer player = new HoldemPokerPlayer(displaySize, name, buyin, mode);
		player.serverHost = host;
		player.serverPort = port;
		// Need to lower priority (from default 5) for unknown reasons,
		// otherwise display does not refresh properly.
		player.setPriority(3);
		player.start();
	}
	@Override
	public void run() {
		try {
			// Connect to server
			Socket sock = new Socket(serverHost, serverPort);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);
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
	public void send(String msg) {
		out.println(msg);
	}
}
