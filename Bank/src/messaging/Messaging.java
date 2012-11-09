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
import java.net.SocketTimeoutException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.lang.InterruptedException;
import java.lang.ClassNotFoundException;

import client.Client;

//import oracle.Oracle.replicaState;


public class Messaging {

    private String branch = null;
    private String replica = null;
    private Map<String, Set<String>> topology = null;
    private Map<String, String[]> resolver = null;

    private Callback tc;
    private Socket clientsocket = null;
    private ObjectOutputStream clientoos = null;
    private ObjectInputStream clientois = null;

    private ServerSocket serversocket = null;
    public Map<String, ObjectOutputStream> branchstreams = null;
    public Map<String, ObjectOutputStream> replicastreams = null;

    private Socket oraclesocket = null;
    private ObjectOutputStream oracleoos = null;
    private ObjectInputStream oracleois = null;

    private String[] oracleAddress;

    //Oracle fields
    private Map<String, ObjectOutputStream> replicaOutputStreams;
    private Map<String, ObjectInputStream> clientInputStreams = new HashMap<String, ObjectInputStream>();
    private Map<String, ObjectOutputStream> clientOutputStreams = new HashMap<String, ObjectOutputStream>();

    private BlockingQueue<Message> messageBuffer = null;

    public enum Type {
        CLIENT,
            BRANCH,
            REPLICA,
            ORACLE
    }

    //This class receives messages and puts them into a synchronized buffer
    private class ConnectionHandler implements Runnable {
        private ObjectInputStream ois = null;
        private Callback callback = null;

        public ConnectionHandler(ObjectInputStream ois) {
            this.ois = ois;
        }

        public ConnectionHandler(ObjectInputStream ois, Callback c) {
            this.ois = ois;
            this.callback = c;
        }

        public void run() {
            try {
                while(true) {
                    Message r = (Message)this.ois.readObject();
                    if(callback != null)
                        this.callback.Callback(r);
                    else
                        messageBuffer.put(r);
                }
            } catch(Exception e) {
                System.out.println(e.toString() + " thrown from ConnectionHandler");
            }
        }
    }

    //This class listens for new connections, distributing input streams to
    //connection handlers
    private class Acceptor implements Runnable {
        public void run() {
            while(true) {
                try {
                    System.out.println("Attempting to accept connection");
                    Socket newsocket = serversocket.accept();

                    ObjectInputStream ois = new ObjectInputStream(newsocket.getInputStream());
                    InitializeMessage ir = (InitializeMessage)ois.readObject();
                    System.out.println("Accepted connection from " + ir.GetBranch() + "." + ir.GetReplica());
                    if(ir.GetBranch() == null && ir.GetReplica() == null) {} /*This is the oracle*/
                    else if(ir.GetBranch().equals(branch) && ir.GetReplica() == null) {
                        System.out.println("Found client");
                        clientoos = new ObjectOutputStream(newsocket.getOutputStream());
                    }
                    else if(ir.GetBranch().equals(branch)) /*This is a replica*/ {
                        System.out.println("Found replica");
                        replicastreams.put(ir.GetReplica(), new ObjectOutputStream(newsocket.getOutputStream()));
                    }
                    else if(ir.GetBranch() != branch) {
                        System.out.println("Found branch");
                        branchstreams.put(ir.GetBranch(), new ObjectOutputStream(newsocket.getOutputStream()));
                    }
                    else
                        continue;

                    new Thread(new ConnectionHandler(ois)).start();
                } catch(Exception e) {
                    System.out.println(e.toString() + " thrown from Acceptor Thread");
                }
            }
        }
    }

    public Messaging(String b, String r) throws MessagingException {
        this(b, r, "topology.txt", "resolver.txt");
    }

