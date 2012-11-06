package messaging;

import java.util.Scanner;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.lang.InterruptedException;


public class Messaging {

    private Integer branch = null;
    private Map<Integer, Set<Integer>> topology = null;
    private Map<Integer, String[]> resolver = null;

    private String head = null;
    private Socket clientsocket = null;
    private ObjectOutputStream clientoos = null;
    private ObjectInputStream clientois = null;

    private ServerSocket serversocket = null;
    private Map<Integer, ObjectOutputStream> branchstreams = null;
    private Map<Integer, ObjectOutputStream> replicastreams = null;
    
    private Socket oraclesocket = null;
    private ObjectOutputStream oracleoos = null;
    private ObjectInputStream oracleois = null;

    private BlockingQueue<Message> messageBuffer; 

    public enum Type {
        CLIENT,
        SERVER
    }

    //This class receives messages and puts them into a synchronized buffer
    private class ConnectionHandler implements Runnable {
        private ObjectInputStream ois = null;

        public ConnectionHandler(ObjectInputStream ois) {
            this.ois = ois;
        }
        public void run() {
            try {
                while(true) {
                    Message r = (Message)this.ois.readObject();
                    messageBuffer.put(r);
                }
            } catch(Exception e) {
                System.out.println(e.toString() + " thrown from ConnectionHandler");
            }
        }
    }

    //This class listens for new connections, distributing ones that are made
    //to a connection handler
    private class Acceptor implements Runnable {
        public void run() {
            while(true) {
                try {
                    System.out.println("Attempting to accept connection");
                    Socket newsocket = serversocket.accept();

                    ObjectInputStream ois = new ObjectInputStream(newsocket.getInputStream());
                    InitializeRequest ir = (InitializeRequest)ois.readObject();
                    if(ir.getType() == Type.CLIENT)
                        clientoos = new ObjectOutputStream(newsocket.getOutputStream());
                    System.out.println("Established incoming connection from " + ir.getBranch());

                    new Thread(new ConnectionHandler(ois)).start();
                } catch(Exception e) {
                    System.out.println(e.toString() + " thrown from Acceptor Thread");
                }
            }
        }
    }

    public Messaging(Integer b, Type T) throws MessagingException {
        this(b, T, "topology.txt", "resolver.txt");
    }

    public Messaging(Integer b, Type T, String topologyfile, String resolverfile) throws MessagingException {
        if(!buildTopology(topologyfile))
            throw new MessagingException(MessagingException.Type.INVALID_TOPOLOGY);
        buildResolver(resolverfile);
        
        this.branch = b;        
        this.messageBuffer = new LinkedBlockingQueue<Message>();
        if (T == Type.SERVER) {
            try {
                this.serversocket = new ServerSocket(Integer.parseInt(this.resolver.get(this.branch)[1]));
                serversocket.setReuseAddress(true);
                this.branchstreams= new HashMap<Integer, ObjectOutputStream>();
                this.replicastreams= new HashMap<Integer, ObjectOutputStream>();
            } catch (IOException e) {
                throw new MessagingException(MessagingException.Type.FAILED_SOCKET_CREATION);
            }
        }
    }

    //We've extended whoNeighbors to return an array:
    //The first element contains all neighbors we can reach in one hop
    //The second element contains all neighbors that can reach us in one hop
    public List<Set<Integer>> whoNeighbors() {
        Set<Integer> first = this.topology.get(this.branch);

        Set<Integer> second = new HashSet<Integer>();
        for(Integer i : this.topology.keySet()) {
            if(this.topology.get(i).contains(this.branch))
                second.add(i);
        }

        List<Set<Integer>> r = new ArrayList<Set<Integer>>();
        r.add(first);
        r.add(second);
        return r;
    }
    
    private void _checkTopopolgy(Set<Integer> checked, Integer current) {
        checked.add(current);
        if(!this.topology.containsKey(current))
            return;

        for(Integer val : this.topology.get(current)) {
            if(checked.contains(val))
                continue;
            else
                _checkTopopolgy(checked, val);
        }
    }

    public boolean checkTopology() throws MessagingException {
        for(Integer key : this.topology.keySet()) {
            Set<Integer> checked = new HashSet<Integer>();
            checked.add(key);
            for(Integer val : this.topology.get(key)) {
                if(checked.contains(val))
                    continue;
                else
                    _checkTopopolgy(checked, val);
            }
            if(checked.size() != this.topology.keySet().size())
                return false;
        }
        return true;
    }

    //Returns true if every server can reach every other one
    private boolean buildTopology(String topologyfile) throws MessagingException {
        this.topology = new HashMap<Integer, Set<Integer>>();
        try {
            Scanner scanner = new Scanner(new File(topologyfile));
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
        } catch (FileNotFoundException e) {
            throw new MessagingException(MessagingException.Type.FILE_NOT_FOUND);
        }
        return checkTopology();
    }

    private void buildResolver(String resolverfile) throws MessagingException {
        this.resolver = new HashMap<Integer, String[]>();
        try {
            Scanner scanner = new Scanner(new File(resolverfile));
            while (scanner.hasNextLine()) {
                String[] branch = scanner.nextLine().split(" ");
                this.resolver.put(Integer.parseInt(branch[0]), new String[]{branch[1], branch[2]});
            }
        } catch (FileNotFoundException e) {
            throw new MessagingException(MessagingException.Type.FILE_NOT_FOUND);
        } 
    }

