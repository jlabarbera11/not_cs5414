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
        for(Integer i : this.backups)
            m.SendToReplica(i, rc);
    }

    //Add to our hashtable of completed transactions
    public ResponseClient recordTransaction(Message rc) {
        if (rc instanceof DepositRequest) {
                System.out.println("Deposit Request received");
                DepositRequest request = (DepositRequest) rc;
                return deposit(request.getAcnt(), request.getAmt(), request.getSerNumber());

            } else if (rc instanceof WithdrawRequest) {
                System.out.println("Withdraw Request received");
                WithdrawRequest request = (WithdrawRequest) rc;
                return withdraw(request.getAcnt(), request.getAmt(), request.getSerNumber());

            } else if (rc instanceof QueryRequest) {
                System.out.println("Query Request received");
                QueryRequest request = (QueryRequest) rc;
                return query(request.getAcnt(), request.getSerNumber());

            } else if (rc instanceof TransferRequest) {
                System.out.println("Transfer Request received");
                TransferRequest request = (TransferRequest) rc;
                if (request.getDestBranch().equals(branchID)) {
                    deposit(request.getDestAcnt(), request.getAmt(), request.getSerNumber());
                    return null;
                } else {
                    withdraw(request.getSrcAcnt(), request.getAmt(), request.getSerNumber());
                    System.out.println("Sending request to second account");
                    try {
                        m.SendToBranch(request.GetDestBranch(), new TransferBranch(request.GetDestAcnt(), request.GetAmt(), request.GetSerialNumber()));
                    } catch (MessagingException e) {
                        System.out.println("Source branch could not send Destination branch deposit");
                    }
                }
                System.out.println("Transfer Request recorded");

            } else if (rc instanceof TransferBranch) {
                System.out.println("DepositFromTransfer Request received");
                TransferBranch = (TransferBranch) rc;
                transferDeposit(rc.getAcnt(), rc.getAmt(), rc.getSerNumber());
                System.out.println("DepsitFromTransfer Request recorded");
            }
    }

    public void run()
    {
        System.out.println("Server starting up!");
        while (true) {
            Message mr = null;
            try {
                mr = m.ReceiveMessage();
            } catch (MessagingException e) {
                System.out.println("Server failed to receive message");
                continue;
            }  

            if (mr instanceof RequestClient) {
                startBackup(mr);
           
            } else if (mr instanceof BranchMessage) {
                startBackup(mr);

            } else if (mr instanceof RequestBackup) {
                mr = (RequestBackup)mr;
                ResponseClient rc = recordTransaction(mr.GetRequest());
                m.sendToReplica(rc.GetReplica(), new BackupResponse(rc));
           
            } else if (mr instanceof ResponseBackup) {
                mr = (RequestBackup)mr;
                waiting_records.get(BackupMessage.GetRequest().getSerial()).add(mr.getReplica());
                if(backup_records.get(BackupMessage.message.getSerial()).equals(this.backups)) {
                    waiting_records.remove(BackupMessage.message.getSerial());
                    m.sendMessage(recordTransaction(mr.GetClientRequest()));
                }
            
            } else if (mr instanceof OracleMessage) {
                mr = (OracleMessage)mr;
            }
        }
    }

    public static void main(String args[])
    {
        new Server(new Integer(args[0]), new Integer(args[1])).run();
    }
}
