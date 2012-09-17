package server;

import messaging.Messaging;
import messaging.MessageRequest;
import messaging.DepositRequest;
import messaging.WithdrawRequest;
import messaging.QueryRequest;
import messaging.DepositFromTransferRequest;
import messaging.TransferRequest;
import messaging.MessagingException;

import java.util.HashMap;

public class Server
{
    private int branchID;
    private HashMap<AccountNumber, BankAccount> accounts;
    private Messaging m;

    public Server(int branchID)
    {
        this.branchID = branchID;
        accounts = new HashMap<AccountNumber, BankAccount>();
        try {
            m = new Messaging(branchID, Messaging.Type.SERVER);
        } catch (MessagingException e) {
            System.out.println("Server failed to create Messaging");
        }
    }

    public void deposit(int accountID, float amount, int serialNumber)
    {
        getAccount(accountID).deposit(amount, serialNumber);
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

    public void run()
    {
        System.out.println("Server starting up!");
        while (true) {
            MessageRequest mr = null;
            try {
                mr = m.ReceiveMessage();
            } catch (MessagingException e) {
                System.out.println("Server failed to receive message");
                continue;
            }  
            
            if (mr instanceof DepositRequest) {
                System.out.println("Deposit Request received");
                DepositRequest request = (DepositRequest) mr;
                deposit(request.getAcnt(), request.getAmt(), request.getSerNumber());
                System.out.println("Deposit Request handled");

            } else if (mr instanceof WithdrawRequest) {
                System.out.println("Withdraw Request received");
                WithdrawRequest request = (WithdrawRequest) mr;
                withdraw(request.getAcnt(), request.getAmt(), request.getSerNumber());
                System.out.println("Withdraw Request handled");

            } else if (mr instanceof QueryRequest) {
                System.out.println("Query Request received");
                QueryRequest request = (QueryRequest) mr;
                query(request.getAcnt(), request.getSerNumber());
                System.out.println("Query Request handled");

            } else if (mr instanceof TransferRequest) {
                System.out.println("Transfer Request received");
                TransferRequest request = (TransferRequest) mr;
                if (request.getDestBranch().equals(branchID)) {
                    transferWithdraw(request.getSrcAcnt(), 0.0f, request.getSerNumber());
                } else {
                    transferWithdraw(request.getSrcAcnt(), request.getAmt(), request.getSerNumber());
                    try {
                        m.DepositFromTransfer(request.getDestBranch(), request.getDestAcnt(), request.getAmt(), request.getSerNumber());
                    } catch (MessagingException e) {
                        System.out.println("Source branch could not send Destination branch deposit");
                    }
                }
                System.out.println("Transfer Request handled");

            } else if (mr instanceof DepositFromTransferRequest) {
                System.out.println("DepositFromTransfer Request received");
                DepositFromTransferRequest request = (DepositFromTransferRequest) mr;
                transferDeposit(request.getAcnt(), request.getAmt(), request.getSerNumber());
                System.out.println("DepsitFromTransfer Request handled");
            }
        }
    }

    public static void main(String args[])
    {
        new Server(new Integer(args[0])).run();
    }
}
