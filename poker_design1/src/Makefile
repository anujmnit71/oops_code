#
# Makefile for Poker Server
#
# George Ferguson, ferguson@cs.rochester.edu, 12 Sep 1998
# Time-stamp: <Thu Jan 19 15:21:21 EST 2006 ferguson>
#

JAVAC  = javac
JAVA   = java
JFLAGS = 
JAR    = jar

SRCS_ippd = \
	PokerServer.java PokerServerDisplay.java \
	PokerGame.java HoldemPokerGame.java DrawPokerGame.java \
	FiveCardStudPokerGame.java SevenCardStudPokerGame.java \
	PokerFrame.java \
	PokerClient.java \
	HandTypeSelector.java HandTypeSelectorInterface.java \
	Flush.java FourOfAKind.java \
	FullHouse.java HandType.java \
	HandTypeFormatException.java \
	HighCard.java OnePair.java \
	Straight.java StraightFlush.java \
	ThreeOfAKind.java TwoPair.java \
	Card.java CardDisplay.java CardVector.java \
	Deck.java Pot.java \
	Player.java PlayerDisplay.java PlayerVector.java \
	ShowdownResult.java \
	MessageDialog.java \
	DrawSelector.java DrawSelectorInterface.java \
	OnePlayerLeftException.java \
	RandomInt.java Utilities.java
SRCS_ippplayer = PokerPlayer.java HoldemPokerPlayer.java
SRCS_ipphuman = PokerClient.java
SRCS = $(SRCS_ippd) $(SRCS_ippplayer) $(SRCS_ipphuman)
CLASSES = $(SRCS:.java=.class)
XCLASSES = \
	CardPanelLayout.class \
	PlayerHandler.class \
	PokerFrame\$$1.class \
	PokerTablePanel.class \
	PokerLayoutManager.class \
	RankSelector.class \
	ShowdownResult.class
JAR = ipp.jar
DOCS = doc
PROGS = ippd ippplayer ipphuman
MANS = ippd.man ippplayer.man ipphuman.man
XTRA = ippd.sh ippplayer.sh ipphuman.sh Makefile README.txt HISTORY.txt \
	examples

.SUFFIXES: .class .java

.java.class:
	$(JAVAC) $(JFLAGS) $<

default: $(JAR)

.DUMMY: ippd ippplayer ipphuman

ippd: $(SRCS_ippd:.java=.class)

ippplayer: $(SRCS_ippplayer:.java=.class)

ipphuman: $(SRCS_ippd:.java=.class)

MANIFEST = manifest.txt
jar $(JAR): ippd ippplayer ipphuman
	echo "Main-Class: $(MAIN)" >$(MANIFEST)
	jar cmvf $(MANIFEST) $(JAR) $(CLASSES) $(XCLASSES) images images.small
	rm $(MANIFEST)

clean:
	rm -f $(CLASSES) $(XCLASSES) $(JAR)

install:
	test -d $(INSTALL_DIR) || mkdir $(INSTALL_DIR)
	#chmod 0775 $(INSTALL_DIR)
	# Libraries (classes)
	test -d $(INSTALL_DIR)/lib || mkdir $(INSTALL_DIR)/lib
	chmod 0775 $(INSTALL_DIR)/lib
	cp -p ipp.jar $(INSTALL_DIR)/lib
	chmod 0664 $(INSTALL_DIR)/lib/ipp.jar
	# Executables
	test -d $(INSTALL_DIR)/bin || mkdir $(INSTALL_DIR)/bin
	chmod 0775 $(INSTALL_DIR)/bin
	for prog in $(PROGS); do \
	  cp -p $$prog.sh $(INSTALL_DIR)/bin/$$prog; \
	  chmod 775 $(INSTALL_DIR)/bin/$$prog; \
	done
	# Manpages
	test -d $(INSTALL_DIR)/man || mkdir $(INSTALL_DIR)/man
	chmod 0775 $(INSTALL_DIR)/man
	test -d $(INSTALL_DIR)/man/man1 || mkdir $(INSTALL_DIR)/man/man1
	chmod 0775 $(INSTALL_DIR)/man/man1
	for prog in $(PROGS); do \
	  cp -p $$prog.man $(INSTALL_DIR)/man/man1/$$prog.1; \
	  chmod 0664 $(INSTALL_DIR)/man/man1/$$prog.1; \
	done
	# Documentation
	test -d $(INSTALL_DIR)/doc || mkdir $(INSTALL_DIR)/doc
	chmod 0775 $(INSTALL_DIR)/doc
	cp -p $(DOCS) $(INSTALL_DIR)/doc
	chmod 0664 $(INSTALL_DIR)/doc/*
	# Examples
	test -d $(INSTALL_DIR)/examples || mkdir $(INSTALL_DIR)/examples
	chmod 0775 $(INSTALL_DIR)/examples
	cp -p examples/* $(INSTALL_DIR)/examples
	chmod g-w $(INSTALL_DIR)/examples/*

install.man:
	test -d $(INSTALL_DIR)/man || mkdir $(INSTALL_DIR)/man
	chmod 0775 $(INSTALL_DIR)/man
	test -d $(INSTALL_DIR)/man/man1 || mkdir $(INSTALL_DIR)/man/man1
	chmod 0775 $(INSTALL_DIR)/man/man1
	for prog in $(PROGS); do \
	  cp -p $$prog.man $(INSTALL_DIR)/man/man1/$$prog.1; \
	  chmod 0664 $(INSTALL_DIR)/man/man1/$$prog.1; \
	done

mantext:
	for file in *.man; do \
	  nroff -man $$file | perl -p -e 's/.\010//g' > `basename $$file .man`.txt; \
	done
