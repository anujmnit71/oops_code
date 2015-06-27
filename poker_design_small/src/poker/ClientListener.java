/* @author Anton Kibalnik
A ClientListener listens for commands from the RoomView class and executes them on behalf 
the player, sending back the state of the game.
*/
package poker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ClientListener extends Thread {
	
	private BufferedReader in;
	private ObjectOutputStream out;
	private ArrayList<ObjectOutputStream> outs;
	private String command;
	private Room room;
	private Player player;
	
	public ClientListener(Socket client, ObjectOutputStream o, ArrayList<ObjectOutputStream> streams, Room r) {
		
		try { in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		} catch (IOException e) { e.printStackTrace(); return; }
		out = o;
		outs = streams;
		command = "";
		room = r;
		player = null;
		
	}
	
	public void run() {
					
		while (true) {
			
			try { command = in.readLine(); } catch (IOException e) { e.printStackTrace(); break; }
			
			if(command.startsWith("playAs")) { room.players().put(command.substring(7), player = new Player(command.substring(7))); }
			else if(command.startsWith("join")) player.join(room.tables().get(Integer.parseInt(command.substring(5,6))));
			else if(command.startsWith("leaveTable")) player.leaveTable();
			else if(command.startsWith("sitOut")) player.sitOut(command.substring(7).equals("true"));
			else if(command.startsWith("fold")) player.fold();
			else if(command.startsWith("call")) player.call();
			else if(command.startsWith("raiseTo")) player.raiseTo(Integer.parseInt(command.substring(8)));
			else if(command.startsWith("say")) room.post(player.name()+": "+command.substring(4));
			else if(command.startsWith("disconnect")) {
				
				if(player!=null) {
					if(player.table()!=null) player.leaveTable();
					room.players().remove(player.name());
				}
				try { outs.remove(out);	in.close();	out.close(); } catch (IOException e) { e.printStackTrace(); break; }
				
			} else continue;
			
			for(ObjectOutputStream out : outs) try { out.writeObject(room); out.flush(); out.reset(); } catch (IOException e) { e.printStackTrace(); break; }
			
			if(player.table()!=null && player.table().ranked()) {
				try { Thread.sleep(8000); } catch (InterruptedException e) { e.printStackTrace(); break; }
				player.table().next();
				for(ObjectOutputStream out : outs) try { out.writeObject(room); out.flush(); out.reset(); } catch (IOException e) { e.printStackTrace(); break; }
			}
			
			if(player.table()!=null && player.table().awarded()) {
				try { Thread.sleep(3000); } catch (InterruptedException e) { e.printStackTrace(); break; }
				player.table().next();
				for(ObjectOutputStream out : outs) try { out.writeObject(room); out.flush(); out.reset(); } catch (IOException e) { e.printStackTrace(); break; }
			}
			
			if(command.startsWith("disconnect")) break;
			
		}

	}

}
