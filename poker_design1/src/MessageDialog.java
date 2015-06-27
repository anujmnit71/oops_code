/*
 * MessageDialog.java
 *
 * George Ferguson, ferguson@cs.rochester.edu, 26 Sep 1998
 * Time-stamp: <Sat Sep 26 16:16:28 EDT 1998 ferguson>
 */

import java.awt.*;
import java.awt.event.*;

public class MessageDialog extends Dialog implements ActionListener {
    public MessageDialog(Frame parent, String title, String msg) {
	super(parent, title, true);
	// Setup window
	Font f = new Font("SansSerif", Font.PLAIN, 14);
	setSize(300, 135);
	setResizable(true);
	// Type selector
	Label label = new Label(msg);
	add(label, "Center");
	// Button
	Panel p = new Panel();
	Button ok = new Button("Ok");
	ok.addActionListener(this);
	p.add(ok);
	add(p, "South");
    }
    public void actionPerformed(ActionEvent evt) {
	setVisible(false);
	dispose();
    }
}
