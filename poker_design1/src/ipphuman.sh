#!/bin/sh
#
# ipphuman.sh: Wrapper for Java PokerClient
#
# George Ferguson, ferguson@cs.rochester.edu,  9 Oct 1998
# Time-stamp: <Thu Jan 19 15:32:43 EST 2006 ferguson>
#

JAVA=java

classpath=ipp.jar

exec $JAVA -classpath $classpath PokerClient ${1+"$@"}
