package server;

import messaging.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import oracle.Oracle;
import oracle.Oracle.replicaState;

public class Server
{
    private String branchID;
    private String replicaID;
    private Map<AccountNumber, BankAccount> accounts;
    private Messaging m;
    private Set<String> backups; //Set of all replicas excluding self. needs to be init
    private Map<Integer, HashSet<String>> waiting_records; //SerialID to returned backups
    private Map<Integer, RequestClient> waiting_clients = new HashMap<Integer, RequestClient>();
    
    private ConcurrentHashMap<Integer, Set<Integer>> topology;
    private ConcurrentHashMap<String, String[]> resolver;
    private ConcurrentHashMap<String, Oracle.replicaState> replicaStates;
    //private ArrayList<String> branchReplicas;
    //sprivate String currentPrimary;
    
    /**ResponseBackup response = (ResponseBackup)mr;
    RequestClient rc = (RequestClient)response.GetMessage();
    waiting_records.get(rc.GetSerialNumber()).add(response.GetReplica());
    if(waiting_records.get(rc.GetSerialNumber()).equals(this.backups)) {
        waiting_records.remove(rc.GetSerialNumber());
        waiting_clients.remove(rc.GetSerialNumber());
        m.SendToClient(recordTransaction(rc));
    }*/
    
    private void checkWaitingRecords(){
    	for (Map.Entry<Integer, HashSet<String>> entry : waiting_records.entrySet()){
    		if (entry.getValue().equals(this.backups)){
    			RequestClient rc = waiting_clients.get(entry.getKey());
    	        waiting_records.remove(rc.GetSerialNumber());
    	        waiting_clients.remove(rc.GetSerialNumber());
    	        m.SendToClient(recordTransaction(rc));
    		}
    	}
    }
    
    private boolean isHead(String replicaID){
    	ArrayList<String> replicas = new ArrayList<String>();
    	for (Map.Entry<String, String[]> entry : resolver.entrySet())
    	{
    	    if (entry.getKey().substring(0,2).equals(replicaID.substring(0,2))){
    	    	replicas.add(entry.getKey());
    	    }
    	}
    	
    	Collections.sort(replicas,new Comparator<String>() {
            public int compare(String string1, String string2) {
                return string1.substring(3,5).compareTo(string2.substring(3,5));
            }
        });
    	
    	for (String entry : replicas){
    		if (entry.equals(replicaID)){
    			return true;
    		} else if (replicaStates.get(entry) == replicaState.running){
    			return false;
    		}
    	}
    	System.out.println("error in isHead");
    	return false;
    }
    
    private String getHead(String branch){
    	ArrayList<String> replicas = new ArrayList<String>();
    	for (Map.Entry<String, String[]> entry : resolver.entrySet())
    	{
    	    if (entry.getKey().substring(0,2).equals(branch)){
    	    	replicas.add(entry.getKey());
    	    }
    	}
    	
    	Collections.sort(replicas,new Comparator<String>() {
            public int compare(String string1, String string2) {
                return string1.substring(3,5).compareTo(string2.substring(3,5));
            }
        });
    	
    	for (String entry : replicas){
    		if (replicaStates.get(entry) == replicaState.running){
    			return entry;
    		}
    	}
    	System.out.println("error in getHead");
    	return null;
    }
    
    private void removeFromBackups(String replicaID){
    	if (replicaID.substring(0,2).equals(branchID)){
    		backups.remove(replicaID.substring(3,5));
    		System.out.println("removed " + replicaID + " from backups list");
    	}
    }
    
