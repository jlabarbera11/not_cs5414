package messaging;

import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

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

    private Socket clientsocket = null;
    private ObjectOutputStream clientoos = null;
    private ObjectInputStream clientois = null;

    private ServerSocket serversocket = null;
    private Map<Integer, ObjectOutputStream> outputstreams = null;

    private BlockingQueue<Message> messageBuffer; 

    public enum Type {
        CLIENT,
        SERVER
    }

    //This class receives messages and puts them into a synchronized buffer
    private class ConnectionHandler implements Runnable {
        private Integer branch;
        private ObjectInputStream ois = null;

        public ConnectionHandler(ObjectInputStream ois, Integer branch) {
            this.branch = branch;
            this.ois = ois;
        }
        public void run() {
            System.out.println("Running connection handler for " + this.branch + "!");
            try {
                while(true) {
                    Message r = (Message)this.ois.readObject();
                    System.out.println("Got message!");
                    messageBuffer.put(r);
                }
            } catch(Exception e) {
                System.out.println(e.toString() + " thrown from ConnectionHandler " + this.branch);
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
                    if(ir.getType() == Type.CLIENT) {
                        outputstreams.put(-1, new ObjectOutputStream(newsocket.getOutputStream()));
                        System.out.println("Added client to output streams");
                    }
                    System.out.println("Established incoming connection from " + ir.getBranch());

                    new Thread(new ConnectionHandler(ois, ir.getBranch())).start();
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

        if (T == Type.SERVER) {
            try {
                this.serversocket = new ServerSocket(Integer.parseInt(this.resolver.get(this.branch)[1]));
                serversocket.setReuseAddress(true);
                this.outputstreams= new HashMap<Integer, ObjectOutputStream>();
                this.messageBuffer = new LinkedBlockingQueue<Message>();
            } catch (IOException e) {
                throw new MessagingException(MessagingException.Type.FAILED_SOCKET_CREATION);
            }
        }
    }

    public Set<Integer> whoNeighbors() {
        return this.topology.get(this.branch);
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

    //Client Methods
    public void connectToServer() throws MessagingException {
        try {
            String[] res = this.resolver.get(this.branch);
            System.out.println("Connecting to server");
            this.clientsocket = new Socket(InetAddress.getByName(res[0]), Integer.parseInt(res[1]));
            System.out.println("Connected to server!");
            this.clientsocket.setSoTimeout(5 * 1000);
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

    private Response sendRequest(Request M) throws MessagingException {
        System.out.println("Sending message!");
        try {
            this.clientoos.writeObject(M);
            System.out.println("Sent message!");
            return (Response)this.clientois.readObject();
        } catch (IOException e) {
            throw new MessagingException(MessagingException.Type.FAILED_REQUEST_SEND);
        } catch (ClassNotFoundException e) {
            throw new MessagingException(MessagingException.Type.FAILED_RESPONSE_RECEIVE);
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

    public SnapshotResponse TakeSnapshot() throws MessagingException {
        return (SnapshotResponse)sendRequest(new SnapshotRequest(-1, 1));
    }
    //End Client Methods


    //Server Methods
    public void makeConnections() {
        new Thread(this.new Acceptor()).start();
    }

    public void sendMessage(Integer branch, Object o) throws MessagingException {
        for(int i=0; i<5; i++) {
            try {
                try {
                    if(this.outputstreams.get(branch) == null)
                        throw new IOException();
                    this.outputstreams.get(branch).writeObject(o);
                    return;
                } catch (IOException e) {
                    System.out.println("Could not connect to branch " + branch + ", trying again");
                    Thread.sleep(1000);
                    String[] res = this.resolver.get(branch);
                    Socket s = new Socket(InetAddress.getByName(res[0]), Integer.parseInt(res[1]));
                    ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                    oos.writeObject(new InitializeRequest(Type.SERVER, this.branch));
                    this.outputstreams.put(branch, oos);
                }
            } catch (IOException e) {
                continue;
            } catch (InterruptedException e) {
                throw new MessagingException(MessagingException.Type.UNKNOWN_ERROR);
            }
        }
        throw new MessagingException(MessagingException.Type.FAILED_REQUEST_SEND);
    }

    //For responding to client
    public void SendResponse(Response M) throws MessagingException {
        System.out.println("Sending response!");
        sendMessage(-1, M);
        System.out.println("Sent response!");
    }

    public void FinishTransfer(Integer branch, Integer acnt, Float amt, Integer ser_number) throws MessagingException {
        if(this.branch.compareTo(branch) == 0)
            return;
        sendMessage(branch, new DepositFromTransferMessage(acnt, amt, ser_number));
    }

    public void sendSnapshot(Integer id, Snapshot snapshot) throws MessagingException {
        for(Integer i : this.topology.get(this.branch))
            sendMessage(i, snapshot);
    }

    public Message ReceiveMessage() throws MessagingException {
        try {
            return this.messageBuffer.take();
        } catch(java.lang.InterruptedException e) {
            throw new MessagingException(MessagingException.Type.FAILED_SYNC_BUFFER);
        }
    }
    //End Server Methods
   
}

