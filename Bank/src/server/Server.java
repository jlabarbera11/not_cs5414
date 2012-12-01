package server;

import messaging.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
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
    private int branchID;
    private int replicaID;
    private Map<AccountNumber, BankAccount> accounts;
    private NewMessaging newMessaging;
    private Set<Integer> backups; //Set of all replicas excluding self. needs to be init
    private Map<Integer, HashSet<Integer>> waiting_records; //SerialID to returned backups
    private Map<Integer, RequestClient> waiting_clients = new HashMap<Integer, RequestClient>();
    
    private void checkWaitingRecords(){
        for (Map.Entry<Integer, HashSet<Integer>> entry : waiting_records.entrySet()){
            if (entry.getValue().equals(this.backups)){
                RequestClient rc = waiting_clients.get(entry.getKey());
                waiting_records.remove(rc.GetSerialNumber());
                waiting_clients.remove(rc.GetSerialNumber());
                //m.SendToClient(recordTransaction(rc));
                try {
					newMessaging.sendToClientNoResponse(branchID, recordTransaction(rc));
				} catch (MessagingException e) {
					e.printStackTrace();
				}
            }
        }
    }
    
    private void removeFromBackups(String replicaID){
    	if (replicaID.substring(0,2).equals(branchID)){
    		backups.remove(replicaID.substring(3,5));
    		System.out.println("removed " + replicaID + " from backups list");
    	}
    }
    
    //TODO: fix this oracle shit!
    
    public void HandleOracleMessage(Message message){
        /**try {
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
        		System.out.println("old primary is " + bo.GetPrimary());
        		System.out.println("my replicaID is " + replicaID + " and received recovering replicaId is " + bo.GetRecoveredReplicaID());
        		if((this.branchID + "." + this.replicaID).equals(bo.GetRecoveredReplicaID())) {
        			System.out.println("starting recovery");
                                m.SendToReplica(bo.GetPrimary().substring(3,5), new RecoverReplicaRequest(this.replicaID));
                        }
                        else
                            this.backups.add(bo.GetRecoveredReplicaID().substring(3,5));
                    
        		boolean headRecovered = isHead(bo.GetRecoveredReplicaID());
                        if (headRecovered){
        			String branchNum = bo.GetRecoveredReplicaID().substring(0,2);
        			try {
        				m.branchstreams.get(branchNum).close();
        			} catch (Exception e ){
        				//ignore
        			}
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
        }*/
    }
    

    public Server(int branchID, int replicaID)
    {
        this.branchID = branchID;
        this.replicaID = replicaID;
        accounts = new ConcurrentHashMap<AccountNumber, BankAccount>();
        this.waiting_records = new HashMap<Integer, HashSet<Integer>>();

        newMessaging = new NewMessaging();
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
        if (this.backups.size() == 0 && !(rc instanceof TransferBranch)){
        	//m.SendToClient(recordTransaction(rc));
        	try {
				newMessaging.sendToClientNoResponse(branchID, recordTransaction(rc));
			} catch (MessagingException e) {
				e.printStackTrace();
			}
        	return;
        }
        waiting_records.put(rc.GetSerialNumber(), new HashSet<Integer>());
        waiting_clients.put(rc.GetSerialNumber(), rc);
        for(Integer i : this.backups) {
            //m.SendToReplica(i, new RequestBackup(this.replicaID, rc));
            try {
				newMessaging.sendToReplicaNoResponse(new ReplicaID(branchID, replicaID), new RequestBackup(replicaID, rc));
			} catch (MessagingException e) {
				e.printStackTrace();
			}
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
                if (request.GetDestBranch() == branchID)
                    transferDeposit(request.GetDestAcnt(), request.GetAmt(), request.GetSerialNumber());
                return transferWithdraw(request.GetSrcAcnt(), request.GetAmt(), request.GetSerialNumber());

            } else if (rc instanceof TransferBranch) {
                System.out.println("DepositFromTransfer Request received");
                TransferBranch request = (TransferBranch) rc;
                transferDeposit(request.GetAcnt(), request.GetAmt(), request.GetSerialNumber());
                System.out.println("DepsitFromTransfer Request recorded");
            }

        return null;
    }
    
    //assumes that everything is a message
    public Message receiveMessage() throws IOException, ClassNotFoundException{
    	ReplicaInfo myInfo = newMessaging.getReplicaInfo(new ReplicaID(branchID, replicaID));
    	ServerSocket serversocket = new ServerSocket(myInfo.port);
        Socket clientSocket = serversocket.accept();
        ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
        Message message = (Message)ois.readObject();
        serversocket.close();
        return message;
    }

    public void run()
    {
        System.out.println("Server starting up!");
        while (true) {
        	try {
	        	Message mr = receiveMessage();
	            System.out.println("Got message");
	
	            if (mr instanceof RequestClient) { //from client
	                System.out.println("Received message from client");
	                startBackup((RequestClient)mr);
	           
	            } else if (mr instanceof BranchMessage) {
	
	            } else if (mr instanceof RequestBackup) { //from primary
	                System.out.println("Received backup request");
	                recordTransaction(((RequestBackup)mr).GetMessage());
	                //m.SendToReplica(mr.GetReplica(), new ResponseBackup(this.replicaID, ((RequestBackup)mr).GetMessage()));
	                newMessaging.sendToReplicaNoResponse(new ReplicaID(mr.GetBranch(), mr.GetReplica()), new ResponseBackup(this.replicaID, ((RequestBackup)mr).GetMessage()));
	           
	            } else if (mr instanceof ResponseBackup) { //from backup
	                System.out.println("Received backup response");
	                ResponseBackup response = (ResponseBackup)mr;
	                RequestClient rc = (RequestClient)response.GetMessage();
	                waiting_records.get(rc.GetSerialNumber()).add(response.GetReplica());
	                if(waiting_records.get(rc.GetSerialNumber()).equals(this.backups)) {
	                    waiting_records.remove(rc.GetSerialNumber());
	                    waiting_clients.remove(rc.GetSerialNumber());
	                    if(!(rc instanceof TransferBranch)){
	                        //m.SendToClient(recordTransaction(rc));
	                    	newMessaging.sendToClientNoResponse(branchID, recordTransaction(rc));
	                    }
	                    if(rc instanceof TransferRequest) {
	                        TransferRequest request = (TransferRequest)rc;
	                        if(request.GetDestBranch() != this.branchID){
	                            //m.SendToBranch(getHead(request.GetDestBranch()), 
	                            newMessaging.sendToPrimaryNoResponse(request.GetDestBranch(), new TransferBranch(request.GetDestAcnt(), request.GetAmt(), request.GetSerialNumber()));
	                        }
	                    }
	                }
	            
	            } else if (mr instanceof OracleMessage) {
	                OracleMessage m = (OracleMessage) mr;
	                HandleOracleMessage(m);
	            
	            } else if (mr instanceof RecoverReplicaRequest) {
	                RecoverReplicaRequest r = (RecoverReplicaRequest)mr;
	                //m.SendToReplica(mr.GetReplica(), new RecoverReplicaResponse(this.backups, this.accounts, this.waiting_clients));
	                newMessaging.sendToReplicaNoResponse(new ReplicaID(mr.GetBranch(), mr.GetReplica()), new RecoverReplicaResponse(this.backups, this.accounts, this.waiting_clients));
	            } else if (mr instanceof RecoverReplicaResponse) {
	                RecoverReplicaResponse r = (RecoverReplicaResponse)mr;
	                this.backups = r.GetBackups();
	                this.accounts = r.GetBankAccounts();
	                for(RequestClient rc : r.GetWaitingClients().values()) {
	                    recordTransaction(rc);
	                    //m.SendToReplica(r.GetReplica(), new ResponseBackup(this.replicaID, rc));
	                    newMessaging.sendToReplicaNoResponse(new ReplicaID(r.GetBranch(), r.GetReplica()),new ResponseBackup(this.replicaID, rc));
	                }
	
	            } else {
	                System.out.println("Don't know how to handle message");
	            }
        	} catch (Exception e){
        		System.out.println("error in server main loop");
        		e.printStackTrace();
        	}
        }
    }

    public static void main(String args[])
    {
        new Server(Integer.parseInt(args[0]), Integer.parseInt(args[1])).run();
    }
}
