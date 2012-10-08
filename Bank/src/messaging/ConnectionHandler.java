package messaging;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.ObjectInputStream;
import java.io.IOException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.lang.InterruptedException;

public class ConnectionHandler implements Runnable {

    private Integer branch;
    private ObjectInputStream ois = null;
    private BlockingQueue<MessageRequest> messageBuffer = null;

    public ConnectionHandler(ObjectInputStream ois, BlockingQueue<MessageRequest> m, Integer branch) {
        this.branch = branch;
        this.ois = ois;
        this.messageBuffer = m;
    }

    public void run() {
        System.out.println("Running connection handler for " + this.branch + "!");
        try {
            while(true) {
                MessageRequest r = (MessageRequest)this.ois.readObject();
                System.out.println("Got message!");
                messageBuffer.put(r);
            }
        } catch(ClassNotFoundException e) {
            System.out.println("1st exception in " + this.branch);
        } catch(IOException e) {
            System.out.println("2nd exception in " + this.branch);
        } catch(InterruptedException e) {
            System.out.println("3rd exception in " + this.branch);
        }

    }
}

