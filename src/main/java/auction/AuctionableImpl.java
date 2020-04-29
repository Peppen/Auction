package auction;

import auction.exception.AuctionException;
import auction.listener.MessageListener;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class AuctionableImpl implements Auctionable {


    private final static String AUCTION_NAME = "auctionName";
    private final static String END_TIME = "endTime";
    private final static String RESERVED_PRICE = "reservedPrice";
    private final static String DESCRIPTION = "description";
    private final static String PEER_ADDRESSES = "peerAddresses";
    private final static String SOURCE = "source";
    private final static String CREATION_TIME = "creationTime";
    private final static String BIDDERS = "bidders";
    private final static String ERROR_MASTER = " ERROR IN MASTER PEER BOOTSTRAP. \n";
    private final static String AUCTION_ENDED = " AUCTION ENDED \n";
    private final static String BID_PLACED = " A NEW BID WAS PLACED IN AUCTION ";
    private final static String BID_NOT_ENTERED = " THERE IS A BID BIGGER THAN THIS, BID NOT ENTERED SUCCESSFULLY \n";
    private final static String NOT_POSSIBLE_AUCTION_ENDED = " THE AUCTION IS ENDED, IT IS NOT POSSIBLE TO PLACE A BID \n";
    private final static String NOT_ALLOWED = "\n NOT ALLOWED TO PLACE A BID \n";
    private final static String NEW_LINE = " \n ";
    private final static String BID_ENTERED = " BID ENTERED SUCCESSFULLY \n";
    private final static String NEW_RESERVED_PRICE = " THE NEW RESERVED PRICE IS ";
    private final static String OPERATION_FAILED = "\n OPERATION FAILED \n";
    private final static String PRICE = " WITH PRICE ";
    private final static int DEFAULT_MASTER_PORT = 4000;
    private final PeerDHT dht;


    AuctionableImpl(int id, String masterPeer, MessageListener listener) throws IOException {
        Peer peer = new PeerBuilder(Number160.createHash(id)).ports(DEFAULT_MASTER_PORT + id).start();
        dht = new PeerBuilderDHT(peer).start();
        FutureBootstrap fb = peer.bootstrap().inetAddress(InetAddress.getByName(masterPeer)).ports(DEFAULT_MASTER_PORT).start();
        fb.awaitUninterruptibly();
        if (fb.isSuccess()) {
            peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
        } else {
            throw new RuntimeException(ERROR_MASTER);
        }
        peer.objectDataReply(new ObjectDataReply() {
            public Object reply(PeerAddress sender, Object request) {
                return listener.parseMessage(request);
            }
        });
    }

    @Override
    public boolean createAuction(String auctionName, LocalDateTime endTime, double reservedPrice, String description) throws IOException, ClassNotFoundException {
        FutureGet futureGet = dht.get(Number160.createHash(auctionName)).start();
        futureGet.awaitUninterruptibly();
        Set<PeerAddress> addresses = new HashSet<>();
        addresses.add(dht.peerAddress());
        if (futureGet.isSuccess() && !exists(auctionName)) {
            Map<String, Object> map = new HashMap<>();
            map.put(AUCTION_NAME, auctionName);
            map.put(END_TIME, endTime);
            map.put(RESERVED_PRICE, reservedPrice);
            map.put(DESCRIPTION, description);
            map.put(PEER_ADDRESSES, addresses);
            map.put(SOURCE, dht.peerAddress());
            map.put(CREATION_TIME, LocalDateTime.now());
            map.put(BIDDERS, new HashMap<PeerAddress,Double>());
            dht.put(Number160.createHash(auctionName)).data(new Data(map)).start().awaitUninterruptibly();
            startTimer(endTime, auctionName, reservedPrice);
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String checkAuction(String auctionName) throws IOException, ClassNotFoundException {
        FutureGet futureGet = dht.get(Number160.createHash(auctionName)).start();
        futureGet.awaitUninterruptibly();
        if (futureGet.isSuccess()) {
            HashMap<String, Object> map = (HashMap<String, Object>) futureGet.dataMap().values().iterator().next().object();
            LocalDateTime endTime = (LocalDateTime) map.get(END_TIME);
            if (endTime.isBefore(LocalDateTime.now()))
                return AUCTION_ENDED;
            Duration duration = Duration.between(LocalDateTime.now(), endTime);
            long seconds = duration.getSeconds();
            return "THE AUCTION WAS CREATED IN: " + map.get(CREATION_TIME).toString() + NEW_LINE + "THE EXPIRATION IS IN: " + formatDuration(seconds)
                    + NEW_LINE + "THE RESERVED PRICE IS " + map.get(RESERVED_PRICE).toString() + NEW_LINE + "THE DESCRIPTION IS: " + map.get(DESCRIPTION) + NEW_LINE;
        }
        return OPERATION_FAILED;
    }

    @Override
    @SuppressWarnings("unchecked")
    public String placeBid(String auctionName, double bidAmount) throws IOException, ClassNotFoundException {
        FutureGet futureGet = dht.get(Number160.createHash(auctionName)).start();
        futureGet.awaitUninterruptibly();
        if (futureGet.isSuccess()) {
            Map<String, Object> map = (HashMap<String, Object>) futureGet.dataMap().values().iterator().next().object();
            PeerAddress source = (PeerAddress) map.get(SOURCE);
            if (source.equals(dht.peerAddress()))
                return NOT_ALLOWED;
            Double reservedPrice = (Double) map.get(RESERVED_PRICE);
            LocalDateTime endTime = (LocalDateTime) map.get(END_TIME);
            Set<PeerAddress> addresses = (HashSet<PeerAddress>) map.get(PEER_ADDRESSES);
            Map<PeerAddress, Double> bidders = (HashMap<PeerAddress, Double>) map.get(BIDDERS);
            if (bidAmount > reservedPrice) {
                if (endTime.isAfter(LocalDateTime.now())) {
                    map.put(RESERVED_PRICE, bidAmount);
                    bidders.put(dht.peerAddress(),bidAmount);
                    addresses.add(dht.peerAddress());
                    dht.put(Number160.createHash(auctionName)).data(new Data(map)).start().awaitUninterruptibly();
                    notifyPeersBidPlaced(dht.peerAddress(), auctionName, bidAmount);
                    return BID_ENTERED + NEW_RESERVED_PRICE + bidAmount + NEW_LINE;
                } else
                    throw new AuctionException(NOT_POSSIBLE_AUCTION_ENDED);
            } else
                throw new AuctionException(BID_NOT_ENTERED);
        }
        return OPERATION_FAILED;
    }

    @SuppressWarnings("unchecked")
    boolean exists(String auctionName) throws IOException, ClassNotFoundException {
        FutureGet futureGet = dht.get(Number160.createHash(auctionName)).start();
        futureGet.awaitUninterruptibly();
        if (futureGet.isEmpty()) {
            return false;
        }
        HashMap<String, Object> map = (HashMap<String, Object>) futureGet.dataMap().values().iterator().next().object();
        String auction = (String) map.get(AUCTION_NAME);
        LocalDateTime endTime = (LocalDateTime) map.get(END_TIME);
        return auctionName.equals(auction) && endTime.isAfter(LocalDateTime.now());
    }

    @SuppressWarnings("unchecked")
    boolean isCreator(String auctionName) throws IOException, ClassNotFoundException {
        FutureGet futureGet = dht.get(Number160.createHash(auctionName)).start();
        futureGet.awaitUninterruptibly();
        HashMap<String, Object> map = (HashMap<String, Object>) futureGet.dataMap().values().iterator().next().object();
        PeerAddress source = (PeerAddress) map.get(SOURCE);
        return source.equals(dht.peerAddress());

    }

    double getReservedPrice(String auctionName) throws IOException, ClassNotFoundException {
        FutureGet futureGet = dht.get(Number160.createHash(auctionName)).start();
        futureGet.awaitUninterruptibly();
        @SuppressWarnings("unchecked")
        HashMap<String, Object> map = (HashMap<String, Object>) futureGet.dataMap().values().iterator().next().object();
        return (double) map.get(RESERVED_PRICE);
    }

    private void notifyPeersBidPlaced(PeerAddress peer, String auctionName, double price) throws IOException, ClassNotFoundException {
        HashSet<PeerAddress> peerAddresses = getPeerAddresses(auctionName);
        for (PeerAddress peerAddress : peerAddresses) {
            if (!peerAddress.equals(peer)) {
                FutureDirect direct = dht.peer().sendDirect(peerAddress).object(BID_PLACED + auctionName + PRICE + price).start();
                direct.awaitUninterruptibly();
            }
        }
    }

    private void notifyPeersAuctionEnded(String priceInfo, String auctionName) throws IOException, ClassNotFoundException {
        HashSet<PeerAddress> peerAddresses = getPeerAddresses(auctionName);
        for (PeerAddress address : peerAddresses) {
            dht.peer().sendDirect(address).object(priceInfo).start();
        }
    }

    @SuppressWarnings("unchecked")
    private void startTimer(LocalDateTime endTime, String auctionName, Double initialPrice) {
        Runnable runnable = () -> {
            LocalDateTime now = LocalDateTime.now();
            try {
                ZoneOffset o = OffsetDateTime.now().getOffset();
                long diff = endTime.toEpochSecond(o) - now.toEpochSecond(o);
                Thread.sleep(diff * 1000);
                FutureGet futureGet = dht.get(Number160.createHash(auctionName)).start();
                futureGet.awaitUninterruptibly();
                if (futureGet.isSuccess() && !futureGet.isEmpty()) {
                    Map<String, Object> map = (HashMap<String, Object>) futureGet.dataMap().values().iterator().next().object();
                    Map<PeerAddress, Double> bidders = (HashMap<PeerAddress, Double>) map.get(BIDDERS);
                    AuctionResult auctionResult = new AuctionResult();
                    String result = auctionResult.calculateWinner(initialPrice, bidders);
                    notifyPeersAuctionEnded(result, auctionName);
                    map.clear();
                }
            } catch (InterruptedException | ClassNotFoundException | IOException e) {
                System.err.println(e);
            }
        };
        Thread t = new Thread(runnable);
        t.start();
    }

    private String formatDuration(long seconds) {
        return String.format("%02dh:%02dm:%02ds", seconds / 3600, (seconds % 3600) / 60, (seconds % 60));
    }

    @SuppressWarnings("unchecked")
    private HashSet<PeerAddress> getPeerAddresses(String auctionName) throws IOException, ClassNotFoundException {
        FutureGet futureGet = dht.get(Number160.createHash(auctionName)).start();
        futureGet.awaitUninterruptibly();
        HashMap<String, Object> map = (HashMap<String, Object>) futureGet.dataMap().values().iterator().next().object();
        return (HashSet<PeerAddress>) map.get(PEER_ADDRESSES);
    }
}
