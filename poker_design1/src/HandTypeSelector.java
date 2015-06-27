/*
 * HandTypeSelector.java
 *
 * George Ferguson, ferguson@cs.rochester.edu, 26 Sep 1998
 * Time-stamp: <Thu Jan 19 14:28:25 EST 2006 ferguson>
 */

import java.awt.*;
import java.awt.event.*;

public class HandTypeSelector extends Dialog
				implements ActionListener, ItemListener {
    private Choice type;
    private RankSelector ranks[];
    private HandTypeSelectorInterface callback_obj;
    public HandTypeSelector(Frame parent, HandTypeSelectorInterface cb) {
	super(parent, "Select Hand Type", true);
	// Save instance vars
	callback_obj = cb;
	// Setup window
	setSize(300,135);
	// Type selector
	Panel p1 = new Panel();
	add(p1, "Center");
	type = new Choice();
	type.addItemListener(this);
	type.add("STRAIGHTFLUSH");
	type.add("FOUROFAKIND");
	type.add("FULLHOUSE");
	type.add("FLUSH");
	type.add("STRAIGHT");
	type.add("THREEOFAKIND");
	type.add("TWOPAIR");
	type.add("ONEPAIR");
	type.add("HIGHCARD");
	p1.add(type);
	// Rank selectors to go with type
	ranks = new RankSelector[5];
	for (int i=0; i < 5; i++) {
	    ranks[i] = new RankSelector(1);
	    ranks[i].setEnabled(i == 0);
	    p1.add(ranks[i]);
	}
	// Button
	Panel p2 = new Panel();
	Button ok = new Button("Ok");
	ok.addActionListener(this);
	p2.add(ok);
	add(p2, "South");
    }
    public void itemStateChanged(ItemEvent evt) {
	if (evt.getStateChange() == ItemEvent.SELECTED) {
	    String typestr = (String)evt.getItem();
	    //System.err.println("select: " + typestr);
	    if (typestr.equals("STRAIGHTFLUSH") ||
		typestr.equals("STRAIGHT")) {
		for (int i=1; i < 5; i++) {
		    ranks[i].setEnabled((i == 0));
		}
	    } else if (typestr.equals("FOUROFAKIND") ||
		       typestr.equals("FULLHOUSE")) {
		for (int i=1; i < 5; i++) {
		    ranks[i].setEnabled((i <= 1));
		}
	    } else if (typestr.equals("THREEOFAKIND") ||
		       typestr.equals("TWOPAIR")) {
		for (int i=1; i < 5; i++) {
		    ranks[i].setEnabled((i <= 2));
		}
	    } else if (typestr.equals("ONEPAIR")) {
		for (int i=1; i < 5; i++) {
		    ranks[i].setEnabled((i <= 3));
		}
	    } else if (typestr.equals("FLUSH") ||
		       typestr.equals("HIGHCARD")) {
		for (int i=1; i < 5; i++) {
		    ranks[i].setEnabled(true);
		}
	    }
	}
    }
    public void actionPerformed(ActionEvent evt) {
	String str = type.getSelectedItem();
	//System.err.println("OK: " + typestr);
	for (int i=0; i < 5; i++) {
	    if (ranks[i].isEnabled()) {
		str = str + " " + Card.rankToString(ranks[i].getRank());
	    }
	}
	try {
	    callback_obj.handTypeSelectorCB(HandType.fromString(str));
	} catch (HandTypeFormatException ex) {
	    System.err.println("yow! bogus HandType from selector: " + str);
	}
	setVisible(false);
    }
}

class RankSelector extends Panel implements ActionListener {
    private int rank = 1;
    private Button up;
    private Button down;
    private Label label;
    public RankSelector(int r) {
	rank = r;
	setLayout(new GridLayout(3, 1));
	up = new Button("+");
	add(up);
	up.addActionListener(this);
	label = new Label(Card.rankToString(rank), Label.CENTER);
	add(label);
	down = new Button("-");
	down.addActionListener(this);
	add(down);
	setButtonsEnabled();
    }
    public void actionPerformed(ActionEvent evt) {
	String arg = evt.getActionCommand();
	if (arg.equals("+")) {
	    if (rank == 13) {
		// Ace is high
		rank = 1;
	    } else {
		rank += 1;
	    }
	} else if (arg.equals("-")) {
	    if (rank == 1) {
		// Ace is high
		rank = 13;
	    } else {
		rank -= 1;
	    }
	}
	label.setText(Card.rankToString(rank));
	setButtonsEnabled();
    }
    private void setButtonsEnabled() {
	// Ace is high!
	up.setEnabled(!(rank == 1));
	down.setEnabled(!(rank == 2));
    }
    public int getRank() {
	return rank;
    }
}