    //Primitive send and Receive
    private void sendMessage(ObjectOutputStream oos, Message m) throws MessagingException {
        try {
            oos.writeObject(m);
            System.out.println("message sent");
        } catch (IOException e) {
            throw new MessagingException(MessagingException.Type.FAILED_REQUEST_SEND);
        }
    }

    private Message receiveMessage() throws MessagingException {
        try {
            return this.messageBuffer.take();
        } catch(java.lang.InterruptedException e) {
            throw new MessagingException(MessagingException.Type.FAILED_SYNC_BUFFER);
        }
    }

    //Client Methods
    public void connectToServer(Callback c) throws MessagingException {
        try {
            String[] res = this.resolver.get(this.branch);
            System.out.println("Connecting to server");
            this.clientsocket = new Socket(InetAddress.getByName(res[0]), Integer.parseInt(res[1]));
            System.out.println("Connected to server!");
            this.clientoos = new ObjectOutputStream(this.clientsocket.getOutputStream());
            System.out.println("Sending initialize request");
            this.clientoos.writeObject(new InitializeRequest(Type.CLIENT, -1));
            System.out.println("Sent initialize request");
            this.clientois = new ObjectInputStream(this.clientsocket.getInputStream());
        } catch (UnknownHostException e) {
            throw new MessagingException(MessagingException.Type.UNKNOWN_HOST);
        } catch(IOException e) {
            throw new MessagingException(MessagingException.Type.FAILED_SOCKET_CREATION);
        }
    }

    //Convient method to send a message and return a response for client
    ////TODO: Make asynchrounous
    private ResponseClient sendRequest(RequestClient M) throws MessagingException {
        while(true) {
            try {
                sendMessage(this.oracleoos, new WhoIsHeadRequest());
                WhoIsHeadResponse r = (WhoIsHeadResponse)receiveMessage();
                if (r.GetHead() != this.head) {
                    String[] head = r.GetHead().split(":");
                    this.clientsocket = new Socket(InetAddress.getByName(head[0]), Integer.parseInt(head[1]));
                    this.clientoos = new ObjectOutputStream(this.clientsocket.getOutputStream());
                    this.clientois = new ObjectInputStream(this.clientsocket.getInputStream());
                }
                sendMessage(this.clientoos, M);
                return (ResponseClient)receiveMessage();
            } catch(MessagingException e) {
                continue;
            } catch(IOException e) {
                throw new MessagingException(MessagingException.Type.FAILED_SOCKET_CREATION);
            }
        }
    }

    public DepositResponse Deposit(Integer branch, Integer acnt, Float amt, Integer ser_number) throws MessagingException {
        if (branch.compareTo(this.branch) != 0)
            return new DepositResponse("Cannot desposit to this branch");
        return (DepositResponse)sendRequest(new DepositRequest(acnt, amt, ser_number));
    }

    public WithdrawResponse Withdraw(Integer branch, Integer acnt, Float amt, Integer ser_number) throws MessagingException {
        if (branch.compareTo(this.branch) != 0)
            return new WithdrawResponse("Cannot withdraw from this branch");
        return (WithdrawResponse)sendRequest(new WithdrawRequest(acnt, amt, ser_number));
    }

    public QueryResponse Query(Integer branch, Integer acnt, Integer ser_number) throws MessagingException {
        if (branch.compareTo(this.branch) != 0)
            return new QueryResponse("Cannot query account info from this branch");
        return (QueryResponse)sendRequest(new QueryRequest(acnt, ser_number));
    }

    public TransferResponse Transfer(Integer src_branch, Integer src_acnt, Integer dest_branch, Integer dest_acnt, Float amt, Integer ser_number) throws MessagingException {
        if (this.branch.compareTo(src_branch) != 0)
            return new TransferResponse("Cannot transfer money from this branch");
        if (src_branch.compareTo(dest_branch) != 0 && !topology.get(src_branch).contains(dest_branch))
            return new TransferResponse("Cannot transfer money to this branch");
        return (TransferResponse)sendRequest(new TransferRequest(dest_branch, src_acnt, dest_acnt, amt, ser_number));
    }
    //End Client Methods


    //Server Methods
    public void makeConnections() {
        new Thread(this.new Acceptor()).start();
    }

    public void SendToClient(ResponseClient M) throws MessagingException {
        sendMessage(this.clientoos, M);
    }

    public void SendToBranch(Integer branch, Message M) throws MessagingException {
        sendMessage(this.branchstreams.get(branch), M);
    }

    public void SendToReplica(Integer replica, Message M) throws MessagingException {
            this.sendMessage(this.replicastreams.get(replica), M);
    }

    public void SendToOracle(Message M) throws MessagingException {
        this.sendMessage(this.oracleoos, M);
    }
    
    public Message ReceiveMessage() throws MessagingException {
        return this.receiveMessage();
    }

    public void FinishTransfer(Integer branch, Integer acnt, Float amt, Integer ser_number) throws MessagingException {
        if(this.branch.compareTo(branch) == 0)
            return;
        this.SendToBranch(branch, new DepositFromTransferMessage(this.branch, acnt, amt, ser_number));
    }
    //End Server Methods
   
}

