# rflr
*[raf-uh ler] The most amazingest rafling application on the market yet.*

- - -

This file contains some information on the application, its like a report, but less formal.

This application is a raffle game, based on a famous danish game called "Snyd", and many other things
a Wikipedia article can be found at this  [link](https://da.wikipedia.org/wiki/T%C3%A6nkeboks_(terningspil%29)
The article contains the basic rules of the game.

This file will shortly introduce what parts of the game has been implemented, as not all rules has
been implemented, as the game is played differently from place to place.

## The advanced topics in this application is:

- Bluetooth
- Sensors (Accelerometer)


## The implementation has the following features:

- Connect to another player using Bluetooth.
- Bluetooth Service as an android service.
- Shake the device, and use accelerometer to "roll" the dice.
- Make a guess for what's in the cups (raflebægere).
- Lift the cups, to see who won.
- Remove a die, when I win.
- Calculate the stair (trappen).
- Turn-based gameplay.


## How it works:

One of the players must host the game, by selecting the "Host Game" option in the main menu. This will make
his device act as a Bluetooth server, on which another device can connect to, to get a Bluetooth socket to
send messages to.
The other player must then chose the "Connect to Game" option in the main menu, to connect to the first player.
Once connected, the players will receive a Toast, saying that they are connected.

They are now connected, and can proceed into the "ingame-activity", by selecting the "Start Ingame".
This will take them to a new activity, where they can play the game.

When the ingame activity is started, the devices sends a "handshake", where they send a random integer to the
other device. This is done for two reasons: 1) to ensure that the connection works, and that the other player
is ready. 2) to decide who will start the game, highest integer starts.

All calculations are done "de-centralized", which means that each device makes decisions based on the data
received from the other device. An example for this is the calculation of the winner, When the "lift" button is
selected, the device will send over the cup to the other device. Upon receiving a cup, the device will calculate
who won, based on the last guess received, and send over his own cup to the other device.
So to summarize, all calculations are done independently, this can, and has, lead to some errors, but not game
breaking errors, where the two disagreed.


*Our face after making this "great application"*
![Image of Harold](http://i1.kym-cdn.com/photos/images/original/000/848/178/9f9.png)
