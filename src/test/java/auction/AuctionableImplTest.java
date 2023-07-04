package auction;

import auction.listener.MessageListener;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.peers.Number160;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Map;

import static org.junit.Assert.*;

// Test Class

public class AuctionableImplTest {

    private final static String AUCTION_NAME = "auctionName";
    private final static String END_TIME = "endTime";
    private final static String RESERVED_PRICE = "reservedPrice";
    private final static String DESCRIPTION = "description";
    private int id = 0;
    private String masterPeer = "127.0.0.1";

    private MessageListener listener = new MessageListener() {
        @Override
        public Object parseMessage(Object obj) {
            return null;
        }
    };

    private AuctionableImpl createAuctionable() throws IOException {
        return new AuctionableImpl(id, masterPeer, listener);
    }


    private PeerDHT getDHT(AuctionableImpl auctionableImpl) throws NoSuchFieldException, IllegalAccessException {
        Field dht = AuctionableImpl.class.
                getDeclaredField("dht");
        dht.setAccessible(true);
        return (PeerDHT) dht.get(auctionableImpl);
    }

    @Test
    public void testCreateAuction() throws IOException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        AuctionableImpl auctionableImpl = createAuctionable();
        LocalDateTime expectedEndTime = LocalDateTime.now();
        String expectedAuctionName = "Auction";
        Double expectedReservedPrice = 120.0;
        String expectedDescription = "Description";
        auctionableImpl.createAuction(expectedAuctionName, expectedEndTime, expectedReservedPrice, expectedDescription);
        PeerDHT dht = getDHT(auctionableImpl);
        FutureGet futureGet = dht.get(Number160.createHash(expectedAuctionName)).start();
        futureGet.awaitUninterruptibly();
        Map<String, Object> map = (Map<String, Object>) futureGet.dataMap().values().iterator().next().object();
        String auctionName = (String) map.get(AUCTION_NAME);
        LocalDateTime endTime = (LocalDateTime) map.get(END_TIME);
        Double reservedPrice = (Double) map.get(RESERVED_PRICE);
        String description = (String) map.get(DESCRIPTION);
        assertEquals(auctionName, expectedAuctionName);
        assertEquals(endTime, expectedEndTime);
        assertEquals(0, reservedPrice.compareTo(expectedReservedPrice));
        assertEquals(description, expectedDescription);
    }
}
