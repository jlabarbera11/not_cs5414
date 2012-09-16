package server;

import messaging.Messaging;
import messaging.MessageRequest;
import messaging.DepositRequest;
import messaging.WithdrawRequest;
import messaging.QueryRequest;
import messaging.TransferRequest;
import messaging.MessagingException;

import java.util.HashMap;

public class Branch
{
	private int branchID;
	private HashMap<AccountNumber, BankAccount> accounts;

	public Branch(int branchID)
	{
		this.branchID = branchID;
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
        while (true) {
            try {
                Messaging m = new Messaging(branchID, Messaging.Type.SERVER);
                MessageRequest mr = m.ReceiveMessage();
                if (mr instanceof DepositRequest) {
                    DepositRequest request = (DepositRequest) mr;
                    deposit(request.getAcnt(), request.getAmt(), request.getSerNumber());
                } else if (mr instanceof WithdrawRequest) {
                    WithdrawRequest request = (WithdrawRequest) mr;
                    withdraw(request.getAcnt(), request.getAmt(), request.getSerNumber());
                } else if (mr instanceof QueryRequest) {
                    QueryRequest request = (QueryRequest) mr;
                    query(request.getAcnt(), request.getSerNumber());
                } else if (mr instanceof TransferRequest) {
                    TransferRequest request = (TransferRequest) mr;
                    transfer(request.getSrcAcnt(), request.getDestAcnt(), request.getAmt(), request.getSerNumber());
                }
            } catch (MessagingException e) {

            }        
        }
    }
}