package server;

import messaging.Messaging;
import messaging.DepositResponse;
import messaging.WithdrawResponse;
import messaging.QueryResponse;
import messaging.TransferResponse;
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
        TRANSFER,
        RECEIVE
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
        transaction(Transaction.DEPOSIT, null, amount, serialNumber);
    }

    public void withdraw(float amount, int serialNumber)
    {
        transaction(Transaction.WITHDRAW, null, amount, serialNumber);
    }

    public void query(int serialNumber)
    {
        transaction(Transaction.QUERY, null, 0.0f, serialNumber);
    }

    public void transfer(BankAccount destination, float amount, int serialNumber)
    {
        transaction(Transaction.TRANSFER, destination, amount, serialNumber);
    }

    private void receive(float amount, int serialNumber)
    {
        transaction(Transaction.RECEIVE, null, amount, serialNumber);
    }

    private void transaction(Transaction t, BankAccount destination, float amount, int serialNumber) 
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
                case TRANSFER:
                    System.out.println("Balance before: " + balance);
                    balance -= amount;
                    System.out.println("Balance after: " + balance);
                    destination.receive(amount, serialNumber);
                    break;
                case RECEIVE:
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
                case TRANSFER:
                    m.SendResponse(new TransferResponse(balance));
                    break;
                case RECEIVE:
                    break;
            } 
        } catch (MessagingException e) {
            System.out.println("Failed to send response");
        }
    }
}
