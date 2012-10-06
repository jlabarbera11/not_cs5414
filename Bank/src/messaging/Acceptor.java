package messaging;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Acceptor implements Runnable {

    private ServerSocket socket = null;
    private BlockingQueue<MessageRequest> messageBuffer = null;
    private int numconnections;

    public Acceptor(Socket s, BlockingQueue<MessageRequest> m, int n) {
        this.socket = socket;
        this.messageBuffer = m;
        this.numconnections = n;
    }

    public void run() {
        for(int i=0; i<this.numconnections; i++) {
            try {
                Socket s = this.socket.accept();
                ConnectionHandler c = new ConnectionHandler(s, this.messageBuffer);
                c.run();
            } catch (IOException e) {}
        }
    }
}
