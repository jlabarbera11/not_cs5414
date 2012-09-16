package messaging;

import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

public class Messaging {

    private Integer branch = null;
    private Map<Integer, Set<Integer>> topology = null;

    private ServerSocket serversocket = null;
    private Socket clientsocket = null;
    private ObjectInputStream ois = null;
    private ObjectOutputStream oos = null;

    private InetAddress serverHost = null;
    private Integer serverPort = null;

    public enum Type {
        CLIENT,
        SERVER
    }
    
    private void buildTopology() {
        this.topology = new HashMap<Integer, Set<Integer>>();
        Scanner scanner = new Scanner("topology.txt");
        while (scanner.hasNextLine()) {

            String[] a = scanner.nextLine().split(" ");
            Integer key = Integer.parseInt(a[0]);
            Integer val = Integer.parseInt(a[1]);

            if (this.topology.containsKey(key)) {
                this.topology.get(key).add(val);
            }

            else {
                Set<Integer> s = new HashSet<Integer>();
                s.add(val);
                this.topology.put(key, s);
            }
        }
    }

    private void resolveBranch() throws MessagingException {
        Scanner scanner = new Scanner("resolver.txt");
        while (scanner.hasNextLine()) {
            String[] branch = scanner.nextLine().split(" ");
            if (Integer.parseInt(branch[0]) == this.branch) {
                try {
                    this.serverHost = InetAddress.getByName(branch[1]);
                } catch (UnknownHostException e) {
                    throw new MessagingException(MessagingException.Type.UNKNOWN_HOST);
                }
                this.serverPort = Integer.parseInt(branch[2]);
            }
        }
    }

    public Messaging(Integer b, Type T) throws MessagingException {
        buildTopology();
        if (!topology.containsKey(b)) {
            throw new MessagingException(MessagingException.Type.INVALID_BRANCH_DECLARATION);
        }
        
        this.branch = b;
        resolveBranch();

        if (T == Type.SERVER) {
            try {
                this.serversocket = new ServerSocket(this.serverPort);
            } catch (IOException e) {
                throw new MessagingException(MessagingException.Type.FAILED_SOCKET_CREATION);
            }
        }
    }

    private MessageResponse sendRequest(MessageRequest M) throws MessagingException {

        try {
            Socket socket = new Socket(this.serverHost, this.serverPort);
            socket.setSoTimeout(5 * 1000);
            
            ObjectOutputStream o = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream i = new ObjectInputStream(socket.getInputStream());

            o.writeObject(M);
            MessageResponse response = (MessageResponse)i.readObject();

            i.close();
            o.close();
            socket.close();

            return response;
        } catch (IOException e) {
            throw new MessagingException(MessagingException.Type.FAILED_REQUEST_SEND);
        } catch (ClassNotFoundException e) {
            throw new MessagingException(MessagingException.Type.FAILED_RESPONSE_RECEIVE);
        }
    }

    public DepositResponse Deposit(Integer acnt, Float amt, Integer ser_number) throws MessagingException {
        if (branch != this.branch)
            return new DepositResponse(false);

        return (DepositResponse)sendRequest(new DepositRequest(acnt, amt, ser_number));
    }

    public WithdrawResponse Withdraw(Integer branch, Integer acnt, Float amt, Integer ser_number) throws MessagingException {
        if (branch != this.branch)
            throw new MessagingException(MessagingException.Type.INVALID_DEST_BRANCH);

        return (WithdrawResponse)sendRequest(new WithdrawRequest(acnt, amt, ser_number));
    }

    public QueryResponse Query(Integer branch, Integer acnt, Integer ser_number) throws MessagingException {
        if (branch != this.branch)
            return new QueryResponse(false);

        return (QueryResponse)sendRequest(new QueryRequest(acnt, ser_number));
    }

    public TransferResponse Transfer(Integer src_branch, Integer src_acnt, Integer dest_branch, Integer dest_acnt, Float amt, Integer ser_number) throws MessagingException {
        if (this.branch != src_branch)
            return new TransferResponse(false);
        if (src_branch != dest_branch && !topology.get(src_branch).contains(dest_branch))
            return new TransferResponse(false);

        return (TransferResponse)sendRequest(new TransferRequest(src_acnt, dest_acnt, amt, ser_number));
    }


    //Server Methods
    public MessageRequest ReceiveMessage() throws MessagingException {
        try {
            this.clientsocket = serversocket.accept();
            this.oos = new ObjectOutputStream(this.clientsocket.getOutputStream());
            this.ois = new ObjectInputStream(this.clientsocket.getInputStream());

            return (MessageRequest)this.ois.readObject();
        } catch (IOException e) {
            throw new MessagingException(MessagingException.Type.FAILED_REQUEST_RECEIVE);
        } catch (ClassNotFoundException e) {
            throw new MessagingException(MessagingException.Type.FAILED_REQUEST_RECEIVE);
        }
    }

    public void SendResponse(MessageResponse M) throws MessagingException {
        try {
            this.oos.writeObject(M);

            this.ois.close();
            this.oos.close();
            this.clientsocket.close();
        } catch (IOException e) {
            throw new MessagingException(MessagingException.Type.FAILED_RESPONSE_SEND);
        }
    }

}
