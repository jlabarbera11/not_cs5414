package messaging.test;

import org.junit.*;
import messaging.*;

public class MessagingTest {

    @Test public void testTopology() throws Exception {
        
        //test valid topology
        Messaging valid = new Messaging(1, Messaging.Type.CLIENT, "src/messaging/test/validtopology.txt", "src/messaging/test/resolver.txt");

        //test invalid topology
        try {
            Messaging invalid = new Messaging(1, Messaging.Type.CLIENT, "src/messaging/test/invalidtopology.txt", "src/messaging/test/resolver.txt");
            assert(false);
        } catch (Exception e) {
            assert(e instanceof MessagingException && ((MessagingException)(e)).type == MessagingException.Type.INVALID_TOPOLOGY);
        }
    }
    
    @Test public void testDeposit() throws Exception {
            Messaging server = new Messaging(1, Messaging.Type.SERVER, "src/messaging/test/simpletopology.txt", "src/messaging/test/resolver.txt");
            server.makeConnections();

            Messaging client = new Messaging(1, Messaging.Type.CLIENT, "src/messaging/test/simpletopology.txt", "src/messaging/test/resolver.txt");
            client.connectToServer();

            client.Deposit(new Integer(1), new Integer(11111), new Float(100), new Integer(1));
            DepositRequest message = (DepositRequest)server.ReceiveMessage();
            assert(message.getAcnt() == 11111 && message.getAmt() == 100 && message.getSerNumber() == 1);
    }
}
