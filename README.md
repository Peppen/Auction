Exam Project for "Architetture Distribuite per il Cloud"
# Auction Mechanism P2P
<img src="https://rokt.com/wp-content/uploads/2019/04/auction-3.png" align="right" Hspace="8" Vspace="0" width="600" height="200"
Border="0">
Each peer can sell and buy goods using a Second-Price Auctions (EBay). 
Second-price auction is a non-truthful auction mechanism for multiple items. 
Each bidder places a bid. The highest bidder gets the first slot, the second-highest, the second slot and so on, 
but the highest bidder pays the price bid by the second-highest bidder, the second-highest pays the price bid by the third-highest, and so on. 
The systems allows the users to create new auction (with an ending time, a reserved selling price and a description), 
check the status of an auction, and eventually place new bid for an auction. As described in the [AuctionMechanism Java API](https://github.com/Peppen/Auction/blob/master/src/main/java/auction/Auctionable.java).

## Development
Project was developed using Java language (also, using Apache Maven as software project management) and TomP2P framework/library. TomP2P is a DHT with additional features, such as storing multiple values for a key. Each peer has a table (either disk-based or memory-based) to store its values. A single value can be queried/updated with a secondary key. The underlying communication framework uses Java NIO to handle many concurrent connections. Project can be deployed also on a Docker container, that allows the users to easily execute the application peer. The application parameters can be easily managed using the Docker environment variables. The project provides a command line interface, that enables to exploit all functionalities developed and tested. <br>
```
  $ docker run -i -e MASTERIP="127.0.0.1" -e ID=0 docker --name MASTER-PEER
  $ docker run -i -e MASTERIP="172.17.0.2" -e ID=1 docker --name GENERIC-PEER 
```

## Implementation

Problem solution involved the creation of a interface [**Auctionable**](https://github.com/Peppen/Auction/blob/master/src/main/java/auction/Auctionable.java) containing the main methods:
* **createAction** allows auction creation: it takes as input *auction name*(Type **String**), *expired Date*(Type **LocalDateTime**), a *description*(Type **String**) and a *starting price*(Type **double**); before auction is created, it checks that there is no auction with that name in order not to overwrite the present one and later, it proceeds to request the mandatory data for its creation by inserting them in a HashMap. At the end of the auction creation, a timer starts to mark the expiration of the same; once the time has elapsed, the auction comes to an end and the winner and the amount to spend are printed on the screen.
* **checkAuction** allows to check auction state: it takes as input *auction name*(Type **String**), it goes back to the HashMap which contains all the data relating to that auction and calculates the time remaining until the end of the auction; as output it returns a string containing the data relating to that auction and the time remaining until its end.
* **placeBid** allows to place a bid in that auction: it takes as input *auction name*(Type **String**) and the *bid*(Type **double**); it goes back to the HashMap containing the data relating to the auction, checks that the peer creator of the auction is not bidding for a new bid, because it does not have the permissions, that the new bid is higher than the one already present and that the auction is not still over, and depending on these checks it returns a string with the new bet if everything was successful, or with an error depending on the checks in case of failure.
All these methods are implemented in [**AuctionableImpl**](https://github.com/Peppen/Auction/blob/master/src/main/java/auction/AuctionableImpl.java) class; it was also created a graphical interface in the main class [**AuctionMain**](https://github.com/Peppen/Auction/blob/master/src/main/java/auction/AuctionMain.java).

## Project Member

The project contains a main package [**Auction**](https://github.com/Peppen/Auction/tree/master/src/main/java/auction) in which there are four classes or interfaces (*Auctionable*, *AuctionMain*, *AuctionableImpl* ed [*AuctionResult*](https://github.com/Peppen/Auction/blob/master/src/main/java/auction/AuctionResult.java)) and three other packages:
* [**Exception**](https://github.com/Peppen/Auction/tree/master/src/main/java/auction/exception) package: it contains [**AuctionException**](https://github.com/Peppen/Auction/blob/master/src/main/java/auction/exception/AuctionException.java) class that takes as input a text message and returns that message each time it is thrown
* [**Listener**](https://github.com/Peppen/Auction/tree/master/src/main/java/auction/listener) package: it contains [**MessageListener**](https://github.com/Peppen/Auction/blob/master/src/main/java/auction/listener/MessageListener.java) interface and [**MessageListenerImpl**](https://github.com/Peppen/Auction/blob/master/src/main/java/auction/listener/MessageListenerImpl.java) class  in which it is implemented the only method contained in the MessageListener interface, **ParseMessage**; ParseMessage simply prints the direct message received from other peers in the TextTerminal.
* [**Util**](https://github.com/Peppen/Auction/tree/master/src/main/java/auction/util) package: it contains the [**MapUtil**](https://github.com/Peppen/Auction/blob/master/src/main/java/auction/util/MapUtil.java) class in which it is implemented the **reverse** method that reverses the HashMap.
About the other classes, AuctionableImpl, in addition to containing the implementation of Auctionable interface methods, contains other functions used by the methods of the same interface for checks and notifications: **exists** method checks if there is an auction with the same name and if the auction end time has already passed so that an auction with an already existing name can be created; **isCreator** method checks if the peer placing a new bid is the creator peer of the auction; **notifyPeersBidPlaced** and **notifyPeersAuctionEnded** methods are notification method, they send a message as soon as a bid is placed in the first case or as soon as the auction ends in the second case; **startTimer** method is invoked every time an auction starts and has been implemented in order to check every second, via a thread, if the auction is about to end; as soon as the auction ends, the notifyPeersAuctionEnded method is invoked, which notifies all peers of the end of the auction, the winner and how much each peer has to pay.


