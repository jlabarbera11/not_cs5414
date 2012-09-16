package messaging;

import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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

    private String[] resolveBranch() {
        Scanner scanner = new Scanner("resolver.txt");
        while (scanner.hasNextLine()) {
            String[] branch = scanner.nextLine().split(" ");
            if (Integer.parseInt(branch[0]) == this.branch) {
                this.serverHost = InetAddress.getByName(branch[1]);
                this.serverPort = Integer.parseInt(branch[2]);
            }
        }
    }

    public void Messaging(Integer b, Type T) throws MessagingException {
        buildTopology();
        if (!topology.containsKey(b)) {
            throw new MessagingException(MessagingException.Type.INVALID_BRANCH_DECLARATION);
        }
        
        this.branch = b;
        resolveBranch();

        if (T == Type.SERVER) {
            this.serversocket = new ServerSocket(this.serverPort);
        }
    }

    private Message sendRequest(Message M) {
        Socket socket = new Socket(this.serverHost, this.serverPort);
        socket.setSoTimeout(5 * 1000);

        ObjectOutputStream o = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream i = new ObjectInputStream(socket.getInputStream());

        o.writeObject(M);
        Message resp = (Message)i.readObject();

        i.close();
        o.close();
        socket.close();

        return resp;
    }

    public DepositResponse Deposit(Integer acnt, Float amt, Integer ser_number) {
        if (branch != this.branch)
            throw new MessagingException(MessagingException.Type.INVALID_DEST_BRANCH);

        return (DespositResponse)SendRequest(new DespositRequest(acnt, amt, ser_number));
    }

    public WithdrawResponse Withdraw(Integer branch, Integer acnt, float amt, float ser_number) {
        if (branch != this.branch)
            throw new MessagingException(MessagingException.Type.INVALID_DEST_BRANCH);

        Socket socket = new Socket(this.serverHost, this.serverPort);
        return SendRequest(new WithdrawRequest(acnt, amt, ser_number));
    }

    public QueryResponse Query(Integer branch, Integer acnt, Integer ser_number) {
        if (branch != this.branch)
            throw new MessagingException(MessagingException.Type.INVALID_DEST_BRANCH);

        return SendRequest(new DespositRequest(acnt, amt, ser_number));
    }

    public TranseferResponse Transfer(Integer src_branch, Integer src_acnt, Integer dest_branch, Integer dest_act, float amt, float ser_number) {
        if (this.branch != src_branch)
            throw new MessagingException(MessagingException.Type.INVALID_SRC_BRANCH);
        if (src_branch != dest_branch && !topology.get(src_branch).contains(dest_branch))
            throw new MessagingException(MessagingException.Type.INVALID_DEST_BRANCH);

        return SendRequest(new TransferRequest(src_acnt, dest_acnt, amt, ser_number));
    }


    //Server Methods
    public Message ReceiveMessage(Message M) {
        this.clientsocket = serversocket.accept();
        this.oos = new ObjectOutputStream(this.clientsocket.getOutputStream());
        this.ois = new ObjectInputStream(this.clientsocket.getInputStream());

        return (Message)this.ois.readObject();
    }

    public Message SendResponse(Message M) {
        this.oos.writeObject(M);

        this.ois.close();
        this.oos.close();
        this.clientsocket.close();
    }

}
