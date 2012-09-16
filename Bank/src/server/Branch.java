package server;

import java.util.Hashmap;

public class Branch
{
	private int branchID;
	private Hashmap<AccountNumber, BankAccount> accounts;

	public Branch(int branchID)
	{
		this.branchID = branchID;
	}

	public void deposit(int accountID, float amount, int serialNumber)
	{
		BankAccount acnt = getAccount(accountID);

        if (acnt == null) {
            acnt.deposit(amount, serialNumber);
        }
	}

	public void withdraw(int accountID, float amount, int serialNumber)
	{
        BankAccount acnt = getAccount(accountID);

        if (acnt != null) {
            acnt.withdraw(amount, serialNumber);
        }
	}

	public void query(int accountID, int serialNumber)
	{
        BankAccount acnt = getAccount(accountID);

        if (acnt != null) {
            acnt.query(serialNumber);
        }
	}

	public void transfer(int sourceAccount, int destinationAccount, float amount, int serialNumber)
	{
        BankAccount source = getAccount(sourceAccount);
        BankAccount destination = getAccount(destinationAccount);

        if (source != null && destination != null) {
            source.transfer(destination, amount, serialNumber);
        }

	}

	public BankAccount getAccount(int accountID)
	{
		AccountNumber acnt = new AccountNumber(branchID, accountID);
        if (!accounts.containsKey(acnt))
            return null;

        return accounts.get(acnt);
	}
}