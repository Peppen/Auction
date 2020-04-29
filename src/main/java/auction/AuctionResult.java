package auction;

import auction.util.MapUtil;
import net.tomp2p.peers.PeerAddress;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class AuctionResult {

    private final static String NEW_LINE = " \n ";
    private final static String PRICE = "HAS TO PAY: ";
    private final static String PEER = "PEER: ";

    String calculateWinner(Double initialPrice, Map<PeerAddress, Double> bidders) {
        Map<Double,PeerAddress> reversedBidders = MapUtil.reverse(bidders);
        Map<Double,PeerAddress> reversedSortedBidders = new TreeMap<>(reversedBidders);
        StringBuilder priceInfo = new StringBuilder();
        int size = bidders.size();
        for (int i = 0; i < size; i++) {
            priceInfo.append(getPriceInfo(i, initialPrice, reversedSortedBidders)).append(NEW_LINE);
        }
        return priceInfo.toString();
    }

    private String getPriceInfo(int pos, Double initialPrice, Map<Double, PeerAddress> bidders) {

        List<PeerAddress> addresses = new ArrayList<>(bidders.values());
        List<Double> prices = new ArrayList<>(bidders.keySet());
        PeerAddress winner;
        Double highBid;
        if (pos == 0) {
            winner = addresses.get(0);
            highBid = initialPrice;
        } else {
            winner = addresses.get(pos);
            highBid = prices.get(pos - 1);
        }
        return PEER + winner.peerId().intValue() + NEW_LINE + PRICE + highBid + NEW_LINE;
    }
}
