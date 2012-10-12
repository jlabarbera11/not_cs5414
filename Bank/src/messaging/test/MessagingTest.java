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
    
}
