/*
 * RandomInt.java
 *
 * George Ferguson, ferguson@cs.rochester.edu,  8 Sep 1998
 * Time-stamp: <Mon Sep 14 15:17:33 EDT 1998 ferguson>
 *
 * Class that cannot be instantiated but which supplies random numbers
 * between 0 and N via method random(N).
 */

import java.util.Random;

public class RandomInt {
    // Members
    private static Random rand = null;
    // Methods
    public static int random(int n) {
	if (rand == null) {
	    rand = new Random();
	}
	int r = rand.nextInt();
	if (r < 0) {
	    r *= -1;
	}
	return r % n;
    }
}
