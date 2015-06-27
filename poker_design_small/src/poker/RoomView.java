/* @author Anton Kibalnik
The RoomView is the graphical interface for the game which also handles the networking for the client.
*/
package poker;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class RoomView extends javax.swing.JFrame {
	
	private Socket socket;
	public ObjectInputStream in;
	private PrintWriter out;
	private ImageIcon back;
	private String[] model;
	private Room room;
	private Player me;
	
	public RoomView() {
		
		socket = null;
		in = null;
		out = null;
		back = new ImageIcon(getClass().getResource("/cards_gif/back.gif"));
		model = new String[]{""};
		room = null;
		me = null;
		initComponents();
		
	}

	private void connectActionPerformed() {
    	
		try {
			
			socket = new Socket("localhost", Integer.parseInt(port.getText()));
			in = new ObjectInputStream(socket.getInputStream());
			out = new PrintWriter(socket.getOutputStream(), true);
			
		} catch (IOException e) { error(); return; }
		
		host.setEnabled(false);
		port.setEnabled(false);
		connect.setEnabled(false);
		playAs.setEnabled(true);
    	name.setEnabled(true);
    	tables.setEnabled(true);
    	chat.setText("Connected. Game on!");
    	
    }
	
    private void playAsActionPerformed() {
    	
    	if(room.players().containsKey(name.getText())) { chat.setText("Player name '"+name.getText()+"' is already taken."); return; }
    	playAs.setEnabled(false);
    	name.setEnabled(false);
    	out.println("playAs "+name.getText());
    	
    }
    private void joinActionPerformed() { out.println("join "+tables.getSelectedIndex()); }
    private void leaveTableActionPerformed() { if(me.turn()) players.get(me.table().seats().indexOf(me)).get("name").setBackground(pot.getBackground()); out.println("leaveTable"); }
    private void sitOutActionPerformed() { out.println("sitOut "+sitOut.isSelected()); }
    private void foldActionPerformed() { out.println("fold"); }
    private void callActionPerformed() { out.println("call"); }
    private void raiseToActionPerformed() {
    	if(Integer.parseInt(raise.getText())<me.table().toCall()+2*me.table().small() && Integer.parseInt(raise.getText())<me.chips()) raise.setText(Integer.toString(me.table().toCall()+2*me.table().small()>me.chips() ? me.chips() : me.table().toCall()+2*me.table().small()));
    	else out.println("raiseTo "+raise.getText());
    }
    private void sayActionPerformed() { out.println("say "+message.getText()); message.setText(""); }
    
	private String status(Player p) {
		
		if(p.table()==null) return " ";
		else if(!p.table().inHand().contains(p)) {
			
			if(p.table().dealTo().contains(p) && p.table().inHand().size()>0) return "Folded";
			else if(!p.table().in().contains(p)) return "Sitting Out";
			else return " ";
			
		} else if(p.chips()==0) return "All In";
		else if(p.table().hasToCall().contains(this) && !p.action().startsWith("Posted")) return " ";
		else return p.action();
		
	}
    
    public void error() { chat.setText("Server error."); }
    
	public void update(Room r) {
		
		if(room==null) model = new String[r.tables().size()];
		room = r;
		me = name.isEnabled() ? null : r.players().get(name.getText());
		for(int i=0; i<model.length; i++) model[i] = "Table "+i+" ("+room.tables().get(i).size()+")";
		tables.setModel(new javax.swing.DefaultComboBoxModel(model));
		if(me==null) return;
		
		join.setEnabled(me.table()==null);
		leaveTable.setEnabled(me.table()!=null);
		sitOut.setEnabled(me.table()!=null);
		say.setEnabled(me.table()!=null);
		raise.setEnabled(me.table()!=null);
		fold.setEnabled(me.turn());
		call.setEnabled(me.turn());
		raiseTo.setEnabled(me.turn() && me.chips()>me.table().toCall());
		chat.setText(room.chat()); //room vs table chat
		
		if(me.table()==null) {
			
			for(int i=0; i<5; i++) board.get(i).setIcon(null);
			pot.setText(" ");
			potNow.setText(" ");
			inPot.setText(" ");
			toCall.setText(" ");
			raise.setText(" ");
			
			for(int i=0; i<9; i++) {
				
				players.get(i).get("name").setText(" ");
	        	players.get(i).get("chips").setText(" ");
	        	players.get(i).get("toPot").setText(" ");
	        	players.get(i).get("card0").setIcon(null);
	        	players.get(i).get("card1").setIcon(null);
	        	players.get(i).get("status").setText(" ");
	        	
			}
			
			return;
			
		}
		
		tables.setSelectedIndex(room.tables().indexOf(me.table()));
		pot.setText(Integer.toString(me.table().pot()));
		potNow.setText(Integer.toString(me.table().pot()+me.table().toPot()));
		for(int i=0; i<me.table().board().size(); i++) board.get(i).setIcon(new ImageIcon(getClass().getResource("/cards_gif/"+me.table().board().get(i).image())));
		for(int i=me.table().board().size(); i<5; i++) board.get(i).setIcon(null);
		
		sitOut.setSelected(!me.table().in().contains(me));
		sitOut.setEnabled(me.chips()>0);
		inPot.setText(Integer.toString(me.inPot()));
		toCall.setText(Integer.toString(me.table().toCall()-me.toPot()));
		raise.setText(Integer.toString(me.table().toCall()+2*me.table().small()>me.chips() ? me.chips() : me.table().toCall()+2*me.table().small()));
		
		for(int i=0; i<9; i++) {
			
			if(me.table().seats().get(i)==null) {
				
				players.get(i).get("name").setText("empty");
	        	players.get(i).get("chips").setText(" ");
	        	players.get(i).get("toPot").setText(" ");
	        	players.get(i).get("card0").setIcon(null);
	        	players.get(i).get("card1").setIcon(null);
	        	players.get(i).get("status").setText(" ");
				
			} else {
				
				players.get(i).get("name").setText(me.table().seats().get(i).name());
	        	players.get(i).get("chips").setText(Integer.toString(me.table().seats().get(i).chips()));
	        	players.get(i).get("toPot").setText(Integer.toString(me.table().seats().get(i).toPot()));
	        	
	        	if(me.table().inHand().contains(me.table().seats().get(i))) { //contains! and status computation at gui! make function
	        		
	            	if(me.table().ranked() && me.table().mustShow().contains(me.table().seats().get(i))) {
	            		
	            		players.get(i).get("card0").setIcon(new ImageIcon(getClass().getResource("/cards_gif/"+me.table().seats().get(i).cards().get(0).image())));
	                	players.get(i).get("card1").setIcon(new ImageIcon(getClass().getResource("/cards_gif/"+me.table().seats().get(i).cards().get(1).image())));
	            		players.get(i).get("status").setText(me.table().seats().get(i).hand().type().toString());
	            		if(me.table().winners().contains(me.table().seats().get(i))) players.get(i).get("toPot").setText("wins "+(me.table().seats().get(i).chips()==0 && me.table().seats().get(i).sidePot()!=me.table().pot() ? "side pot" : "main pot"));

	            	} else if(i!=me.table().seats().indexOf(me)) {
	            		
	            		players.get(i).get("card0").setIcon(back);
		            	players.get(i).get("card1").setIcon(back);
		            	players.get(i).get("status").setText(me.table().ranked() ? "Mucks" : status(me.table().seats().get(i)));
	            			
	            			
            		} else {
            			
            			players.get(i).get("card0").setIcon(new ImageIcon(getClass().getResource("/cards_gif/"+me.cards().get(0).image())));
            			players.get(i).get("card1").setIcon(new ImageIcon(getClass().getResource("/cards_gif/"+me.cards().get(1).image())));
            			players.get(i).get("status").setText(me.table().board().size()>2 ? me.hand().type().toString() : status(me));
            			
            		}
	        	
	        	} else {
	        		
	        		players.get(i).get("card0").setIcon(null);
	            	players.get(i).get("card1").setIcon(null);
	            	players.get(i).get("status").setText(status(me.table().seats().get(i)));
	            	
	        	}
	        	
	        	if(me.table().seats().get(i).turn()) players.get(i).get("name").setBackground(Color.white);
	        	else players.get(i).get("name").setBackground(pot.getBackground());
				
			}
			
		}
		
		players.get(me.table().dealer()).get("name").setText(players.get(me.table().dealer()).get("name").getText()+" (D)");
	
	}
	
	
	//main + GUI components ------------------------
	
	public static void main(String[] args) {
		
		RoomView view = new RoomView();
		while(view.connect.isEnabled())	try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); return; }
		
		while(true) {
			
			try { view.update((Room) view.in.readObject()); }
			catch (IOException e) { view.error(); break; }
			catch (ClassNotFoundException e) { view.error(); break; }
			
		}

	}
	
	private JComboBox tables;
	private ArrayList<JPanel> panels;
	private ArrayList<HashMap<String, JLabel>> players;
	private ArrayList<JLabel> board;
	private JLabel hostTag, portTag, pot, potNow, inPot, toCall, potNowTag;
	public JButton connect;
    private JButton playAs, join, leaveTable, fold, call, raiseTo, say;
    private JCheckBox sitOut;
    private JTextField host, port, name, raise, message;
	private JTextArea chat;
    private JScrollPane chatPane;

    private void initComponents() {
    	
    	setTitle("Holdem Client");
        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setPreferredSize(new Dimension(1024,640));
        getContentPane().setLayout(new AbsoluteLayout());
        
        addWindowListener( new WindowAdapter() { 
            @Override
            public void windowClosing(WindowEvent e) {
            	if(me!=null && me.turn()) players.get(me.table().seats().indexOf(me)).get("name").setBackground(pot.getBackground());
            	if(out!=null) { out.println("disconnect"); out.close(); }         	
            }
        });
    	
    	tables = new JComboBox();
    	board = new ArrayList<JLabel>(5);
        for(int i=0; i<5; i++) board.add(new JLabel());
        hostTag = new JLabel();
        portTag = new JLabel();
    	pot = new JLabel();
        potNow = new JLabel();
        inPot = new JLabel();
        toCall = new JLabel();
        potNowTag = new JLabel();
        connect = new JButton();
        playAs = new JButton();
        join = new JButton();
        leaveTable = new JButton();
        fold = new JButton();
        call = new JButton();
        raiseTo = new JButton();
        say = new JButton();
        sitOut = new JCheckBox();
        host = new JTextField();
        port = new JTextField();
        name = new JTextField();
        raise = new JTextField();
        message = new JTextField();
        chat = new JTextArea();
        chatPane = new JScrollPane();

        panels = new ArrayList<JPanel>(9);
        players = new ArrayList<HashMap<String, JLabel>>(9);
        for(int i=0; i<9; i++) {
        	panels.add(new JPanel());
        	players.add(new HashMap<String, JLabel>(6));
        	players.get(i).put("name", new JLabel());
        	players.get(i).get("name").setOpaque(true);
        	players.get(i).put("chips", new JLabel());
        	players.get(i).put("toPot", new JLabel());
        	players.get(i).put("card0", new JLabel());
        	players.get(i).put("card1", new JLabel());
        	players.get(i).put("status", new JLabel());
        }
        
        hostTag.setText("Host:");
        getContentPane().add(hostTag, new AbsoluteConstraints(10, 10, -1, -1));
        host.setText("localhost");
        getContentPane().add(host, new AbsoluteConstraints(45, 10, 97, -1));
        portTag.setText("Port:");
        getContentPane().add(portTag, new AbsoluteConstraints(10, 35, -1, -1));
        port.setText("444");
        getContentPane().add(port, new AbsoluteConstraints(45, 35, 45, -1));
        connect.setText("Connect");
        connect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) { connectActionPerformed(); }
        });
        getContentPane().add(connect, new AbsoluteConstraints(10, 60, 130, 30));
        
        playAs.setText("Play as");
        playAs.setEnabled(false);
        playAs.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) { playAsActionPerformed(); }
        });
        getContentPane().add(playAs, new AbsoluteConstraints(845, 10, -1, -1));
        name.setText("Player");
        name.setEnabled(false);
        getContentPane().add(name, new AbsoluteConstraints(925, 13, 90, -1));
        join.setText("Join");
        join.setEnabled(false);
        join.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) { joinActionPerformed(); }
        });
        getContentPane().add(join, new AbsoluteConstraints(845, 45, 70, -1));
        tables.setEnabled(false);
        getContentPane().add(tables, new AbsoluteConstraints(924, 45, 90, -1));
        leaveTable.setText("Leave Table");
        leaveTable.setEnabled(false);
        leaveTable.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) { leaveTableActionPerformed(); }
        });
        getContentPane().add(leaveTable, new AbsoluteConstraints(845, 80, 170, -1));
        
        for(JLabel l : board) {
        	l.setText(" ");
            getContentPane().add(l, new AbsoluteConstraints(323+77*board.indexOf(l), 211, 71, 96));
        }
        pot.setHorizontalAlignment(SwingConstants.CENTER);
        pot.setText(" ");
        getContentPane().add(pot, new AbsoluteConstraints(450, 330, 90, -1));
        
        potNowTag.setText("Pot:");
        getContentPane().add(potNowTag, new AbsoluteConstraints(430, 570, -1, -1));
        potNow.setText(" ");
        getContentPane().add(potNow, new AbsoluteConstraints(460, 570, 70, -1));

        inPot.setHorizontalAlignment(SwingConstants.CENTER);
        inPot.setText(" ");
        getContentPane().add(inPot, new AbsoluteConstraints(600, 611, 125, -1));
        fold.setText("Fold");
        fold.setEnabled(false);
        fold.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) { foldActionPerformed(); }
        });
        getContentPane().add(fold, new AbsoluteConstraints(603, 567, 125, 40));
        
        call.setText("Check/Call");
        call.setEnabled(false);
        call.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) { callActionPerformed(); }
        });
        getContentPane().add(call, new AbsoluteConstraints(746, 567, 125, 40));
        toCall.setHorizontalAlignment(SwingConstants.CENTER);
        toCall.setText(" ");
        getContentPane().add(toCall, new AbsoluteConstraints(746, 611, 125, -1));
        raiseTo.setText("Raise to");
        raiseTo.setEnabled(false);
        raiseTo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) { raiseToActionPerformed(); }
        });
        getContentPane().add(raiseTo, new AbsoluteConstraints(889, 567, 125, 40));
        raise.setText(" ");
        raise.setEnabled(false);
        getContentPane().add(raise, new AbsoluteConstraints(889, 611, 125, -1));

        sitOut.setText("Sit out next hand");
        sitOut.setEnabled(false);
        sitOut.setSelected(true);
        sitOut.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) { sitOutActionPerformed(); }
        });
        getContentPane().add(sitOut, new AbsoluteConstraints(10, 447, 120, -1));
        chat.setColumns(20);
        chat.setRows(5);
        chatPane.setViewportView(chat);
        getContentPane().add(chatPane, new AbsoluteConstraints(10, 472, 240, 129));
        getContentPane().add(message, new AbsoluteConstraints(10, 608, 176, -1));
        say.setText("Say");
        say.setEnabled(false);
        say.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) { sayActionPerformed(); }
        });
        getContentPane().add(say, new AbsoluteConstraints(192, 607, 57, 22));

        int[][] pos = new int[9][2];
        pos[0][0] = 700; pos[0][1] = 360;
        pos[1][0] = 500; pos[1][1] = 370;
        pos[2][0] = 291; pos[2][1] = 366;
        pos[3][0] = 151; pos[3][1] = 274;
        pos[4][0] = 151; pos[4][1] = 70;
        pos[5][0] = 323; pos[5][1] = 18;
        pos[6][0] = 512; pos[6][1] = 18;
        pos[7][0] = 695; pos[7][1] = 18;
        pos[8][0] = 800; pos[8][1] = 170;
        
        GroupLayout layout;
        
        for(int i=0; i<9; i++) {
        	
        	players.get(i).get("name").setHorizontalAlignment(SwingConstants.CENTER);
        	players.get(i).get("name").setText(" ");
        	players.get(i).get("chips").setHorizontalAlignment(SwingConstants.CENTER);
        	players.get(i).get("chips").setText(" ");
        	players.get(i).get("toPot").setHorizontalAlignment(SwingConstants.CENTER);
        	players.get(i).get("toPot").setText(" ");
        	players.get(i).get("status").setHorizontalAlignment(SwingConstants.CENTER);
        	players.get(i).get("status").setText(" ");

            layout = new GroupLayout(panels.get(i));
            panels.get(i).setLayout(layout);
            layout.setHorizontalGroup(
            		layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(8, 8, 8)
                    .addComponent(players.get(i).get("card0"), GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
                    .addComponent(players.get(i).get("card1"), GroupLayout.PREFERRED_SIZE, 71, GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE))
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addComponent(players.get(i).get("toPot"), GroupLayout.PREFERRED_SIZE, 113, GroupLayout.PREFERRED_SIZE)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                            .addComponent(players.get(i).get("status"), GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(players.get(i).get("chips"), GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(players.get(i).get("name"), GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)))
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            layout.setVerticalGroup(
            		layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addComponent(players.get(i).get("name"))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(players.get(i).get("chips"))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addComponent(players.get(i).get("card0"), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(players.get(i).get("card1"), GroupLayout.PREFERRED_SIZE, 96, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(players.get(i).get("status"))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(players.get(i).get("toPot")))
            );

            getContentPane().add(panels.get(i), new AbsoluteConstraints(pos[i][0], pos[i][1], 113, -1));
        	
        }
        
        pack();
        setVisible(true);
        
    }
	
}
