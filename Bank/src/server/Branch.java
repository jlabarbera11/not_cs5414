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
}