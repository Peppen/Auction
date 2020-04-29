package auction;

import java.io.IOException;
import java.time.LocalDateTime;

public interface Auctionable {

    boolean createAuction(String auctionName, LocalDateTime endTime, double reservedPrice, String description) throws IOException, ClassNotFoundException;

    String checkAuction(String auctionName) throws IOException, ClassNotFoundException;

    String placeBid(String auctionName, double bidAmount) throws IOException, ClassNotFoundException;

}
