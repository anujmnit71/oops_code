/*
 * PokerFrame.java
 *
 * George Ferguson, ferguson@cs.rochester.edu, 24 Sep 1998
 * Time-stamp: <Sun Oct 31 11:50:11 EST 1999 ferguson>
 */

import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.Vector;

public class PokerFrame extends Frame {
    // Members
    protected PlayerVector players;
    protected Pot pot;
    protected String status;
    protected static int TABLE_PANEL_WIDTH = 950;
    protected static int TABLE_PANEL_HEIGHT = 800;
    protected static String displaySize;
    private PokerTablePanel panel;
    private TextArea msgTextArea;
    private TextArea winnersTextArea;
    private Panel buttonPanel;
    private Vector commonCardDisplays;
    // Constructor
    public PokerFrame(String dispSize, PlayerVector ps, Pot p) {
	// Initialize display size
	displaySize = dispSize;
	if (displaySize.equalsIgnoreCase("small")) {
	    TABLE_PANEL_WIDTH = 700;
	    TABLE_PANEL_HEIGHT = 700;
	}
	CardDisplay.initialize(displaySize);
	PlayerDisplay.initialize(displaySize);
	// Save instance vars
	players = ps;
	pot = p;
	commonCardDisplays = new Vector();
	// Setup window
	setSize(TABLE_PANEL_WIDTH, TABLE_PANEL_HEIGHT+150);
	Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
	setLocation((d.width-TABLE_PANEL_WIDTH)/2,
		    (d.height-(TABLE_PANEL_HEIGHT+150))/2);
	addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) 
		{System.exit(0);}
	});
	// Table and players
	panel = new PokerTablePanel(this);
	panel.setSize(TABLE_PANEL_HEIGHT, TABLE_PANEL_WIDTH);
	add(panel, "North");
	// System messages
	msgTextArea = new TextArea();
	msgTextArea.setSize(10, 40);
        msgTextArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        msgTextArea.setEditable(false);
	add(msgTextArea, "West");
	// Game log (winners)
	winnersTextArea = new TextArea();
	winnersTextArea.setSize(10, 30);
        winnersTextArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        winnersTextArea.setEditable(false);
	add(winnersTextArea, "Center");
	// Control buttons
	buttonPanel = new Panel();
	buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 1));
	panel.add(buttonPanel);
	// Make frame visible
	show();
    }
    // Methods
    public void addButton(Button b) {
	buttonPanel.add(b);
	buttonPanel.doLayout();
	panel.doLayout();
	panel.validate();
    }
    public void addPlayer(Player p) {
	//System.err.println("PokerFrame.addPlayer: " + p.getName());
	panel.add(p.getDisplay());
	panel.doLayout();
    }
    public void removePlayer(Player p) {
	//System.err.println("PokerFrame.removePlayer: " + p.getName());
	panel.remove(p.getDisplay());
	panel.doLayout();
    }
    public void addCommonCard(Card c) {
	CardDisplay cd = new CardDisplay(c);
	cd.setBackground(Color.green.darker());
	panel.add(cd);
	panel.doLayout();
	commonCardDisplays.addElement(cd);
    }
    public void clearCommonCards() {
	for (int i=0; i < commonCardDisplays.size(); i++) {
	    panel.remove((CardDisplay)(commonCardDisplays.elementAt(i)));
	}
	//commonCardDisplays.removeAllElements();
    }
    public void setStatus(String msg) {
	status = msg;
	update();
    }
    public void displayMsg(String msg) {
	//System.err.println("PokerFrame.displayMsg: " + msg);
	msgTextArea.append(msg + "\n");
	if (msg.startsWith(">ALL WINNER")) {
	    winnersTextArea.append(msg.substring(12) + "\n");
	}
    }
    public void update() {
	//System.err.println("PokerFrame.repaint");
	panel.repaint();
    }
}

