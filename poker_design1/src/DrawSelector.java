/*
 * DrawSelector.java
 *
 * George Ferguson, ferguson@cs.rochester.edu, 31 Oct 1999
 * Time-stamp: <Sun Oct 31 13:13:22 EST 1999 ferguson>
 */

import java.awt.*;
import java.awt.event.*;

public class DrawSelector extends Dialog implements ActionListener {
    private TextField text;
    private DrawSelectorInterface callback_obj;
    public DrawSelector(Frame parent, DrawSelectorInterface cb) {
	super(parent, "Enter DRAW message", true);
	// Save instance vars
	callback_obj = cb;
	// Setup window
	setSize(300,135);
	// Type selector
	Panel p1 = new Panel();
	add(p1, "Center");
	text = new TextField("", 25);
	p1.add(text);
	// Button
	Panel p2 = new Panel();
	Button ok = new Button("Ok");
	ok.addActionListener(this);
	p2.add(ok);
	add(p2, "South");
    }
    public void actionPerformed(ActionEvent evt) {
	String str = text.getText();
	callback_obj.drawSelectorCB(str);
	setVisible(false);
    }
}
