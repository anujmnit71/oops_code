/*
 * PokerClient.java
 *
 * George Ferguson, ferguson@cs.rochester.edu,  1 Oct 1998
 * Time-stamp: <Mon Oct 19 17:06:14 EDT 1998 ferguson>
 *
 * All this client does is hook up to the IPP server, echo lines received
 * from the server to stdout, prompt for replies from stdin, and send
 * the replies back to the server.
 *
 * Compile with: javac PokerClient.java
 * Run with: java PokerClient
 */

import java.io.*;
import java.net.*;

public class PokerClient extends Thread {
    // Members
    private BufferedReader in;
    private PrintWriter out;
    private static final String USAGE =
	"usage: java PokerClient [-server host[:port]]";
    // Constructor
    public PokerClient() {
    }
    // Methods
    public boolean connect(String host, int port) {
	try {
	    Socket sock = new Socket(host, port);
	    in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
	    out = new PrintWriter(sock.getOutputStream(), true);
	    return true;
	} catch (IOException ex) {
	    System.err.println("IO error: " + ex);
	    return false;
	}
    }
    public void run() {
	BufferedReader stdin =
	    new BufferedReader(new InputStreamReader(System.in));
	PrintWriter stdout = new PrintWriter(System.out, true);
	try {
	    // Do forever...
	    while (true) {
		// Read msg from server
		String msg = in.readLine();
		// Copy to stdout
		stdout.println(msg);
		// EOF?
		if (msg == null) {
		    // Then exit
		    System.exit(0);
		} else if (msg.indexOf("?") > 0 || msg.startsWith("IPP")) {
		    // Otherwise if msg requires response, prompt for it
		    stdout.print("reply: ");
		    stdout.flush();
		    // Read response from stdin
		    String reply = stdin.readLine();
		    // And send it back to the server
		    out.println(reply);
		}
	    }
	} catch (IOException ex) {
	    System.err.println("IO error: " + ex);
	}
    }
    // Main for sample client
    public static void main(String[] argv) {
	// Defaults
	String host = "localhost";
	int  port = 9898;
	// Process command-line
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
	    } else {
		System.err.println("unknown option: " + argv[i]);
		System.err.println(USAGE);
		System.exit(-1);
	    }
	}
	// Here we go
	PokerClient client = new PokerClient();
	if (client.connect(host, port)) {
	    client.run();
	}
    }
}
