# Auction Mechanism P2P
<img src="https://rokt.com/wp-content/uploads/2019/04/auction-3.png" align="right" Hspace="8" Vspace="0" width="600" height="200"
Border="0">
Each peer can sell and buy goods using a Second-Price Auctions (EBay). 
Second-price auction is a non-truthful auction mechanism for multiple items. 
Each bidder places a bid. The highest bidder gets the first slot, the second-highest, the second slot and so on, 
but the highest bidder pays the price bid by the second-highest bidder, the second-highest pays the price bid by the third-highest, and so on. 
The systems allows the users to create new auction (with an ending time, a reserved selling price and a description), 
check the status of an auction, and eventually place new bid for an auction. As described in the [AuctionMechanism Java API](https://github.com/Peppen/Auction/blob/master/src/main/java/auction/Auctionable.java).

# Development
Projects was developed using Java languages (also, using Apache Maven as software project management) and TomP2P framework/library. TomP2P is a DHT with additional features, such as storing multiple values for a key. Each peer has a table (either disk-based or memory-based) to store its values. A single value can be queried / updated with a secondary key. The underlying communication framework uses Java NIO to handle many concurrent connections. Project can be deployed also on a Docker container, that allows the users to easily execute the application peer. The application parameters can be easily managed using the Docker environment variables. The project provides a command line interface, that enables to exploit all functionalities developed and tested. <br> 
<code width="1000">
  $ docker run -i -e MASTERIP="127.0.0.1" -e ID=0 docker --name MASTER-PEER
  $ docker run -i -e MASTERIP="172.17.0.2" -e ID=1 docker --name GENERIC-PEER 
<\code>