    public void HandleOracleMessage(Message message){
        try {
        	if (message instanceof FailureOracle){
        		FailureOracle fo = (FailureOracle)message;
        		boolean headFailed = isHead(fo.failedReplicaID);
        		System.out.println("got failure from oracle for replica " + fo.failedReplicaID);
        		replicaStates.put(fo.failedReplicaID, replicaState.failed);
        		removeFromBackups(fo.failedReplicaID);
        		if (headFailed){
        			String branchNum = fo.failedReplicaID.substring(0,2);
        			try {
        				m.branchstreams.get(branchNum).close();
        			} catch (Exception e){
        				//ignore
        			}
        			String[] resolverEntry = resolver.get(getHead(fo.failedReplicaID.substring(0,2)));
        			Socket newSocket = new Socket(InetAddress.getByName(resolverEntry[0]), Integer.parseInt(resolverEntry[1]));
        			m.branchstreams.put(branchNum, new ObjectOutputStream(newSocket.getOutputStream()));
        		}
        		backups.remove(fo.failedReplicaID.substring(3,5));
        		checkWaitingRecords();
        	} else if (message instanceof BackupOracle){
        		replicaStates.put(((BackupOracle)message).GetRecoveredReplicaID(), replicaState.running);
        		BackupOracle bo = (BackupOracle)message;
        		System.out.println("got recovery from oracle for replica " + bo.GetRecoveredReplicaID());
        		
        		if(this.replicaID.equals(bo.GetRecoveredReplicaID())) {
                            m.SendToReplica(bo.GetPrimary(), new RecoverReplicaRequest(this.replicaID));
                        }
                        
        		boolean headRecovered = isHead(bo.GetRecoveredReplicaID());
                        if (headRecovered){
        			String branchNum = bo.GetRecoveredReplicaID().substring(0,2);
        			m.branchstreams.get(branchNum).close();
        			String[] resolverEntry = resolver.get(bo.GetRecoveredReplicaID());
        			Socket newSocket = new Socket(InetAddress.getByName(resolverEntry[0]), Integer.parseInt(resolverEntry[1]));
        			m.branchstreams.put(branchNum, new ObjectOutputStream(newSocket.getOutputStream()));
        		}
        	} else {
        		System.out.println("invalid message type received by client from oracle");
        	}
        } catch(Exception e) {
            System.out.println(e.toString() + " thrown from HandleOracleMessages Thread");
            e.printStackTrace();
        }
    }

    public HashSet<String> createBackups(String branchID, String replicaID){
        HashSet<String> output = new HashSet<String>();
        for (Map.Entry<String, String[]> entry : resolver.entrySet()){
            if (entry.getKey().substring(0,2).equals(branchID) && !entry.getKey().substring(3,5).equals(replicaID)){
                System.out.println(entry.getKey());
                output.add(entry.getKey().substring(3,5));
            }
        }
        return output;
    }
    

    public Server(String branchID, String replicaID)
    {
        this.branchID = branchID;
        this.replicaID = replicaID;
        accounts = new ConcurrentHashMap<AccountNumber, BankAccount>();
        this.waiting_records = new HashMap<Integer, HashSet<String>>();

        try {
            m = new Messaging(branchID, replicaID);
            m.makeConnections();
        } catch (MessagingException e) {
            System.out.println("Server failed to create Messaging");
        }

        //init oracle address?
        try {
            this.topology = Messaging.buildTopologyHelper("topology.txt");
            this.resolver = Messaging.buildResolverHelper("resolver.txt");
            this.replicaStates = Oracle.buildReplicaStates(this.resolver);
            this.backups = createBackups(branchID, replicaID);
            //this.branchReplicas = this.buildBranchReplicas();
            //this.initializePrimary();
        } catch (MessagingException e) {
            System.out.println("reading files failed in server");
            e.printStackTrace();
            return;
        }
        
    }

    public DepositResponse deposit(int accountID, float amount, int serialNumber)
    {
        return getAccount(accountID).deposit(amount, serialNumber);
    }

    public WithdrawResponse withdraw(int accountID, float amount, int serialNumber)
    {
        return getAccount(accountID).withdraw(amount, serialNumber);
    }

    public QueryResponse query(int accountID, int serialNumber)
    {
        return getAccount(accountID).query(serialNumber);
    }

    public TransferResponse transferWithdraw(int accountID, float amount, int serialNumber)
    {
        return getAccount(accountID).transferWithdraw(amount, serialNumber);
    }

    public void transferDeposit(int accountID, float amount, int serialNumber)
    {
        getAccount(accountID).transferDeposit(amount, serialNumber);
    }

    public BankAccount getAccount(int accountID)
    {
        AccountNumber accountNumber = new AccountNumber(branchID, accountID);

        if (!accounts.containsKey(accountNumber)) {
            BankAccount bankAccount = new BankAccount(accountNumber);
            accounts.put(accountNumber, bankAccount);
            return bankAccount;
        }

        return accounts.get(accountNumber);
    }

    public Set<BankAccount> getBranchState()
    {
        Set<BankAccount> branchState = new HashSet<BankAccount>();
        for (BankAccount ba : accounts.values()) {
            if (ba.getBalance() > 0.0f) {
                branchState.add(ba);
                System.out.println("adding bankacocunt with " + ba.getBalance() + " balance for getBranchState");
            }
        }

        System.out.println("Size of set " + branchState.size());
        return branchState;
    }