class PokerTablePanel extends Panel {
    private PokerFrame frame;
    private Image dealerImage = null;
    public PokerTablePanel(PokerFrame f) {
	frame = f;
	setLayout(new PokerLayoutManager());
	if (frame.displaySize.equalsIgnoreCase("small")) {
	    setFont(new Font("SansSerif", Font.BOLD, 12));
	} else {
	    setFont(new Font("SansSerif", Font.BOLD, 24));
	}
    }
    public void paint(Graphics g) {
	//System.err.println("PokerTablePanel.paint");
	int centerx = PokerFrame.TABLE_PANEL_WIDTH/2;
	int centery = PokerFrame.TABLE_PANEL_HEIGHT/2;
	// Table
	int radius = PokerFrame.TABLE_PANEL_WIDTH/4;
	g.setColor(Color.green.darker());
	g.fillArc(centerx-radius, centery-radius, radius*2, radius*2, 0, 360);
	// Dealer
	if (dealerImage == null) {
	    MediaTracker tracker = new MediaTracker(this);
	    String filename = CardDisplay.IMAGE_DIR + "Dealer" + CardDisplay.IMAGE_EXT;
	    URL url = PokerTablePanel.class.getResource(filename);
	    dealerImage = Toolkit.getDefaultToolkit().getImage(url);
	    tracker.addImage(dealerImage, 0);
	    try {
		tracker.waitForAll();
	    } catch (InterruptedException e) {
	    }
	}
	int DEALER_WIDTH = dealerImage.getWidth(null);
	int DEALER_HEIGHT = dealerImage.getHeight(null);
	g.drawImage(dealerImage, centerx-DEALER_WIDTH/2, centery+(int)(radius*1.2), this);
	// Pot
	FontMetrics fm = g.getFontMetrics(g.getFont());
	String str = "$" + frame.pot.getCash();
	int strw = fm.stringWidth(str);
	g.setColor(Color.yellow);
	g.drawString(str, centerx-strw/2, centery - radius/2);
	//  Status
	str = frame.status;
	if (str != null && !str.equals("")) {
	    strw = fm.stringWidth(str);
	    int strh = fm.getHeight();
	    g.drawString(str, centerx-strw/2, centery + radius/2 - strh);
	}
	// Button
	PlayerVector players = frame.players;
	int nplayers = players.size();
	Player p = players.getPlayerOnButton();
	if (p != null) {
	    int arcrad = radius/20;
	    int pos = players.indexOf(p);
	    double angle = 2 * Math.PI * (pos+1)/(nplayers+1) + Math.PI/2;
	    int x = centerx + (int)(Math.cos(angle) * (radius+arcrad+3));
	    int y = centery + (int)(Math.sin(angle) * (radius+arcrad+3));
	    g.setColor(Color.green.darker());
	    g.fillArc(x-arcrad, y-arcrad, 2*arcrad, 2*arcrad, 0, 360);
	}
	// Current
	p = players.getCurrentPlayer();
	if (p != null) {
	    int pos = players.indexOf(p);
	    double angle = 2 * Math.PI * (pos+1)/(nplayers+1) + Math.PI/2;
	    int x1 = centerx + (int)(Math.cos(angle) * (radius));
	    int y1 = centery + (int)(Math.sin(angle) * (radius));
	    int x2 = centerx + (int)(Math.cos(angle) * (radius*1.17));
	    int y2 = centery + (int)(Math.sin(angle) * (radius*1.17));
	    g.drawLine(x1, y1, x2, y2);
	}
    }
}

//
// PokerLayoutManager
//
class PokerLayoutManager implements LayoutManager {
    public void addLayoutComponent(String s, Component comp) {
	// Nothing to do
    }
    public void removeLayoutComponent(Component comp) {
	// Nothing to do
    }
    public Dimension preferredLayoutSize(Container parent) {
	Insets insets = parent.getInsets();
	return new Dimension(PokerFrame.TABLE_PANEL_WIDTH + insets.left + insets.right,
			     PokerFrame.TABLE_PANEL_HEIGHT + insets.top + insets.bottom);
    }
    public Dimension minimumLayoutSize(Container parent) {
	Insets insets = parent.getInsets();
	return new Dimension(PokerFrame.TABLE_PANEL_WIDTH + insets.left + insets.right,
			     PokerFrame.TABLE_PANEL_HEIGHT + insets.top + insets.bottom);
    }
    public void layoutContainer(Container parent) {
	Insets insets = parent.getInsets();
	Dimension containerSize = parent.getSize();
	int containerWidth = containerSize.width - insets.left - insets.right;
	int containerHeight = containerSize.height - insets.top - insets.bottom;
	//int xcenter = insets.left + containerWidth/2;
	//int ycenter = insets.top + containerHeight/2;
	int xcenter = PokerFrame.TABLE_PANEL_WIDTH/2;
	int ycenter = PokerFrame.TABLE_PANEL_HEIGHT/2;
	int xradius = (int)(PokerFrame.TABLE_PANEL_WIDTH * 0.3);
	int yradius = (int)(PokerFrame.TABLE_PANEL_HEIGHT * 0.3);
	int n = parent.getComponentCount();
	//System.err.println("layoutContainer: " + n + " components, center=" + xcenter + "," + ycenter + ", radius=" + xradius + "," + yradius);
	int nplayers = 0;
	for (int i=0; i < n; i++) {
	    Component c = parent.getComponent(i);
	    if (c.isVisible() && c instanceof PlayerDisplay) {
		nplayers += 1;
	    }
	}
	int ncards = 0;
	int playernum = 0;
	for (int i=0; i < n; i++) {
	    Component c = parent.getComponent(i);
	    if (c.isVisible()) {
		if (c instanceof PlayerDisplay) {
		    Dimension d = c.getPreferredSize();
		    double angle = 2 * Math.PI * (playernum+1)/(nplayers+1) + Math.PI/2;
		    playernum += 1;
		    //int x = xcenter + (int)(Math.cos(angle) * (xradius+d.width/2));
		    //int y = ycenter + (int)(Math.sin(angle) * (yradius+d.height*2));
		    int x = xcenter + (int)(Math.cos(angle) * (xradius+d.width/2));
		    int y = ycenter + (int)(Math.sin(angle) * (yradius+d.height/2));
		    //System.err.println("layoutContainer: component " + i + " Player at " + x + "," + y);
		    c.setBounds(x - d.width/2, y - d.height/2, d.width, d.height);
		} else if (c instanceof CardDisplay) {
		    Dimension d = c.getPreferredSize();
		    int x = xcenter + (ncards-2) * d.width;
		    int y = ycenter;
		    //System.err.println("layoutContainer: component " + i + " Card at " + x + "," + y);
		    c.setBounds(x - d.width/2, y - d.height/2, d.width, d.height);
		    ncards += 1;
		} else if (c instanceof Panel) {
		    Dimension d = c.getPreferredSize();
		    //System.err.println("layoutContainer: component " + i + " Panel " + d.width + "x" + d.height);
		    c.setBounds(xcenter - d.width/2, containerHeight - d.height, d.width, d.height);
		    
		}
	    }
	}
    }
}
