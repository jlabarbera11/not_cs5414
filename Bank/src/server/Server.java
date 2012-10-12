package server;

import messaging.Snapshot;
import messaging.Messaging;
import messaging.Message;
import messaging.Request;
import messaging.DepositRequest;
import messaging.WithdrawRequest;
import messaging.QueryRequest;
import messaging.TransferRequest;
import messaging.SnapshotRequest;
import messaging.DepositFromTransferMessage;
import messaging.SnapshotMessage;
import messaging.SnapshotResponse;
import messaging.MessagingException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.TreeSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class Server
{
    private int branchID;
    private ConcurrentHashMap<AccountNumber, BankAccount> accounts;
    private Messaging m;
    private Snapshots ss;

    public Server(int branchID)
    {
        this.branchID = branchID;
        accounts = new ConcurrentHashMap<AccountNumber, BankAccount>();

        try {
            m = new Messaging(branchID, Messaging.Type.SERVER);
            m.makeConnections();
        } catch (MessagingException e) {
            System.out.println("Server failed to create Messaging");
        }

        ss = new Snapshots(((ArrayList<Set<Integer>>) m.whoNeighbors()).get(1).size());
    }

    /**
     * @param resp whether to send response
     */
    public void deposit(int accountID, float amount, int serialNumber, boolean resp)
    {
        getAccount(accountID).deposit(amount, serialNumber, resp);
    }

    public void withdraw(int accountID, float amount, int serialNumber)
    {
        getAccount(accountID).withdraw(amount, serialNumber);
    }

    public void query(int accountID, int serialNumber)
    {
        getAccount(accountID).query(serialNumber);
    }

    public void transferWithdraw(int accountID, float amount, int serialNumber)
    {
        getAccount(accountID).transferWithdraw(amount, serialNumber);
    }

    public void transferDeposit(int accountID, float amount, int serialNumber)
    {
        getAccount(accountID).transferDeposit(amount, serialNumber);
    }

    public BankAccount getAccount(int accountID)
    {
        AccountNumber accountNumber = new AccountNumber(branchID, accountID);

        if (!accounts.containsKey(accountNumber)) {
            BankAccount bankAccount = new BankAccount(accountNumber, m);
            accounts.put(accountNumber, bankAccount);
            return bankAccount;
        }

        return accounts.get(accountNumber);
    }

    public Set<BankAccount> getBranchState()
    {
        Set<BankAccount> branchState = new TreeSet<BankAccount>();
        for (BankAccount ba : accounts.values()) {
            if (ba.getBalance() > 0.0f)
                branchState.add(ba);
        }

        return branchState;
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

            // TODO (KKH48): Reimplement using Visitor pattern
            if (mr instanceof DepositRequest) {
                System.out.println("Deposit Request received");
                DepositRequest request = (DepositRequest) mr;
                deposit(request.getAcnt(), request.getAmt(), request.getSerNumber(), true);
                ss.recordMessage(request.getSender(), request);
                System.out.println("Deposit Request handled");

            } else if (mr instanceof WithdrawRequest) {
                System.out.println("Withdraw Request received");
                WithdrawRequest request = (WithdrawRequest) mr;
                withdraw(request.getAcnt(), request.getAmt(), request.getSerNumber());
                ss.recordMessage(request.getSender(), request);
                System.out.println("Withdraw Request handled");

            } else if (mr instanceof QueryRequest) {
                System.out.println("Query Request received");
                QueryRequest request = (QueryRequest) mr;
                query(request.getAcnt(), request.getSerNumber());
                ss.recordMessage(request.getSender(), request);
                System.out.println("Query Request handled");

            } else if (mr instanceof TransferRequest) {
                System.out.println("Transfer Request received");
                TransferRequest request = (TransferRequest) mr;
                ss.recordMessage(request.getSender(), request);
                if (request.getDestBranch().equals(branchID) && request.getSrcAcnt().equals(request.getDestAcnt())) {
                    System.out.println("Transfering to itself");
                    transferWithdraw(request.getSrcAcnt(), 0.0f, request.getSerNumber());
                } else if (request.getDestBranch().equals(branchID)) {
                    transferWithdraw(request.getSrcAcnt(), request.getAmt(), request.getSerNumber());
                    deposit(request.getDestAcnt(), request.getAmt(), request.getSerNumber(), false);
                } else {
                    transferWithdraw(request.getSrcAcnt(), request.getAmt(), request.getSerNumber());
                    System.out.println("Sending request to second account");
                    try {
                        m.FinishTransfer(request.getDestBranch(), request.getDestAcnt(), request.getAmt(), request.getSerNumber());
                    } catch (MessagingException e) {
                        System.out.println("Source branch could not send Destination branch deposit");
                    }
                }
                System.out.println("Transfer Request handled");

            } else if (mr instanceof DepositFromTransferMessage) {
                System.out.println("DepositFromTransfer Request received");
                DepositFromTransferMessage request = (DepositFromTransferMessage) mr;
                ss.recordMessage(request.getSender(), request);
                transferDeposit(request.getAcnt(), request.getAmt(), request.getSerNumber());
                System.out.println("DepsitFromTransfer Request handled");
            
            } else if (mr instanceof SnapshotRequest) {
                // should only be received from client
                SnapshotRequest request = (SnapshotRequest) mr;
                ss.startSnapshot(request.getID(), getBranchState());
                try {
                    m.PropogateSnapshot(new SnapshotMessage(this.branchID, request.getID()));
                } catch (Exception e) {

                }

            } else if (mr instanceof SnapshotMessage) {
                SnapshotMessage message = (SnapshotMessage) mr;
                Integer ssID = message.getID();
                
                if (ss.snapshotExists(ssID)) {
                    if (ss.closeChannel(ssID, message.getSender())) {
                        // All channels are closed; send snapshot response
                        try {
                            m.SendResponse(new SnapshotResponse(new Snapshot(ss.getSSInfo(ssID))));
                        } catch (Exception e) {

                        }
                    }
                } else {
                    ss.startSnapshot(ssID, getBranchState());
                }
            }
        }
    }

    public static void main(String args[])
    {
        new Server(new Integer(args[0])).run();
    }
}
