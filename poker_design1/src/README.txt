Time-stamp: <Mon Oct  5 16:36:39 EDT 1998 ferguson>

Very brief description of what is in here.


I have tried to separate game-specific functionality from generic
functionality. So there are several classes like "PokerServer.java"
and "HoldemPokerServer.java", the idea being that other games could be
implemented as cublasses of the generic class, and then the main() in
the generic class could be modified to invoke the right game, perhaps
based on a command-line argument.

The faceless server is implemented by PokerServer.java and its
game-specific subclass HoldemPokerServer.java. The actual flow of the
game is implemented by HoldemPokerGame.java.

The general class for drawing the display of Holdem game is in
HoldemPokerDisplay.java. This is used by clients of all kinds.

The client for letting a human player play is in PokerClient.java and
HoldemPokerClient.java. It tries to be smart about what the player's
options are by monitoring the state of the game and enabling buttons
as necessary.

The display client for the server is implemented by
PokerController.java and HoldemPokeController.java. This code is
derived from the PokerClient, although it doesn't have to be as smart
and it understands messages a bit differently that the human-interface
client.

If I get around to it, there will be a PokerPlayer.java and
HoldemPokerPlayer.java that will implement autonomous clients. These
will also be based on the PokerClient code.

Card is the basic card class, CardVector is a vector of same,
CardDisplay is the way of drawing these in a display. Player is the
basic representation of a player in the game, PlayerVector is a vector
of these, and PlayerDisplay is the way of drawing these in a display.
Deck is a deck of cards. Pot is the pot of money in the game.
HandTypeSelector and the HandTypeSelectorInterface are for asking a
person what hand they have. MessageDialog is used to put up model
dialog box. RandomInt and Utilities are what you'd expect.
OnePlayerLeftException is thrown when there's one player left in the
game after the second to last player folds, so we don't have to
clutter up the game playing code testing this condition.

