package server;

import messaging.Messaging;
import messaging.DepositResponse;
import messaging.WithdrawResponse;
import messaging.QueryResponse;
import messaging.TransferResponse;
import messaging.DepositFromTransferResponse;
import messaging.MessagingException;

import java.util.HashSet;

public class BankAccount {
    private AccountNumber accountNumber;
    private float balance;
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

    public void deposit(float amount, int serialNumber)
    {
        transaction(Transaction.DEPOSIT, amount, serialNumber);
    }

    public void withdraw(float amount, int serialNumber)
    {
        transaction(Transaction.WITHDRAW, amount, serialNumber);
    }

    public void query(int serialNumber)
    {
        transaction(Transaction.QUERY, 0.0f, serialNumber);
    }

    public void transferWithdraw(float amount, int serialNumber)
    {
        transaction(Transaction.TRANSFER_WITHDRAW, amount, serialNumber);
    }

    public void transferDeposit(float amount, int serialNumber)
    {
        transaction(Transaction.TRANSFER_DEPOSIT, amount, serialNumber);
    }

    private void transaction(Transaction t, float amount, int serialNumber) 
    {
        if (!serials.contains(serialNumber)) {
            serials.add(serialNumber);

            switch (t) {
                case DEPOSIT:
                    System.out.println("Balance before: " + balance);
                    balance += amount;
                    System.out.println("Balance after: " + balance);
                    break;
                case WITHDRAW:
                    System.out.println("Balance before: " + balance);
                    balance -= amount;
                    System.out.println("Balance after: " + balance);
                    break;
                case QUERY:
                    break;
                case TRANSFER_WITHDRAW:
                    System.out.println("Balance before: " + balance);
                    balance -= amount;
                    System.out.println("Balance after: " + balance);
                    break;
                case TRANSFER_DEPOSIT:
                    balance += amount;
                    break;
            }
        }

        try {
            switch (t) {
                case DEPOSIT:
                    m.SendResponse(new DepositResponse(balance));
                    break;
                case WITHDRAW:
                    m.SendResponse(new WithdrawResponse(balance));
                    break;
                case QUERY:
                    m.SendResponse(new QueryResponse(balance));
                    break;
                case TRANSFER_WITHDRAW:
                    m.SendResponse(new TransferResponse(balance));
                    break;
                case TRANSFER_DEPOSIT:
                    m.SendResponse(new DepositFromTransferResponse(balance));
                    break;
            } 
        } catch (MessagingException e) {
            System.out.println("Failed to send response");
        }
    }
}
