/*
 * CardDisplay.java
 *
 * George Ferguson, ferguson@cs.rochester.edu, 24 Sep 1998
 * Time-stamp: <Mon Oct 12 12:44:21 EDT 1998 ferguson>
 */

import java.awt.*;
import java.net.*;
import java.util.Hashtable;

public class CardDisplay extends Canvas {
    // Members
    private Card card;
    public static int CARD_WIDTH = 53;
    public static int CARD_HEIGHT = 74;
    public static String IMAGE_DIR = "images/";
    public static String IMAGE_EXT = ".gif";
    private static boolean initialized = false;
    private static Hashtable images = new Hashtable(52);
    private static Image cardBackImage;
    private static Image unknownCardImage;
    // "Class initialization"
    public static void initialize(String size) {
	if (size.equalsIgnoreCase("small")) {
	    CARD_WIDTH = 27;
	    CARD_HEIGHT = 37;
	    IMAGE_DIR = "images.small/";
	}
    }
    // Constructor
    public CardDisplay(Card c) {
	//System.err.println("CardDisplay.new: " + c);
	card = c;
	setSize(CARD_WIDTH, CARD_HEIGHT);
	if (!initialized) {
	    readImages();
	    initialized = true;
	}
    }
    // Methods
    private void readImages() {
	MediaTracker tracker = new MediaTracker(this);
	int id = 0;
	for (int i = 0; i < Card.ranks.length; i++) {
	    for (int j = 0; j < Card.suits.length; j++) {
		String name = Card.ranks[i] + Card.suits[j];
		String filename = IMAGE_DIR + name + IMAGE_EXT;
		URL url = CardDisplay.class.getResource(filename);
		//System.err.println("reading image: " + url);
		Image img = Toolkit.getDefaultToolkit().getImage(url);
		images.put(name, img);
		tracker.addImage(img, id++);
	    }
	}
	String filename = IMAGE_DIR + "Back" + IMAGE_EXT;
	URL url = CardDisplay.class.getResource(filename);
	cardBackImage = Toolkit.getDefaultToolkit().getImage(url);
	tracker.addImage(cardBackImage, id++);
	filename = IMAGE_DIR + "Unknown" + IMAGE_EXT;
	url = CardDisplay.class.getResource(filename);
	unknownCardImage = Toolkit.getDefaultToolkit().getImage(url);
	tracker.addImage(unknownCardImage, id++);
	//System.err.println("waiting for image reading to finish");
	try {
	    tracker.waitForAll();
	} catch (InterruptedException e) {
	}
	//System.err.println("image reading complete");
    }
    public void paint(Graphics g) {
	//System.err.println("CardDisplay.paint: " + card);
	Image image;
	if (card.isFaceUp()) {
	    image = (Image)images.get(card.toString());
	    if (image == null) {
		image = unknownCardImage;
	    }
	} else {
	    image = cardBackImage;
	}
	g.drawImage(image, 0, 0, this);
    }
}
