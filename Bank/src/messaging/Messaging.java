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

import oracle.Oracle.replicaState;


public class Messaging {

    private Integer branch = null;
    private Map<Integer, Set<Integer>> topology = null;
    private Map<String, String[]> resolver = null;

    private Callback tc;
    private Socket clientsocket = null;
    private ObjectOutputStream clientoos = null;
    private ObjectInputStream clientois = null;

    private ServerSocket serversocket = null;
    private Map<Integer, ObjectOutputStream> branchstreams = null;
    private Map<Integer, ObjectOutputStream> replicastreams = null;
    
    private Socket oraclesocket = null;
    private ObjectOutputStream oracleoos = null;
    private ObjectInputStream oracleois = null;
    
    //Oracle fields
    private Map<String, ObjectInputStream> replicaInputStreams;
    private Map<String, ObjectOutputStream> replicaOutputStreams;

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
                    if(ir.GetBranch() == branch && ir.GetReplica() == null)
                        clientoos = new ObjectOutputStream(newsocket.getOutputStream());
                    else if(ir.GetBranch() == null) {/*This is the oracle*/}
                    else if(ir.GetBranch() == branch)
                        replicastreams.put(ir.GetReplica(), new ObjectOutputStream(newsocket.getOutputStream()));
                    else if(ir.GetBranch() != branch)
                        branchstreams.put(ir.GetBranch(), new ObjectOutputStream(newsocket.getOutputStream()));
                    else
                        continue;

                    new Thread(new ConnectionHandler(ois)).start();
                } catch(Exception e) {
                    System.out.println(e.toString() + " thrown from Acceptor Thread");
                }
            }
        }
    }

    public Messaging(Integer b, Integer r) throws MessagingException {
        this(b, r, "topology.txt", "resolver.txt");
    }

    public Messaging(Integer b, Integer r, String topologyfile, String resolverfile) throws MessagingException {
        if(!buildTopology(topologyfile))
            throw new MessagingException(MessagingException.Type.INVALID_TOPOLOGY);
        buildResolver(resolverfile);
        
        this.branch = b;        
        this.messageBuffer = new LinkedBlockingQueue<Message>();
        if (r != null) {
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
    
    public static HashMap<Integer, Set<Integer>> buildTopologyHelper(String topologyfile) throws MessagingException {
    	HashMap<Integer, Set<Integer>> map = new HashMap<Integer, Set<Integer>>();
        try {
            Scanner scanner = new Scanner(new File(topologyfile));
            while (scanner.hasNextLine()) {

                String[] a = scanner.nextLine().split(" ");
                Integer key = Integer.parseInt(a[0]);
                Integer val = Integer.parseInt(a[1]);

                if (map.containsKey(key)) {
                    map.get(key).add(val);
                }

                else {
                    Set<Integer> s = new HashSet<Integer>();
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
    
    public static Map<String, String[]> buildResolverHelper(String resolverfile) throws MessagingException {
    	Map<String, String[]> resolver = new HashMap<String, String[]>();
        try {
            Scanner scanner = new Scanner(new File(resolverfile));
            while (scanner.hasNextLine()) {
                String[] branch = scanner.nextLine().split(" ");
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
    public void connectToServer(Callback t) throws MessagingException {
        try {
            String[] res = this.resolver.get(this.branch);
            this.clientsocket = new Socket(InetAddress.getByName(res[0]), Integer.parseInt(res[1]));
            this.clientoos = new ObjectOutputStream(this.clientsocket.getOutputStream());
            this.clientoos.writeObject(new InitializeMessage(this.branch, null));
            this.clientois = new ObjectInputStream(this.clientsocket.getInputStream());
            
            res = this.resolver.GetOracle();
            this.oraclesocket = new Socket(InetAddress.getByName(res[0]), Integer.parseInt(res[1]));
            new ConnectionHandler(new ObjectInputStream(this.clientsocket.getInputStream()), t).run();
        } catch (UnknownHostException e) {
            throw new MessagingException(MessagingException.Type.UNKNOWN_HOST);
        } catch(IOException e) {
            throw new MessagingException(MessagingException.Type.FAILED_SOCKET_CREATION);
        }
    }

    //Convient method to send a message and return a response for client
    private ResponseClient sendRequest(RequestClient M) throws MessagingException {
        while(true) {
            try {
                sendMessage(this.clientoos, M);
                return (ResponseClient)receiveMessage();
            } catch(MessagingException e) {
                continue;
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
        this.SendToBranch(branch, new TransferBranch(this.branch, acnt, amt, ser_number));
    }
    //End Server Methods
    
    /**try {
        String[] res = this.resolver.get(this.branch);
        this.clientsocket = new Socket(InetAddress.getByName(res[0]), Integer.parseInt(res[1]));
        this.clientoos = new ObjectOutputStream(this.clientsocket.getOutputStream());
        this.clientoos.writeObject(new InitializeMessage(this.branch, null));
        this.clientois = new ObjectInputStream(this.clientsocket.getInputStream());
        
        res = this.resolver.GetOracle();
        this.oraclesocket = new Socket(InetAddress.getByName(res[0]), Integer.parseInt(res[1]));
        new ConnectionHandler(new ObjectInputStream(this.clientsocket.getInputStream()), t).run();
    } catch (UnknownHostException e) {
        throw new MessagingException(MessagingException.Type.UNKNOWN_HOST);
    } catch(IOException e) {
        throw new MessagingException(MessagingException.Type.FAILED_SOCKET_CREATION);
    }*/
    
    //Begin Oracle Methods
    public boolean OracleConnectToReplica(String id){
    	System.out.println("Oracle is trying to initialize connection to replica " + id);
    	try {
	        String[] res = this.resolver.get(id);
	        Socket replicaSocket = new Socket(InetAddress.getByName(res[0]), Integer.parseInt(res[1]));
	        ObjectOutputStream replicaoos = new ObjectOutputStream(replicaSocket.getOutputStream());
	        replicaoos.writeObject(new InitializeMessage(null, null));
	        ObjectInputStream replicaois = new ObjectInputStream(replicaSocket.getInputStream());
	        this.replicaInputStreams.put(id, replicaois);
	        this.replicaOutputStreams.put(id, replicaoos);
	        return true;
    	} catch(Exception e) {
    		System.out.println("connection failed to " + id);
    		e.printStackTrace();
    		return false;
    	}
    }
    
    //private HashMap<Integer, Set<Integer>> topology;
    //private Map<String, String[]> resolver;
    
    //private Map<Integer, Set<Integer>> topology = null;
    //private Map<String, String[]> resolver = null;
    
    public void OracleConnectToAllReplicas(){
    	for (Map.Entry<String, String[]> entry : resolver.entrySet())
    	{
    	    boolean result = OracleConnectToReplica(entry.getKey());
    	    if (!result){
    	    	throw new MessagingException(MessagingException.Type.FAILED_CONNECTION_TO_REPLICA);
    	    }
    	}
    	
    }
    
    
   
}

