package messaging;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.ObjectInputStream;
import java.io.IOException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.lang.InterruptedException;

public class ConnectionHandler implements Runnable {

    private Socket socket = null;
    private ObjectInputStream ois = null;
    private BlockingQueue<MessageRequest> messageBuffer = null;

    public ConnectionHandler(Socket s, BlockingQueue<MessageRequest> m) {
        try {
            this.socket = socket;
            this.ois = new ObjectInputStream(this.socket.getInputStream());
            this.messageBuffer = m;
        } catch(java.io.IOException e) {}
    }

    public void run() {
        while(true) {
            try {
            MessageRequest r = (MessageRequest)this.ois.readObject();
            messageBuffer.put(r);
            } catch(ClassNotFoundException e) {
            } catch(IOException e) {
            } catch(InterruptedException e) {}
        }

    }
}

