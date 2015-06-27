#!/bin/sh
#
# ippplayer.sh: Wrapper for Java PokerPlayer
#
# George Ferguson, ferguson@cs.rochester.edu,  9 Oct 1998
# Time-stamp: <Thu Jan 19 15:32:04 EST 2006 ferguson>
#

JAVA=java

classpath=ipp.jar

exec $JAVA -classpath $classpath PokerPlayer ${1+"$@"}
