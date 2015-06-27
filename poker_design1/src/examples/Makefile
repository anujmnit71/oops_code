#
# Makefile for sample poker clients
#
# George Ferguson, ferguson@cs.rochester.edu,  1 Oct 1998
# Time-stamp: <Tue Oct 13 18:23:26 EDT 1998 ferguson>
#
# Perl clients don't need to be made.
#


CC    = gcc
CXX   = g++
JAVAC = /usr/java/bin/javac -verbose
.SUFFIXES: .java .class

all: pokerclient PokerClient PokerClient.class socket.so

#
# C client
#
pokerclient: pokerclient.c
	$(CC) -o $@ pokerclient.c -lsocket -lnsl

#
# C++ client
#
PokerClient: PokerClient.C PokerClient.h
	$(CXX) -o $@ PokerClient.C -lsocket -lnsl

#
# Java client
#
PokerClient.class: PokerClient.java
	$(JAVAC) PokerClient.java

#
# Lisp client foreign functions
#
socket.so: socket.o
	ld -G -o $@ socket.o

#
# Cleanup
#
clean:
	rm -f pokerclient PokerClient.class PokerClient socket.o socket.so

install:
	cp -p Makefile \
		pokerclient.c pokerclient \
		pokerclient.perl5 \
		PokerClient.java PokerClient.class \
		PokerClient.C PokerClient.h PokerClient \
		pokerclient.lisp socket.c socket.so \
	    /u/ferguson/poker/clients
