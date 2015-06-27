#!/bin/sh
#
# ippd.sh: Wrapper for Java PokerServer
#
# George Ferguson, ferguson@cs.rochester.edu,  9 Oct 1998
# Time-stamp: <Thu Jan 19 15:32:15 EST 2006 ferguson>
#

JAVA=java

classpath=ipp.jar

exec $JAVA -classpath $classpath PokerServer ${1+"$@"}
