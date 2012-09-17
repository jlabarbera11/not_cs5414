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
import java.io.File;
import java.io.FileNotFoundException;

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
    
    private void buildTopology() throws MessagingException {
        this.topology = new HashMap<Integer, Set<Integer>>();
        try {
	        Scanner scanner = new Scanner(new File("topology.txt"));
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
        
    }
    
    private String[] resolveBranch(Integer b) throws MessagingException {
        try {

            Scanner scanner = new Scanner(new File("resolver.txt"));
            while (scanner.hasNextLine()) {
                String[] branch = scanner.nextLine().split(" ");
                if (Integer.parseInt(branch[0]) == b)
                    return branch;
            }
            throw new MessagingException(MessagingException.Type.UNRECOGNIZED_BRANCH);
        } catch (MessagingException e) {
            throw e;
        } catch (FileNotFoundException e) {
            throw new MessagingException(MessagingException.Type.FILE_NOT_FOUND);
        } 
    }

    public Messaging(Integer b, Type T) throws MessagingException {
        buildTopology();
        
        this.branch = b;
        try {
            String[] res = resolveBranch(this.branch);
            this.serverHost = InetAddress.getByName(res[1]);
            this.serverPort = Integer.parseInt(res[2]);
        } catch (MessagingException e) {
            throw e;
        } catch (UnknownHostException e) {
            throw new MessagingException(MessagingException.Type.UNKNOWN_HOST);
        }

        if (T == Type.SERVER) {
            try {
                this.serversocket = new ServerSocket(this.serverPort);
                serversocket.setReuseAddress(true);
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

    public DepositResponse Deposit(Integer branch, Integer acnt, Float amt, Integer ser_number) throws MessagingException {
        if (branch.compareTo(this.branch) != 0)
            return new DepositResponse("Cannot desposit to this branch");

        return (DepositResponse)sendRequest(new DepositRequest(acnt, amt, ser_number));
    }

    public DepositFromTransferResponse DepositFromTransfer(Integer dest_branch, Integer dest_acnt, Float amt, Integer ser_number) throws MessagingException {
        if (!topology.get(this.branch).contains(dest_branch))
            return new DepositFromTransferResponse("Cannot transfer money to this branch");

        return (DepositFromTransferResponse)sendRequestFromServer(new DepositFromTransferRequest(dest_acnt, amt, ser_number), dest_branch);
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


    //Server Methods
    private MessageResponse sendRequestFromServer(MessageRequest M, Integer branch) throws MessagingException {

        try {
            String[] res = resolveBranch(branch);
            Socket socket = new Socket(InetAddress.getByName(res[1]), Integer.parseInt(res[2]));
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
