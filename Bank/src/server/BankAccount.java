package server;

import messaging.Messaging;
import messaging.DepositResponse;
import messaging.WithdrawResponse;
import messaging.QueryResponse;
import messaging.TransferResponse;
import messaging.MessagingException;

import java.io.Serializable;
import java.util.HashSet;


public class BankAccount implements Comparable<BankAccount>, Serializable {
    private AccountNumber accountNumber;
    private Float balance;
    private HashSet<Integer> serials;
    private Messaging m;

    private enum Transaction
    {
        DEPOSIT,
        WITHDRAW,
        QUERY,
        TRANSFER_WITHDRAW,
        TRANSFER_DEPOSIT
    }

    public BankAccount(AccountNumber accountNumber, Messaging m)
    {
        this.accountNumber = accountNumber;
        balance = 0.0f;
        serials = new HashSet<Integer>();
        this.m = m;
    }

    public Float getBalance()
    {
        return balance;
    }

    public void deposit(Float amount, int serialNumber, boolean resp)
    {
        transaction(Transaction.DEPOSIT, amount, serialNumber, resp);
    }

    public void withdraw(Float amount, int serialNumber)
    {
        transaction(Transaction.WITHDRAW, amount, serialNumber, true);
    }

    public void query(int serialNumber)
    {
        transaction(Transaction.QUERY, 0.0f, serialNumber, true);
    }

    public void transferWithdraw(Float amount, int serialNumber)
    {
        transaction(Transaction.TRANSFER_WITHDRAW, amount, serialNumber, true);
    }

    public void transferDeposit(Float amount, int serialNumber)
    {
        transaction(Transaction.TRANSFER_DEPOSIT, amount, serialNumber, true);
    }

    private void transaction(Transaction t, Float amount, int serialNumber, boolean resp) 
    {
        boolean valid = !(serials.contains(serialNumber));
        
        if (valid) {
            System.out.println("new transaction number " + serialNumber);
            serials.add(serialNumber);

            switch (t) {
                case DEPOSIT:
                case TRANSFER_DEPOSIT:
                    balance += amount;
                    break;
                case WITHDRAW:
                case TRANSFER_WITHDRAW:
                    balance -= amount;
                    break;
                case QUERY:
                    break;
            }
        }

        try {
            if (resp) {
                switch (t) {
                    case DEPOSIT:
                        m.SendResponse(valid ? new DepositResponse(balance) : new DepositResponse("Invalid Serial Number"));
                        break;
                    case WITHDRAW:
                        m.SendResponse(valid ? new WithdrawResponse(balance) : new WithdrawResponse("Invalid Serial Number"));
                        break;
                    case QUERY:
                        m.SendResponse(valid ? new QueryResponse(balance) : new QueryResponse("Invalid Serial Number"));
                        break;
                    case TRANSFER_WITHDRAW:
                        m.SendResponse(valid ? new TransferResponse(balance) : new TransferResponse("Invalid Serial Number"));
                        break;
                    case TRANSFER_DEPOSIT:
                        break;
                }
            } 
        } catch (MessagingException e) {
            System.out.println("Failed to send response");
        }
    }

    public AccountNumber getAccountNumber()
    {
        return accountNumber;
    }

    @Override
    public int compareTo(BankAccount ba)
    {
        return accountNumber.hashCode() - ba.getAccountNumber().hashCode();
    }
}
