package auction.listener;

import org.beryx.textio.swing.SwingTextTerminal;

import java.util.HashMap;

public class MessageListenerImpl implements MessageListener {

    private final static String DIRECT_MESSAGE = " (DIRECT MESSAGE RECEIVED) ";
    private final static String SUCCESS = " SUCCESS ";
    private final static String NEW_LINE = " \n ";
    private SwingTextTerminal terminal;

    public MessageListenerImpl(SwingTextTerminal terminal) {
        this.terminal = terminal;
    }

    @Override
    public Object parseMessage(Object obj) {
        terminal.print(DIRECT_MESSAGE + obj + NEW_LINE);
        return SUCCESS;
    }
}
