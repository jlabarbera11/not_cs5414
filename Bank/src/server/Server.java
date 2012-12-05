package server;

import messaging.*;
import messaging.Messaging.replicaState;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import jvm.Pinger;

public class Server extends Thread
{
	private volatile boolean running = true;
    private int branchID;
    private int replicaID;
    private Map<AccountNumber, BankAccount> accounts;
    private Messaging messaging;
    private ConcurrentLinkedQueue<Integer> backups = new ConcurrentLinkedQueue<Integer>(); //Set of all replicas with greater replica IDs
    private Map<Integer, ConcurrentLinkedQueue<Integer>> waiting_records = new ConcurrentHashMap<Integer, ConcurrentLinkedQueue<Integer>>();; //SerialID to returned backups
    private Map<Integer, RequestClient> waiting_clients = new ConcurrentHashMap<Integer, RequestClient>();
    ServerSocket serversocket;
    Pinger pinger;
    public boolean debug = false;
    
    public void printIfDebug(String s){
    	if (debug){
    		System.out.println(s);
    	}
    }

    boolean isPrimary = false;
    
    public void kill(){
    	running = false;
    	pinger.kill();
    	System.out.println("server " + branchID + "." + replicaID + " is shutting down...");
    	try {
			serversocket.close();
		} catch (IOException e) {
			//do nothing
		}
    }

