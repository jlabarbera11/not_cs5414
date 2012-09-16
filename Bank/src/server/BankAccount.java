package server;

import messaging.Messaging;
import messaging.DepositResponse;
import messaging.WithdrawResponse;
import messaging.QueryResponse;
import messaging.TransferResponse;

import java.util.Hashset;

public class BankAccount {
    private AccountNumber accountNumber;
    private float balance;
    private Hashset<Integer> serials;

    private enum Transaction
    {
        DEPOSIT,
        WITHDRAW,
        QUERY,
        TRANSFER,
        RECEIVE
    }

	public BankAccount(AccountNumber accountNumber)
    {
        this.accountNumber = accountNumber;
        balance = 0.0;
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
        transaction(Transaction.QUERY, null, 0.0, serialNumber);
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
        if serials.contains(serialNumber) {
            serials.add(serialNumber);

            switch (t) {
                case DEPOSIT:
                    balance += amount;
                    break;
                case WITHDRAW:
                    balance -= amount;
                    break;
                case QUERY:
                    break;
                case TRANSFER:
                    balance -= amount;
                    destination.receive(amount, serialNumber);
                    break;
                case RECEIVE:
                    balance += amount;
                    break;
            }
        }

        switch (t) {
                case DEPOSIT:
                    Messaging.SendResponse(new DepositResponse(balance));
                    break;
                case WITHDRAW:
                    Messaging.SendResponse(new WithdrawResponse(balance));
                    break;
                case QUERY:
                    Messaging.SendResponse(new QueryResponse(balance));
                    break;
                case TRANSFER:
                    Messaging.SendResponse(new TransferResponse(balance));
                    break;
                case RECEIVE:
                    break;
        }
    }
}