    public void startBackup(RequestClient rc) {
        if (this.backups.size() == 0){
        	m.SendToClient(recordTransaction(rc));
        	return;
        }
        waiting_records.put(rc.GetSerialNumber(), new HashSet<String>());
        waiting_clients.put(rc.GetSerialNumber(), rc);
        for(String i : this.backups) {
            m.SendToReplica(i, new RequestBackup(this.replicaID, rc));
        }
    }

    //Add to our hashtable of completed transactions
    public ResponseClient recordTransaction(Message rc) {
        if (rc instanceof DepositRequest) {
                System.out.println("Deposit Request received");
                DepositRequest request = (DepositRequest) rc;
                return deposit(request.GetAcnt(), request.GetAmt(), request.GetSerialNumber());

            } else if (rc instanceof WithdrawRequest) {
                System.out.println("Withdraw Request received");
                WithdrawRequest request = (WithdrawRequest) rc;
                return withdraw(request.GetAcnt(), request.GetAmt(), request.GetSerialNumber());

            } else if (rc instanceof QueryRequest) {
                System.out.println("Query Request received");
                QueryRequest request = (QueryRequest) rc;
                return query(request.GetAcnt(), request.GetSerialNumber());

            } else if (rc instanceof TransferRequest) {
                System.out.println("Transfer Request received");
                TransferRequest request = (TransferRequest) rc;
                if (request.GetDestBranch().equals(branchID)) {
                    deposit(request.GetDestAcnt(), request.GetAmt(), request.GetSerialNumber());
                } else {
                    withdraw(request.GetSrcAcnt(), request.GetAmt(), request.GetSerialNumber());
                    System.out.println("Sending request to second account");
                    m.SendToBranch(request.GetDestBranch(), new TransferBranch(request.GetDestAcnt(), request.GetAmt(), request.GetSerialNumber()));
                }
                System.out.println("Transfer Request recorded");

            } else if (rc instanceof TransferBranch) {
                System.out.println("DepositFromTransfer Request received");
                TransferBranch request = (TransferBranch) rc;
                transferDeposit(request.GetAcnt(), request.GetAmt(), request.GetSerialNumber());
                System.out.println("DepsitFromTransfer Request recorded");
            }

        return null;
    }

    public void run()
    {
        System.out.println("Server starting up!");
        while (true) {
            Message mr = m.ReceiveMessage();
            System.out.println("Got message");

            if (mr instanceof RequestClient) { //from client
                System.out.println("Received message from client");
                startBackup((RequestClient)mr);
           
            } else if (mr instanceof BranchMessage) {
                if(mr instanceof TransferBranch)
                    startBackup((RequestClient)mr);

            } else if (mr instanceof RequestBackup) { //from primary
                System.out.println("Received backup request");
                recordTransaction(((RequestBackup)mr).GetMessage());
                m.SendToReplica(mr.GetReplica(), new ResponseBackup(this.replicaID, ((RequestBackup)mr).GetMessage()));
           
            } else if (mr instanceof ResponseBackup) { //from backup
                System.out.println("Received backup response");
                ResponseBackup response = (ResponseBackup)mr;
                RequestClient rc = (RequestClient)response.GetMessage();
                waiting_records.get(rc.GetSerialNumber()).add(response.GetReplica());
                if(waiting_records.get(rc.GetSerialNumber()).equals(this.backups)) {
                    waiting_records.remove(rc.GetSerialNumber());
                    waiting_clients.remove(rc.GetSerialNumber());
                    m.SendToClient(recordTransaction(rc));
                }
            
            } else if (mr instanceof OracleMessage) {
                OracleMessage m = (OracleMessage) mr;
                HandleOracleMessage(m);
            
            } else if (mr instanceof RecoverReplicaRequest) {
                RecoverReplicaRequest r = (RecoverReplicaRequest)mr;
                m.SendToReplica(mr.GetReplica(), new RecoverReplicaResponse(this.backups, this.accounts, this.waiting_clients));

            } else if (mr instanceof RecoverReplicaResponse) {
                RecoverReplicaResponse r = (RecoverReplicaResponse)mr;
                this.backups = r.GetBackups();
                this.accounts = r.GetBankAccounts();
                for(RequestClient rc : r.GetWaitingClients().values()) {
                    recordTransaction(rc);
                    m.SendToReplica(r.GetReplica(), new ResponseBackup(this.replicaID, rc));
                }

            } else
                System.out.println("Don't know how to handle message");
        }
    }

    public static void main(String args[])
    {
        new Server(args[0], args[1]).run();
    }
}
