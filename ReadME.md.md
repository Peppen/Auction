# Auction Project

Meccanismo di vendita all'asta basato sulla **rete P2P**. Ogni peer può vendere e acquistare beni utilizzando il meccanismo dell'asta al secondo prezzo, un meccanismo di asta non veritiero per più oggetti.  
Ogni offerente effettua un'offerta. Il miglior offerente ottiene il primo slot, il secondo più alto, il secondo slot e così via, ma il miglior offerente paga l'offerta di prezzo del secondo miglior offerente, che a sua volta paga l'offerta di prezzo del terzo e così via.


## Soluzione

L'implementazione della soluzione ha previsto la realizzazione di un'interfaccia **Auctionable** contenente i metodi prencipali ( **createAuction** che permette la creazione di un'asta, **checkAuction** che permette di controllare lo stato dell'asta e **placeBid** che permette di piazzare una puntata in quell'asta), l'implementazione di ognuno di essi nella classe **AuctionablaImpl**, e la creazione di un'interfaccia grafica nella classe Main **AuctionMain**. Il metodo **createAuction** prende in input il nome dell'asta (**String**), la data di scadenza (**LocalDateTime**), la descrizione (**String**) ed il prezzo di partenza (**double**); prima della creazione di una nuova asta, controlla che non ci sia un'asta con tale nome per non sovrascrivere quella presente ed in seguito, procede alla richiesta dei dati obbligatori per la sua creazione inserendoli in una **HashMap**. Alla fine della creazione dell'asta, un timer parte per segnare lo scadere della stessa.
Il metodo **checkAuction** prende in input il nome dell'asta (**String**), risale alla HashMap che contiene tutti i dati relativi a quell'asta e calcola il tempo rimanente alla fine dell'asta; in output ritorna una stringa contenente i dati relativi a quell'asta e il tempo rimanente alla sua fine.
Il metodo **placeBid** prende in input il nome dell'asta (**String**) e la offerta (**double**); esso risale alla HashMap contenente i dati relativi all'asta, controlla che a puntare una nuova offerta non sia il peer creatore dell'asta che non ha i permessi, che la nuova offerta sia superiore a quella già presente e che l'asta non sia ancora finita, ed a seconda di questi controlli ritorna una stringa con la nuova puntata, se tutto ha avuto successo, o con un errore a seconda dei controlli in caso di insuccesso.


## Membri del Progetto
Il progetto contiene un package principale **Auction** che contiene quattro classi e intefacce (Auctionable, AuctionMain, AuctionableImpl ed AuctionResult) e tre package: il package **exception**, il package **listener** ed il package **util**. Il package exception contiene la classe **AuctionException** che prende in input un messaggio e ritorna, ogni volta che viene lanciata, quel messaggio. Il package listener contiene l'interfaccia **MessageListener** e la classe **MessageListenerImpl** che contiene l'implementazione dell'unico metodo contenuto nell'interfaccia MessageListener, **ParseMessage**. ParseMessage non fa altro che stampare nel TextTerminal il messaggio diretto ricevuto dagli altri peer. Il package util contiene la classe **MapUtil** che contiene il metodo **reverse** che inverte l'HashMap. 
Per quanto riguarda le altre classi, AuctionableImpl, oltre a contenere l'implementazione dei metodi dell'interfaccia Auctionable, contiene altri metodi che servono ai metodi della stessa interfaccia per controlli e notifiche. Il metodo **exists** controlla se esiste un'asta con lo stesso nome e se il tempo di fine asta è già superato in modo che un'asta con un nome già esistente possa essere creata.
Il metodo **isCreator** controlla se il peer che punta una nuova offerta sia il peer creatore dell'asta. I metodi **notifyPeersBidPlaced** e **notifyPeersAuctionEnded** sono metodi di notifica, inviano un messaggio non appena un'offerta viene piazzata nel primo caso o non appena l'asta finisce nel secondo. Il metodo **startTimer** viene invocato ogni volta che un'asta parte ed è stato implementato in modo da controllare ogni secondo, tramite un thread, se l'asta sta per finire; non appena l'asta finisce viene invocato il metodo notifyPeersAuctionEnded che avvisa tutti i peer della fine dell'asta, del vincitore e di quanto debba pagare ogni peer.


## Test Cases
La classe **AuctionableImplTest** contiene l'implementazione dei test cases riguardanti i metodi dell'interfaccia **Auctionable**;
**testCreateAuction** crea una nuova asta e controlla che tutti i suoi parametri siano uguali a quelli presenti nella HashMap; **testCheckAuction** controlla che le stringhe di ritorno dell'asta appena creata e quella presa dalla HashMap siano uguali; **testPlaceBid** controlla che la puntata appena fatta e quella presente nella HashMap siano uguali. 




