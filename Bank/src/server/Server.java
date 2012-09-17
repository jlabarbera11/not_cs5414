package server;

import messaging.Messaging;
import messaging.MessageRequest;
import messaging.DepositRequest;
import messaging.WithdrawRequest;
import messaging.QueryRequest;
import messaging.TransferRequest;
import messaging.MessagingException;

import java.util.HashMap;

public class Server
{
    private int branchID;
    private HashMap<AccountNumber, BankAccount> accounts;

    public Server(int branchID)
    {
        this.branchID = branchID;
        accounts = new HashMap<AccountNumber, BankAccount>();
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

    public void transfer(int sourceAccount, int destinationAccount, float amount, int serialNumber)
    {
        getAccount(sourceAccount).transfer(getAccount(destinationAccount), amount, serialNumber);
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

    public void run()
    {
        Messaging m = null;
        try {
            m = new Messaging(branchID, Messaging.Type.SERVER);
        } catch (MessagingException e) {
            System.out.println("Server failed to create Messaging");
            return;
        }
        
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
                transfer(request.getSrcAcnt(), request.getDestAcnt(), request.getAmt(), request.getSerNumber());
                System.out.println("Transfer Request handled");
            }
        }
    }

    public static void main(String args[])
    {
        new Server(new Integer(args[0])).run();
    }
}