    public Server(int branchID, int replicaID)
    {
        this.branchID = branchID;
        this.replicaID = replicaID;
        accounts = new ConcurrentHashMap<AccountNumber, BankAccount>();
        messaging = new Messaging();
        this.backups = messaging.initBackups(branchID, replicaID);

        //init serversocket
    	ReplicaInfo myInfo = messaging.getReplicaInfo(new ReplicaID(branchID, replicaID));
		try {
			serversocket = new ServerSocket(myInfo.port);
			serversocket.setReuseAddress(true);
		} catch (IOException e) {
			e.printStackTrace();
		}

		//start pinger
		ReplicaID myReplicaID = new ReplicaID(branchID, replicaID);
		pinger = new Pinger(messaging.getJvmID(myReplicaID), myReplicaID);
		pinger.start();

    	System.out.println("server listening on port " + myInfo.port);

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
                printIfDebug("adding bankacocunt with " + ba.getBalance() + " balance for getBranchState");
            }
        }

        return branchState;
    }
    /**
     * This class is used to handle the case where a primary checks the status of all backups, sends a transaction to them,
     * and then a backup fails. Without this class, the transaction would not succeed at the primary, but would at some of
     * the backups. This class wakes up periodically and, if the transaction has not terminated, checks all backup statuses.
     * @author Ben
     *
     */
    private class CheckBackupStatusThread extends Thread {

        private RequestClient rc;

    	public CheckBackupStatusThread(RequestClient rc){
    		this.rc = rc;
    	}

      public void run(){
        for (int i =0; i<5; i++){
        	
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          printIfDebug("backupstatusthread woke up, checking replica status");
          
          Object waiting_record = waiting_records.get(rc.GetSerialNumber());
          if (waiting_record == null){
        	  //transaction has already terminated
        	  printIfDebug("transaction has already terminated");
        	  return;
          }
          
          replicaState status = null;
    	  synchronized(backups){
	          for(Integer replicaNum : backups) {
	            try {
	              status = messaging.checkReplicaStatus(new ReplicaID(branchID, replicaNum));
	            } catch (MessagingException e) {
	              e.printStackTrace();
	            }
	            if (status != replicaState.running){
	              backups.remove(replicaNum);
	              /**We can't record the JVM failure in messaging because messaging is not thread-safe
	               * This is fine because the server will check backup status before starting the next backup
	               */
	              //messaging.recordJvmFailure(new ReplicaID(branchID, replicaNum));
	              printIfDebug("replica " + replicaNum.toString() + " has failed, removing from backups");
	              
	              if (concurrentEquals(waiting_records.get(rc.GetSerialNumber()), backups)){
	            	printIfDebug("CheckBackupStatusThread found that all backups have responded, sending response");
	                waiting_records.remove(rc.GetSerialNumber());
	                waiting_clients.remove(rc.GetSerialNumber());
	                ResponseClient responseClient = recordTransaction(rc);
	                if(!(rc instanceof TransferDepositToRemoteBranch)){
	                  try {
	                    messaging.sendToClientNoResponse(branchID, responseClient);
	                  } catch (MessagingException e) {
	                    e.printStackTrace();
	                  }
	                }
	                if(rc instanceof TransferRequest) {
	                  TransferRequest request = (TransferRequest)rc;
	                  if(request.GetDestBranch() != branchID){
	                    //m.SendToBranch(getHead(request.GetDestBranch()),
	                	printIfDebug("about to send to transfer recipient branch");
	                    try {
	                      messaging.sendToPrimaryNoResponse(request.GetDestBranch(), new TransferDepositToRemoteBranch(request.GetDestAcnt(), request.GetAmt(), request.GetSerialNumber()));
	                    } catch (MessagingException e) {
	                      e.printStackTrace();
	                    }
	                  }
	                }
	
	              }
	
	             }
		            
	          }
    	  }
        }
      }
    }

    public void startBackup(RequestClient rc) {
      //update backups
      synchronized(backups){
	      for(Integer replicaNum : this.backups) {
	        //check replica status
	        Messaging.replicaState status = null;
	        try {
	          status = messaging.checkReplicaStatus(new ReplicaID(branchID, replicaNum));
	        } catch (MessagingException e) {
	          printIfDebug("error starting backup");
	          //e.printStackTrace();
	        }
	        if (status == replicaState.running){
	          printIfDebug("state of replica " + replicaNum + " is *running*");
	        } else {
	          printIfDebug("replica " + replicaNum + " is not running, removing from backups");
	          backups.remove(replicaNum);
	          messaging.recordJvmFailure(new ReplicaID(branchID, replicaNum));
	        }
	      }
      }

      if (this.backups.size() == 0){
        try {
          ResponseClient response = recordTransaction(rc);
          if (!(rc instanceof TransferDepositToRemoteBranch)){
        	System.out.println("Transaction complete!");
            messaging.sendToClientNoResponse(branchID, response);
          }
          if (rc instanceof TransferRequest){
            TransferRequest request = (TransferRequest)rc;
            messaging.sendToPrimaryNoResponse(request.GetDestBranch(), new TransferDepositToRemoteBranch(request.GetDestAcnt(), request.GetAmt(), request.GetSerialNumber()));
          }
        } catch (MessagingException e) {
          e.printStackTrace();
        }
        return;
      }
      waiting_records.put(rc.GetSerialNumber(), new ConcurrentLinkedQueue<Integer>());
      waiting_clients.put(rc.GetSerialNumber(), rc);
      synchronized(backups){
	      for(Integer replicaNum : this.backups) {
	        //m.SendToReplica(i, new RequestBackup(this.replicaID, rc));
	        try {
	          //in a RequestBackup, the branchID and replicaID are of the sender
	          messaging.sendToReplicaNoResponse(new ReplicaID(branchID, replicaNum), new RequestBackup(branchID, replicaID, rc));
	        } catch (MessagingException e) {
	          printIfDebug("error sending to replica during backup init");
	        }
	      }
      }
      CheckBackupStatusThread statusThread = new CheckBackupStatusThread(rc);
      statusThread.start();
    }

    //Add to our hashtable of completed transactions
    public ResponseClient recordTransaction(Message rc) {
        if (rc instanceof DepositRequest) {
        		printIfDebug("MESSAGE: Deposit Request received");
                DepositRequest request = (DepositRequest) rc;
                return deposit(request.GetAcnt(), request.GetAmt(), request.GetSerialNumber());

            } else if (rc instanceof WithdrawRequest) {
            	printIfDebug("MESSAGE: Withdraw Request received");
                WithdrawRequest request = (WithdrawRequest) rc;
                return withdraw(request.GetAcnt(), request.GetAmt(), request.GetSerialNumber());

            } else if (rc instanceof QueryRequest) {
            	printIfDebug("MESSAGE: Query Request received");
                QueryRequest request = (QueryRequest) rc;
                return query(request.GetAcnt(), request.GetSerialNumber());

            } else if (rc instanceof TransferRequest) {
            	printIfDebug("MESSAGE: Transfer Request received");
                TransferRequest request = (TransferRequest) rc;
                if (request.GetDestBranch() == branchID){
                    transferDeposit(request.GetDestAcnt(), request.GetAmt(), request.GetSerialNumber());
                }
                return transferWithdraw(request.GetSrcAcnt(), request.GetAmt(), request.GetSerialNumber());

            } else if (rc instanceof TransferDepositToRemoteBranch) {
            	printIfDebug("MESSAGE: TransferDepositToRemoteBranch Request received");
                TransferDepositToRemoteBranch request = (TransferDepositToRemoteBranch) rc;
                transferDeposit(request.GetAcnt(), request.GetAmt(), request.GetSerialNumber());
                printIfDebug("DepsitFromTransfer Request recorded");
            }

        return null;
    }

    //assumes that everything is a message
    public Message receiveMessage() throws IOException, ClassNotFoundException{
        Socket clientSocket = serversocket.accept();
        printIfDebug("server accepted connection");
        ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
        Message message = (Message)ois.readObject();
        clientSocket.close();
        return message;
    }

    public void run()
    {
        while (running) {
        	try {
        		Message mr = receiveMessage();

	            if (mr instanceof RequestClient) { //from client
	                if (!isPrimary){
	                	isPrimary = true;
	                	printIfDebug("I am now the head, recording failures of previous primaries");
	                	messaging.recordPreviousPrimaryFailures(branchID, replicaID);
	                }
	                startBackup((RequestClient)mr);

	            } else if (mr instanceof BranchMessage) {

	            } else if (mr instanceof RequestBackup) { //from primary
	            	printIfDebug("MESSAGE: Received backup request");
	                recordTransaction(((RequestBackup)mr).GetMessage());
	                //m.SendToReplica(mr.GetReplica(), new ResponseBackup(this.replicaID, ((RequestBackup)mr).GetMessage()));
	                messaging.sendToReplicaNoResponse(new ReplicaID(mr.GetBranch(), mr.GetReplica()), new ResponseBackup(this.replicaID, ((RequestBackup)mr).GetMessage()));
	            } else if (mr instanceof ResponseBackup) { //from backup
	            	printIfDebug("MESSAGE: Received backup response"); //TODO
	                ResponseBackup response = (ResponseBackup)mr;
	                RequestClient rc = (RequestClient)response.GetMessage();
	                waiting_records.get(rc.GetSerialNumber()).add(response.GetReplica());
	                if (concurrentEquals(waiting_records.get(rc.GetSerialNumber()), this.backups)){
	                    waiting_records.remove(rc.GetSerialNumber());
	                    waiting_clients.remove(rc.GetSerialNumber());
	                    ResponseClient responseClient = recordTransaction(rc);
	                    if(!(rc instanceof TransferDepositToRemoteBranch)){
	                    	System.out.println("Transaction complete!");
	                    	messaging.sendToClientNoResponse(branchID, responseClient);
	                    }
	                    if(rc instanceof TransferRequest) {
	                        TransferRequest request = (TransferRequest)rc;
	                        if(request.GetDestBranch() != this.branchID){
	                            //m.SendToBranch(getHead(request.GetDestBranch()),
	                        	printIfDebug("about to send to transfer recipient branch");
	                            messaging.sendToPrimaryNoResponse(request.GetDestBranch(), new TransferDepositToRemoteBranch(request.GetDestAcnt(), request.GetAmt(), request.GetSerialNumber()));
	                        }
	                    }
	                }

	            } else {
	            	printIfDebug("Don't know how to handle message");
	            }
        	} catch (Exception e){
        		printIfDebug("error in server main loop");
        	}
        }
        printIfDebug("Server has quit!");
    }

    private boolean concurrentEquals(ConcurrentLinkedQueue<Integer> queue1, ConcurrentLinkedQueue<Integer> queue2) {
		int size = queue2.size();
    	for (Integer int1 : queue1){
			if (queue2.contains(int1)){
				size--;
			}
		}
		return (size<=0);
	}

	public static void main(String args[])
    {
        new Server(Integer.parseInt(args[0]), Integer.parseInt(args[1])).run();
    }
}
