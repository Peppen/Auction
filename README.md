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

L'implementazione della soluzione ha previsto la realizzazione di un'interfaccia **Auctionable** contenente i metodi prencipali ( **createAuction** che permette la creazione di un'asta, **checkAuction** che permette di controllare lo stato dell'asta e **placeBid** che permette di piazzare una puntata in quell'asta), l'implementazione di ognuno di essi nella classe **AuctionablaImpl**, e la creazione di un'interfaccia grafica nella classe Main **AuctionMain**. Il metodo **createAuction** prende in input il nome dell'asta (**String**), la data di scadenza (**LocalDateTime**), la descrizione (**String**) ed il prezzo di partenza (**double**); prima della creazione di una nuova asta, controlla che non ci sia un'asta con tale nome per non sovrascrivere quella presente ed in seguito, procede alla richiesta dei dati obbligatori per la sua creazione inserendoli in una **HashMap**. Alla fine della creazione dell'asta, un timer parte per segnare lo scadere della stessa; una volta scaduto il tempo l'asta volge al termine e viene stampato a video il vincitore e l'importo da spendere.
Il metodo **checkAuction** prende in input il nome dell'asta (**String**), risale alla HashMap che contiene tutti i dati relativi a quell'asta e calcola il tempo rimanente alla fine dell'asta; in output ritorna una stringa contenente i dati relativi a quell'asta e il tempo rimanente alla sua fine.
Il metodo **placeBid** prende in input il nome dell'asta (**String**) e la offerta (**double**); esso risale alla HashMap contenente i dati relativi all'asta, controlla che a puntare una nuova offerta non sia il peer creatore dell'asta, che non ha i permessi, che la nuova offerta sia superiore a quella già presente e che l'asta non sia ancora finita, ed a seconda di questi controlli ritorna una stringa con la nuova puntata se tutto ha avuto successo, o con un errore a seconda dei controlli in caso di insuccesso.

## Project Member

Il progetto contiene un package principale **Auction** che contiene quattro classi e intefacce (Auctionable, AuctionMain, AuctionableImpl ed AuctionResult) e tre package: il package **exception**, il package **listener** ed il package **util**. Il package exception contiene la classe **AuctionException** che prende in input un messaggio e ritorna, ogni volta che viene lanciata, quel messaggio. Il package listener contiene l'interfaccia **MessageListener** e la classe **MessageListenerImpl** che contiene l'implementazione dell'unico metodo contenuto nell'interfaccia MessageListener, **ParseMessage**. ParseMessage non fa altro che stampare nel TextTerminal il messaggio diretto ricevuto dagli altri peer. Il package util contiene la classe **MapUtil** che contiene il metodo **reverse** che inverte l'HashMap. 
Per quanto riguarda le altre classi, AuctionableImpl, oltre a contenere l'implementazione dei metodi dell'interfaccia Auctionable, contiene altri metodi che servono ai metodi della stessa interfaccia per controlli e notifiche. Il metodo **exists** controlla se esiste un'asta con lo stesso nome e se il tempo di fine asta è già superato in modo che un'asta con un nome già esistente possa essere creata.
Il metodo **isCreator** controlla se il peer che punta una nuova offerta sia il peer creatore dell'asta. I metodi **notifyPeersBidPlaced** e **notifyPeersAuctionEnded** sono metodi di notifica, inviano un messaggio non appena un'offerta viene piazzata nel primo caso o non appena l'asta finisce nel secondo. Il metodo **startTimer** viene invocato ogni volta che un'asta parte ed è stato implementato in modo da controllare ogni secondo, tramite un thread, se l'asta sta per finire; non appena l'asta finisce viene invocato il metodo notifyPeersAuctionEnded che avvisa tutti i peer della fine dell'asta, del vincitore e di quanto debba pagare ogni peer.

## Test Case

La classe **AuctionableImplTest** contiene l'implementazione dei test cases riguardanti i metodi dell'interfaccia **Auctionable**;
**testCreateAuction** crea una nuova asta e controlla che tutti i suoi parametri siano uguali a quelli presenti nella HashMap; **testCheckAuction** controlla che le stringhe di ritorno dell'asta appena creata e quella presa dalla HashMap siano uguali; **testPlaceBid** controlla che la puntata appena fatta e quella presente nella HashMap siano uguali. 