    public Messaging(String b, String r, String topologyfile, String resolverfile) throws MessagingException {
        if(!buildTopology(topologyfile))
            throw new MessagingException(MessagingException.Type.INVALID_TOPOLOGY);
        buildResolver(resolverfile);
        initializeOracleAddress();

        this.branch = b;
        this.replica = r;
        if(b == null && r == null) { /*Oracle*/
            try {
                this.serversocket = new ServerSocket(Integer.parseInt(this.oracleAddress[1]));
                this.replicaOutputStreams = new HashMap<String, ObjectOutputStream>();
            } catch (IOException e) {
                throw new MessagingException(MessagingException.Type.FAILED_SOCKET_CREATION);
            }

        } else if (r != null) { /*Server*/
            try {
                this.serversocket = new ServerSocket(Integer.parseInt(this.resolver.get(this.branch+"."+this.replica)[1]));
                serversocket.setReuseAddress(true);
                this.branchstreams= new HashMap<String, ObjectOutputStream>();
                this.replicastreams= new HashMap<String, ObjectOutputStream>();
                this.messageBuffer = new LinkedBlockingQueue<Message>();
            } catch (IOException e) {
                throw new MessagingException(MessagingException.Type.FAILED_SOCKET_CREATION);
            }
        }

    }

    //We've extended whoNeighbors to return an array:
    //The first element contains all neighbors we can reach in one hop
    //The second element contains all neighbors that can reach us in one hop
    public List<Set<String>> whoNeighbors() {
        Set<String> first = this.topology.get(this.branch);

        Set<String> second = new HashSet<String>();
        for(String i : this.topology.keySet()) {
            if(this.topology.get(i).contains(this.branch))
                second.add(i);
        }

        List<Set<String>> r = new ArrayList<Set<String>>();
        r.add(first);
        r.add(second);
        return r;
    }

    private void _checkTopopolgy(Set<String> checked, String current) {
        checked.add(current);
        if(!this.topology.containsKey(current))
            return;

        for(String val : this.topology.get(current)) {
            if(checked.contains(val))
                continue;
            else
                _checkTopopolgy(checked, val);
        }
    }

