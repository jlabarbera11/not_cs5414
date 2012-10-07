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
    private ServerSocket serverSocket = null;
    private Socket socket = null;
    private ObjectInputStream ois = null;
    private BlockingQueue<MessageRequest> messageBuffer = null;

    public ConnectionHandler(BlockingQueue<MessageRequest> m, Integer branch) {
        this.branch = branch;
        try {
            this.serverSocket = new ServerSocket();
            this.messageBuffer = m;
        } catch(java.io.IOException e) {}
    }
    
    public ConnectionHandler(Socket s, BlockingQueue<MessageRequest> m, Integer branch) {
        this.branch = branch;
        this.socket = s;
        this.messageBuffer = m;
    }

    public void run() {
        System.out.println("Running connection handler!");
        try {
            if(this.socket == null)
                this.socket = this.serverSocket.accept();
            System.out.println("Established connection to " + this.branch);

            ObjectInputStream ois = new ObjectInputStream(this.socket.getInputStream());
            while(true) {
                MessageRequest r = (MessageRequest)ois.readObject();
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

