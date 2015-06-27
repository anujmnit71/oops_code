/*
 * HandType.java
 *
 * George Ferguson, ferguson@cs.rochester.edu,  8 Sep 1998
 * Time-stamp: <Thu Jan 19 14:28:55 EST 2006 ferguson>
 */

abstract public class HandType {
    // Comparison: this will be overridden by subclasses
    abstract public int compare(StraightFlush other);
    abstract public int compare(FourOfAKind other);
    abstract public int compare(FullHouse other);
    abstract public int compare(Flush other);
    abstract public int compare(Straight other);
    abstract public int compare(ThreeOfAKind other);
    abstract public int compare(TwoPair other);
    abstract public int compare(OnePair other);
    abstract public int compare(HighCard other);
    // Fucking Java doesn't have true polymorphism...
    public int compare(HandType other) {
	if (other instanceof StraightFlush) {
	    return compare((StraightFlush)other);
	} else if (other instanceof FourOfAKind) {
	    return compare((FourOfAKind)other);
	} else if (other instanceof FullHouse) {
	    return compare((FullHouse)other);
	} else if (other instanceof Flush) {
	    return compare((Flush)other);
	} else if (other instanceof Straight) {
	    return compare((Straight)other);
	} else if (other instanceof ThreeOfAKind) {
	    return compare((ThreeOfAKind)other);
	} else if (other instanceof TwoPair) {
	    return compare((TwoPair)other);
	} else if (other instanceof OnePair) {
	    return compare((OnePair)other);
	} else if (other instanceof HighCard) {
	    return compare((HighCard)other);
	} else {
	    // Should throw something here...
	    System.err.println("YOW! HandType.compare: " + this + " & " + other);
	    return 0;
	}
    }
    //abstract public int compare(HandType other);
    // Testing: this will be overridden by subclasses
    abstract public boolean test(CardVector cards);
    // Printing: this will be overridden by subclasses
    abstract public String toString();
    // Pseudo-constructor from String used in parsing messages
    public static HandType fromString(String s) throws HandTypeFormatException {
	s = s.toUpperCase();
	if (s.startsWith("STRAIGHTFLUSH")) {
	    return new StraightFlush(s.substring(13).trim());
	} else if (s.startsWith("FOUROFAKIND")) {
	    return new FourOfAKind(s.substring(11).trim());
	} else if (s.startsWith("FLUSH")) {
	    return new Flush(s.substring(5).trim());
	} else if (s.startsWith("STRAIGHT")) {
	    return new Straight(s.substring(8).trim());
	} else if (s.startsWith("FULLHOUSE")) {
	    return new FullHouse(s.substring(9).trim());
	} else if (s.startsWith("THREEOFAKIND")) {
	    return new ThreeOfAKind(s.substring(12).trim());
	} else if (s.startsWith("TWOPAIR")) {
	    return new TwoPair(s.substring(7).trim());
	} else if (s.startsWith("ONEPAIR")) {
	    return new OnePair(s.substring(7).trim());
	} else if (s.startsWith("HIGHCARD")) {
	    return new HighCard(s.substring(8).trim());
	} else {
	    throw new HandTypeFormatException("Unknown HandType: " + s);
	}
    }
    // Pseudo-constructor from CardVector used in evaluating hands
    public static HandType fromCards(CardVector cards) {
	// We could be smarter here and use CB's algorithm
	HandType v;
	if ((v = StraightFlush.fromCards(cards)) != null) {
	    return v;
	}
	if ((v = FourOfAKind.fromCards(cards)) != null) {
	    return v;
	}
	if ((v = Flush.fromCards(cards)) != null) {
	    return v;
	}
	if ((v = Straight.fromCards(cards)) != null) {
	    return v;
	}
	if ((v = FullHouse.fromCards(cards)) != null) {
	    return v;
	}
	if ((v = ThreeOfAKind.fromCards(cards)) != null) {
	    return v;
	}
	if ((v = TwoPair.fromCards(cards)) != null) {
	    return v;
	}
	if ((v = OnePair.fromCards(cards)) != null) {
	    return v;
	}
	/* Otherwise */
	return HighCard.fromCards(cards);
    }
}
