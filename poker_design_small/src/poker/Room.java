/* @author Anton Kibalnik
A Room consists of tables, players, and a common chat and runs the server. The main method listens 
for connections and assigns a ClientListener thread to each connecting player.
*/

package poker;

import java.awt.Dimension;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class Room implements java.io.Serializable {
	
	private ArrayList<Table> tables;
	private HashMap<String, Player> players;
	private String chat;
	
	public Room(int t) {
		
		tables = new ArrayList<Table>(t);
		for(int i=0; i<t; i++) { tables.add(new Table()); }
		players = new HashMap<String, Player>();
		chat = "";
		
	}
	
	public ArrayList<Table> tables() { return tables; }
	public HashMap<String, Player> players() { return players; }
	public String chat() { return chat; }
	
	public void post(String message) { chat += message+"\n"; }

	public static void main(String[] args) {
		
		ServerSocket socket;
		try { socket = new ServerSocket(444); } catch (IOException e1) { e1.printStackTrace(); return; }
		JFrame kill = new JFrame("Server listening on port 444. Close to kill.");
		kill.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		kill.getContentPane().setPreferredSize(new Dimension(340, 40));
		kill.pack();
		kill.setVisible(true);
		Room room = new Room(4);
		Socket client;
		ArrayList<ObjectOutputStream> outs = new ArrayList<ObjectOutputStream>();
		
		while(true) {
			
			try {
				
				client = socket.accept();
				outs.add(new ObjectOutputStream(client.getOutputStream()));
				outs.get(outs.size()-1).writeObject(room); outs.get(outs.size()-1).flush(); outs.get(outs.size()-1).reset();
				(new ClientListener(client, outs.get(outs.size()-1), outs, room)).start();
				
			} catch (IOException e) { e.printStackTrace(); return; }
		
		}

	}

}