    public boolean checkTopology() throws MessagingException {
        for(String key : this.topology.keySet()) {
            Set<String> checked = new HashSet<String>();
            checked.add(key);
            for(String val : this.topology.get(key)) {
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

    public static ConcurrentHashMap<String, Set<String>> buildTopologyHelper(String topologyfile) throws MessagingException {
        ConcurrentHashMap<String, Set<String>> map = new ConcurrentHashMap<String, Set<String>>();
        try {
            Scanner scanner = new Scanner(new File(topologyfile));
            while (scanner.hasNextLine()) {

                String[] a = scanner.nextLine().split(" ");
                String key = a[0];
                String val = a[1];

                if (map.containsKey(key)) {
                    map.get(key).add(val);
                }

                else {
                    Set<String> s = new HashSet<String>();
                    s.add(val);
                    map.put(key, s);
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            throw new MessagingException(MessagingException.Type.FILE_NOT_FOUND);
        }
        return map;
    }

    //Returns true if every server can reach every other one
    private boolean buildTopology(String topologyfile) throws MessagingException {
        this.topology = buildTopologyHelper(topologyfile);    
        return checkTopology();
    }

    public static ConcurrentHashMap<String, String[]> buildResolverHelper(String resolverfile) throws MessagingException {
        ConcurrentHashMap<String, String[]> resolver = new ConcurrentHashMap<String, String[]>();
        try {
            Scanner scanner = new Scanner(new File(resolverfile));
            while (scanner.hasNextLine()) {
                String[] branch = scanner.nextLine().split(" ");
                //System.out.println("resolver iteration. current line contains " + branch[0] + ":" + branch[1] + ":" + branch[2]);
                resolver.put(branch[0], new String[]{branch[1], branch[2]});
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            throw new MessagingException(MessagingException.Type.FILE_NOT_FOUND);
        } 
        return resolver;
    }

    //a resolver key is now a string and has form aa.bb
    private void buildResolver(String resolverfile) throws MessagingException {
        this.resolver = buildResolverHelper(resolverfile);
    }

    //Primitive send and Receive
    private void sendMessage(ObjectOutputStream oos, Message m) throws MessagingException {
        try {
            oos.writeObject(m);
        } catch (IOException e) {
            throw (new MessagingException(MessagingException.Type.FAILED_MESSAGE_SEND));
        }
    }

    private Message receiveMessage() {
        try {
            return this.messageBuffer.take();
        } catch(java.lang.InterruptedException e) {
            System.out.println("Failed to receieve message");
        }
        return null;
    }

    //Client Methods

    //must be called in a synchronized block
    public void ClientUpdatePrimary(String id) throws MessagingException{
    	System.out.println("client updating primary for id " + id);
    	try {
	        String[] res = this.resolver.get(id);
            try {
            	this.clientsocket.close();
            } catch (Exception e){
            	//ignore
            }
	        this.clientsocket = new Socket(InetAddress.getByName(res[0]), Integer.parseInt(res[1]));
	        this.clientoos = new ObjectOutputStream(this.clientsocket.getOutputStream());
	        this.clientoos.writeObject(new InitializeMessage(this.branch, null));
	        this.clientois = new ObjectInputStream(this.clientsocket.getInputStream());
	        System.out.println("client done updating primary");
        } catch (UnknownHostException e) {
            throw new MessagingException(MessagingException.Type.UNKNOWN_HOST);
        } catch(IOException e) {
            throw new MessagingException(MessagingException.Type.FAILED_SOCKET_CREATION);
        }
    }
    
    public void initializeOracleAddress() throws MessagingException{
        try {
            Scanner scanner = new Scanner(new File("oracle.txt"));
            this.oracleAddress = scanner.nextLine().split(" ");
            scanner.close();
        } catch (FileNotFoundException e) {
            throw new MessagingException(MessagingException.Type.FILE_NOT_FOUND);
        } 
    }

    //must call initializeOracleAddress before this
    public void connectToServer(Callback t) throws MessagingException {
        try {
            String[] res = this.resolver.get(this.branch + ".01"); //hard-coded 01 because replica 1 should not start failed
            this.clientsocket = new Socket(InetAddress.getByName(res[0]), Integer.parseInt(res[1]));
            this.clientoos = new ObjectOutputStream(this.clientsocket.getOutputStream());
            this.clientoos.writeObject(new InitializeMessage(this.branch, null));
            this.clientois = new ObjectInputStream(this.clientsocket.getInputStream());
            
            System.out.println("about to connect to oracle");
            this.oraclesocket = new Socket(InetAddress.getByName(this.oracleAddress[0]), Integer.parseInt(this.oracleAddress[1]));
            ObjectOutputStream oracleOutputStream = new ObjectOutputStream(oraclesocket.getOutputStream());
            oracleOutputStream.writeObject(new InitializeMessage(this.branch, null));
            System.out.println("created socket");
            new Thread(new ConnectionHandler(new ObjectInputStream(this.oraclesocket.getInputStream()), t)).start();
            System.out.println("created connection handler");
        } catch (UnknownHostException e) {
            throw new MessagingException(MessagingException.Type.UNKNOWN_HOST);
        } catch(IOException e) {
            throw new MessagingException(MessagingException.Type.FAILED_SOCKET_CREATION);
        }
    }

    //Convient method to send a message and return a response for client
    private ResponseClient sendRequest(RequestClient M) {
        while(true) {
            try {
                try {
                    sendMessage(this.clientoos, M);
                    return (ResponseClient)this.clientois.readObject();
                } catch (MessagingException e) {
                    System.out.println("Failed sending message to primary");
                    Thread.sleep(1000);
                } catch (SocketTimeoutException e) {
                    System.out.println("Failed receiving message from primary");
                    Thread.sleep(1000);
                } catch (IOException e) {
                    System.out.println("Failed receiving message from primary");
                    Thread.sleep(1000);
                } catch (ClassNotFoundException e) {
                    System.out.println("Unexpected ClassNotFoundException occurred");
                }
            } catch (InterruptedException e) {
                System.out.println("Unexpected exception");
            }
        }
    }

    public DepositResponse Deposit(String branch, Integer acnt, Float amt, Integer ser_number) throws MessagingException {
        if (!branch.equals(this.branch))
            return new DepositResponse("Cannot desposit to this branch");
        return (DepositResponse)sendRequest(new DepositRequest(acnt, amt, ser_number));
    }

    public WithdrawResponse Withdraw(String branch, Integer acnt, Float amt, Integer ser_number) throws MessagingException {
        if (!branch.equals(this.branch))
            return new WithdrawResponse("Cannot withdraw from this branch");
        return (WithdrawResponse)sendRequest(new WithdrawRequest(acnt, amt, ser_number));
    }

    public QueryResponse Query(String branch, Integer acnt, Integer ser_number) throws MessagingException {
        if (!branch.equals(this.branch))
            return new QueryResponse("Cannot query account info from this branch");
        return (QueryResponse)sendRequest(new QueryRequest(acnt, ser_number));
    }

    public TransferResponse Transfer(String src_branch, Integer src_acnt, String dest_branch, Integer dest_acnt, Float amt, Integer ser_number) throws MessagingException {
        System.out.println("src_branch is " + topology);
        System.out.println("dest_branch is " + topology);
        System.out.println("topology is " + topology);
        System.out.println("topology at src_branch is " + topology.get(src_branch));
        if (!src_branch.equals(this.branch))
            return new TransferResponse("Cannot transfer money from this branch");
        if (src_branch.compareTo(dest_branch) != 0 && !topology.get(src_branch).contains(dest_branch)) {
            return new TransferResponse("Cannot transfer money to this branch");
        }
        return (TransferResponse)sendRequest(new TransferRequest(dest_branch, src_acnt, dest_acnt, amt, ser_number));
    }
    //End Client Methods


    //Server Methods
    public void makeConnections() {
        new Thread(this.new Acceptor()).start();
    }

    public void SendToClient(ResponseClient M) {
        try {
            this.sendMessage(this.clientoos, M);
        } catch (MessagingException e) {
            System.out.println("Could not send to client");
        }
    }

    public void SendToBranch(String branch, Message M) {
        String branchnum = branch.substring(0,2);
        for(int i=0; i<5; i++) {
            try {
                try {
                    if(this.branchstreams.get(branchnum) == null)
                        throw new MessagingException(MessagingException.Type.UNKNOWN_HOST);
                    System.out.println("sending to branch " + branch);
                    this.sendMessage(this.branchstreams.get(branchnum), M);
                    System.out.println("sent to branch " + branch);
                    return;
                } catch(Exception e) {
                    System.out.println("Failed sending to branch " + branch);
                    Thread.sleep(1000);
                    String[] res = this.resolver.get(branch);
                    Socket s = new Socket(InetAddress.getByName(res[0]), Integer.parseInt(res[1]));
                    ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                    oos.writeObject(new InitializeMessage(this.branch, this.replica));
                    this.branchstreams.put(branchnum, oos);
                    ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                    new Thread(new ConnectionHandler(ois)).start();
                }
            } catch (IOException e) {
                continue;
            } catch (InterruptedException e) {
                System.out.println("Unexpected InterruptedException occurred");
            }
        }
    }

    public void SendToReplica(String replica, Message M) {
    	System.out.println("sending to replica " + replica);
        for(int i=0; i<5; i++) {
            try {
                try {
                    if(this.replicastreams.get(replica) == null)
                        throw new MessagingException(MessagingException.Type.UNKNOWN_HOST);
                    this.sendMessage(this.replicastreams.get(replica), M);
                    System.out.println("Sent message to " + replica);
                    return;
                } catch (MessagingException e) {
                    System.out.println("Failed sending to replica " + replica);
                    Thread.sleep(1000);
                    String[] res = this.resolver.get(this.branch+"."+replica);
                    Socket s = new Socket(InetAddress.getByName(res[0]), Integer.parseInt(res[1]));
                    ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
                    oos.writeObject(new InitializeMessage(this.branch, this.replica));
                    this.replicastreams.put(replica, oos);
                    ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
                    new Thread(new ConnectionHandler(ois)).start();
                }
            } catch (IOException e) {
                continue;
            } catch (InterruptedException e) {
                System.out.println("Unexpected InterruptedException occurred");
            }
        }
    }

    public Message ReceiveMessage() {
        return this.receiveMessage();
    }

    public void FinishTransfer(String branch, Integer acnt, Float amt, Integer ser_number) throws MessagingException {
        this.SendToBranch(branch, new TransferBranch(acnt, amt, ser_number));
    }
    //End Server Methods



    //Begin Oracle Methods

    //A client should send a InitializeMessage with its branch number
    //OracleAcceptor never terminates -> OK?
    private class OracleAcceptor implements Runnable {
        public void run() {
            while(true) {
                try {
                    System.out.println("Oracle attempting to accept connection from clients");
                    Socket newsocket = serversocket.accept();

                    ObjectInputStream ois = new ObjectInputStream(newsocket.getInputStream());
                    InitializeMessage ir = (InitializeMessage)ois.readObject();
                    clientInputStreams.put(ir.GetBranch(), ois);
                    clientOutputStreams.put(ir.GetBranch(), new ObjectOutputStream(newsocket.getOutputStream()));
                    System.out.println("got connection from client " + ir.GetBranch());
                } catch(Exception e) {
                    System.out.println(e.toString() + " thrown from OracleAcceptor Thread");
                    e.printStackTrace();
                }
            }
        }
    }
    
    public void OracleConnectToReplica(String id) throws MessagingException {
        System.out.println("Oracle is trying to initialize connection to replica " + id);
        try {
            String[] res = this.resolver.get(id);
            Socket replicaSocket = new Socket(InetAddress.getByName(res[0]), Integer.parseInt(res[1]));
            ObjectOutputStream replicaoos = new ObjectOutputStream(replicaSocket.getOutputStream());
            replicaoos.writeObject(new InitializeMessage(null, null));
            this.replicaOutputStreams.put(id, replicaoos);
        } catch(Exception e) {
            System.out.println("connection failed to " + id);
            e.printStackTrace();
            throw new MessagingException(MessagingException.Type.FAILED_CONNECTION_TO_REPLICA);
        }
    }

    //This fails if any replica is not up -> OK?
    public void OracleConnectToAllReplicas() throws MessagingException{
        for (Map.Entry<String, String[]> entry : resolver.entrySet())
        {
            System.out.println(entry.getKey());
            OracleConnectToReplica(entry.getKey());
        }
        System.out.println("done");

    }

    public void OracleAcceptClientConnections(){
        new Thread(this.new OracleAcceptor()).start();
    }

    public void OracleSendMessageToAllClients(Message message) throws MessagingException{
        for (Map.Entry<String, ObjectOutputStream> entry : clientOutputStreams.entrySet())
        {
            try {
                sendMessage(entry.getValue(), message);
            } catch (MessagingException e) {
                System.out.println("Failed sending message to all clients");
            }
        }
    }

    public void OracleSendMessageToAllReplicas(Message message) throws MessagingException{
        for (Map.Entry<String, ObjectOutputStream> entry : replicaOutputStreams.entrySet())
        {
            try {
                sendMessage(entry.getValue(), message);
            } catch (MessagingException e) {
                System.out.println("Failed sending message to all replicas");
            }
        }
    }

    public void OracleBroadcastMessage(Message message) throws MessagingException{
        OracleSendMessageToAllClients(message);
        OracleSendMessageToAllReplicas(message);
    }

    //call after replica failure
    public void OracleRemoveReplicaStreams(String id) throws MessagingException{
        if (replicaOutputStreams.remove(id) == null){
            throw new MessagingException(MessagingException.Type.REPLICA_NOT_FOUND);
        }

    }

}

