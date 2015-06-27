/*
 * Utilities.java
 *
 * George Ferguson, ferguson@cs.rochester.edu,  8 Sep 1998
 * Time-stamp: <Sun Sep 20 14:48:45 EDT 1998 ferguson>
 */

public class Utilities {
    // Compares A and B and returns: 1 if A<B, 0 if A==B, and -1 if A>B
    public static int compareInts(int a, int b) {
	if (a < b) {
	    return -1;
	} else if (a > b) {
	    return 1;
	} else {
	    return 0;
	}
    }
    // Compares arrays of ints A and B pairwise by element
    public static int compareIntArrays(int a[], int b[]) {
	//System.err.println("compareIntArrays: a=" + a + ", b=" + b);
	if (a.length != b.length) {
	    System.err.println("compareIntArrays: not same length!");
	    return compareInts(a.length, b.length);
	}
	for (int i=0; i < a.length; i++) {
	    int result = compareInts(a[i], b[i]);
	    if (result != 0) {
		//System.err.println("compareIntArrays: a[" + i + "]=" + a[i] +
		//		   ", b[" + i + "]=" + b[i]);
		//System.err.println("compareIntArrays: returning " + result);
		return result;
	    }
	}
	return 0;
    }
    // Returns true if A starts with B ignoring case, else false
    public static boolean startsWithIgnoreCase(String a, String b) {
	//System.err.println("a=\"" + a + "\", b=\"" + b + "\"");
	//return a.substring(b.length()).equalsIgnoreCase(b);
	return a.regionMatches(true, 0, b, 0, b.length());
    }
}
