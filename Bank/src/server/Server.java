package server;

import messaging.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.TreeSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Server
{
    private int branchID;
    private int replicaID;
    private ConcurrentHashMap<AccountNumber, BankAccount> accounts;
    private Messaging m;
    private HashSet<Integer> backups; //Set of all backups
    private HashMap<Integer, HashSet<Integer>> waiting_records; //SerialID to returned backups

    public Server(int branchID, int replicaID)
    {
        this.branchID = branchID;
        this.replicaID = replicaID;
        accounts = new ConcurrentHashMap<AccountNumber, BankAccount>();

        try {
            m = new Messaging(branchID, replicaID);
            m.makeConnections();
        } catch (MessagingException e) {
            System.out.println("Server failed to create Messaging");
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
        waiting_records.put(rc.GetSerialNumber(), new HashSet<Integer>());
        for(Integer i : this.backups) {
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

            if (mr instanceof RequestClient) {
                startBackup((RequestClient)mr);
           
            } else if (mr instanceof BranchMessage) {
                if(mr instanceof TransferBranch)
                    startBackup((RequestClient)mr);

            } else if (mr instanceof RequestBackup) {
                recordTransaction(((RequestBackup)mr).GetMessage());
                m.SendToReplica(mr.GetReplica(), new ResponseBackup(this.replicaID, mr));
           
            } else if (mr instanceof ResponseBackup) {
                ResponseBackup response = (ResponseBackup)mr;
                RequestClient rc = (RequestClient)response.GetMessage();
                waiting_records.get(rc.GetSerialNumber()).add(response.GetReplica());
                if(waiting_records.get(rc.GetSerialNumber()).equals(this.backups)) {
                    waiting_records.remove(rc.GetSerialNumber());
                    m.SendToClient(recordTransaction(rc));
                }
            
            } else if (mr instanceof OracleMessage) { //TODO
                OracleMessage m = (OracleMessage) mr;
            }
        }
    }

    public static void main(String args[])
    {
        new Server(new Integer(args[0]), new Integer(args[1])).run();
    }
}
