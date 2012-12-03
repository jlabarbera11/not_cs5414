package server;

import messaging.ResponseClient;
import messaging.DepositResponse;
import messaging.WithdrawResponse;
import messaging.QueryResponse;
import messaging.TransferResponse;

import java.io.Serializable;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.List;


public class BankAccount implements Comparable<BankAccount>, Serializable {
    private AccountNumber accountNumber;
    private Float balance;
    private HashSet<Integer> serials;

    private enum Transaction
    {
        DEPOSIT,
        WITHDRAW,
        QUERY,
        TRANSFER_WITHDRAW,
        TRANSFER_DEPOSIT
    }

    public BankAccount(AccountNumber accountNumber)
    {
        this.accountNumber = accountNumber;
        balance = 0.0f;
        serials = new HashSet<Integer>();
    }

    public Float getBalance()
    {
        return balance;
    }

    public DepositResponse deposit(Float amount, int serialNumber)
    {
        return (DepositResponse)transaction(Transaction.DEPOSIT, amount, serialNumber);
    }

    public WithdrawResponse withdraw(Float amount, int serialNumber)
    {
        return (WithdrawResponse)transaction(Transaction.WITHDRAW, amount, serialNumber);
    }

    public QueryResponse query(int serialNumber)
    {
        return (QueryResponse)transaction(Transaction.QUERY, 0.0f, serialNumber);
    }

    public TransferResponse transferWithdraw(Float amount, int serialNumber)
    {
        return (TransferResponse)transaction(Transaction.TRANSFER_WITHDRAW, amount, serialNumber);
    }

    public void transferDeposit(Float amount, int serialNumber)
    {
        transaction(Transaction.TRANSFER_DEPOSIT, amount, serialNumber);
    }

    private ResponseClient transaction(Transaction t, Float amount, int serialNumber)
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

        switch (t) {
            case DEPOSIT:
                return (valid ? new DepositResponse(balance) : new DepositResponse("Invalid Serial Number"));
            case WITHDRAW:
                return (valid ? new WithdrawResponse(balance) : new WithdrawResponse("Invalid Serial Number"));
            case QUERY:
                return (valid ? new QueryResponse(balance) : new QueryResponse("Invalid Serial Number"));
            case TRANSFER_WITHDRAW:
                return (valid ? new TransferResponse(balance) : new TransferResponse("Invalid Serial Number"));
            case TRANSFER_DEPOSIT:
                break;
        }
        return (valid ? new ResponseClient() : new ResponseClient()); //TODO: This is strange.
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
