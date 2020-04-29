package auction;

import auction.exception.AuctionException;
import auction.listener.MessageListenerImpl;
import org.beryx.textio.TextIO;
import org.beryx.textio.swing.SwingTextTerminal;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AuctionMain {

    private final static String AUCTION_NAME = " AUCTION NAME: ";
    private final static String RESERVED_PRICE = " RESERVED PRICE: ";
    private final static String DESCRIPTION = " DESCRIPTION: ";
    private final static String ENTER_BID = " ENTER BID: ";
    private final static String ENTER_CORRECT = "\n ENTER A CORRECT DATE \n";
    private final static String AUCTION_CREATION_ERROR = "\n ERROR IN AUCTION CREATION \n";
    private final static String CREATE_AUCTION = "\n 1 - CREATE AUCTION";
    private final static String CHECK_AUCTION = "\n 2 - CHECK AUCTION";
    private final static String PLACE_BID = "\n 3 - PLACE A BID";
    private final static String EXIT = "\n 4 - EXIT \n";
    private final static String START_PEER = "\n Starting peer id: %d on master node: %s \n";
    private final static String ENTER_DEADLINE = " ENTER DEADLINE IN FORMAT yyyy-MM-dd HH:mm: ";
    private final static String FORMATTER = "yyyy-MM-dd HH:mm";
    private final static String AUCTION_SUCCESSFULLY_CREATED = "\n AUCTION %s SUCCESSFULLY CREATED \n";
    private final static String OPTION = "\n OPTION: ";
    private final static String ONE = "1";
    private final static String TWO = "2";
    private final static String THREE = "3";
    private final static String FOUR = "4";
    private final static String SPACE = " ";
    private final static String EXISTING_AUCTION = "\n EXISTING AUCTION \n";
    private final static String NON_EXISTING_AUCTION = "\n NON EXISTING AUCTION \n";
    private final static String NOT_ALLOWED = "\n NOT ALLOWED TO PLACE A BID \n";
    private final static String PRODUCT = "Product";
    private final static String AUCTION = "Auction";


    @Option(name = "-m", aliases = "--masterip", usage = "the master peer ip address", required = true)
    private static String master;

    @Option(name = "-id", aliases = "--identifierpeer", usage = "the unique identifier for this peer", required = true)
    private static int id;


    public static void main(String[] args) throws IOException, CmdLineException, ClassNotFoundException {
        AuctionMain auctionMain = new AuctionMain();
        auctionMain.start(args);
    }

    private void printMenu(SwingTextTerminal terminal) {
        terminal.printf(CREATE_AUCTION);
        terminal.printf(CHECK_AUCTION);
        terminal.printf(PLACE_BID);
        terminal.printf(EXIT);
    }

    private LocalDateTime getDateTime(TextIO textIO) {
        LocalDateTime endTime = LocalDateTime.now();
        boolean accepted;
        do {
            try {
                String dateTime = textIO.newStringInputReader().withMinLength(0).read(ENTER_DEADLINE);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(FORMATTER);
                endTime = LocalDateTime.parse(dateTime, formatter);
                accepted = true;
            } catch (DateTimeParseException e) {
                accepted = false;
            }
        }
        while (!accepted);
        return endTime;
    }

    private void start(String[] args) throws CmdLineException, IOException, ClassNotFoundException {
        final SwingTextTerminal terminal = new SwingTextTerminal();
        terminal.init();
        TextIO textIO = new TextIO(terminal);

        AuctionMain auction = new AuctionMain();
        final CmdLineParser parser = new CmdLineParser(auction);

        parser.parseArgument(args);
        AuctionableImpl peer = new AuctionableImpl(id, master, new MessageListenerImpl(terminal));
        terminal.printf(START_PEER, id, master);
        double reservedPrice;
        String auctionName;
        while (true) {
            try {
                printMenu(terminal);
                String option = textIO.newStringInputReader().withMinLength(0).read(OPTION);
                switch (option) {
                    case ONE:
                        auctionName = enterAuctionName(textIO);
                        if (peer.exists(auctionName)) {
                            throw new AuctionException(EXISTING_AUCTION);
                        }
                        LocalDateTime endTime = getDateTime(textIO);
                        if (endTime.isAfter(LocalDateTime.now())) {
                            reservedPrice = textIO.newDoubleInputReader().withMinVal((double) 1).withDefaultValue((double) 1).read(RESERVED_PRICE);
                            String description = textIO.newStringInputReader().withDefaultValue(PRODUCT).read(DESCRIPTION);
                            if (peer.createAuction(auctionName, endTime, reservedPrice, description))
                                terminal.printf(AUCTION_SUCCESSFULLY_CREATED, auctionName);
                            else
                                terminal.printf(AUCTION_CREATION_ERROR);
                        } else
                            terminal.printf(ENTER_CORRECT);
                        break;
                    case TWO:
                        auctionName = enterAuctionName(textIO);
                        if (peer.exists(auctionName)) {
                            String result = peer.checkAuction(auctionName);
                            if (result != null) {
                                terminal.print(SPACE + result);
                            }
                        } else
                            throw new AuctionException(NON_EXISTING_AUCTION);
                        break;
                    case THREE:
                        auctionName = enterAuctionName(textIO);
                        if (peer.exists(auctionName)) {
                            if (peer.isCreator(auctionName))
                                throw new AuctionException(NOT_ALLOWED);
                            reservedPrice = textIO.newDoubleInputReader().withDefaultValue(peer.getReservedPrice(auctionName) + 1).read(ENTER_BID);
                            String check = peer.placeBid(auctionName, reservedPrice);
                            if (check != null)
                                terminal.print(check);
                        } else
                            throw new AuctionException(NON_EXISTING_AUCTION);
                        break;
                    case FOUR:
                        System.exit(0);
                    default:
                        break;
                }
            } catch (AuctionException ex) {
                terminal.print(ex.getMessage());
            }
        }

    }

    private String enterAuctionName(TextIO textIO) {
        return textIO.newStringInputReader().withDefaultValue(AUCTION).read(AUCTION_NAME);
    }

}