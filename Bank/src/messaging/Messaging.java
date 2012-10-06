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
    private ServerSocket serversocket = null;
    private Map<Integer, Socket> sockets = null;

    private BlockingQueue<MessageRequest> messageBuffer; 

    public enum Type {
        CLIENT,
            SERVER
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

    public void connectToServer() throws MessagingException {
        try {
            String[] res = this.resolver.get(this.branch);
            this.clientsocket = new Socket(InetAddress.getByName(res[0]), Integer.parseInt(res[1]));
            this.clientsocket.setSoTimeout(5 * 1000);
        } catch (UnknownHostException e) {
            throw new MessagingException(MessagingException.Type.UNKNOWN_HOST);
        } catch(IOException e) {
            throw new MessagingException(MessagingException.Type.FAILED_SOCKET_CREATION);
        }
    }

    private MessageResponse sendRequest(MessageRequest M) throws MessagingException {
        try {
            ObjectOutputStream o = new ObjectOutputStream(this.clientsocket.getOutputStream());
            ObjectInputStream i = new ObjectInputStream(this.clientsocket.getInputStream());

            o.writeObject(M);
            MessageResponse response = (MessageResponse)i.readObject();

            i.close();
            o.close();

            return response; 
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
    //End Client Methods


    //Server Methods
    public void makeConnections() throws MessagingException {
        //TODO:start_thread(listen_for_connections_and_spawn_threads);
        for(Integer branch : this.topology.get(this.branch)) {
            try {
                String[] res = this.resolver.get(this.branch);
                Socket socket = new Socket(InetAddress.getByName(res[1]), Integer.parseInt(res[2]));
                socket.setSoTimeout(5 * 1000);
                this.sockets.put(branch, socket);
            } catch(IOException e) {
                throw new MessagingException(MessagingException.Type.FAILED_SOCKET_CREATION);
            }
        }
    }

    public MessageRequest ReceiveMessage() throws MessagingException {
        try {
            return this.messageBuffer.take();
        } catch(java.lang.InterruptedException e) {
            throw new MessagingException(MessagingException.Type.FAILED_SYNC_BUFFER);
        }
    }

    //For responding to client
    public void SendResponse(MessageResponse M) throws MessagingException {
        try {
            ObjectOutputStream o = new ObjectOutputStream(this.clientsocket.getOutputStream());
            ObjectInputStream i = new ObjectInputStream(this.clientsocket.getInputStream());

            o.writeObject(M);

            i.close();
            o.close();
        } catch (IOException e) {
            throw new MessagingException(MessagingException.Type.FAILED_RESPONSE_SEND);
        }
    }

    public void FinishTransfer(Integer branch, Integer acnt, Float amt, Integer ser_number) throws MessagingException {
        try {
            ObjectOutputStream o = new ObjectOutputStream(this.sockets.get(branch).getOutputStream());
            o.writeObject(new DepositRequest(acnt, amt, ser_number));
            o.close();

        } catch (IOException e) {
            throw new MessagingException(MessagingException.Type.FAILED_REQUEST_SEND);
        }
    }
    //End Server Methods

}
