package server;

import java.io.Serializable;

public class AccountNumber implements Serializable
{
    private String branch;
    private int account;

    public AccountNumber(String branch, int account)
    {
        assert Integer.parseInt(branch) >= 0 && Integer.parseInt(branch) <= 99 : "branch: " + branch;
        assert account >= 0 && account <= 99999 : "account: " + account;

        this.branch = branch;
        this.account = account;
    }

    public String getBranch()
    {
        return branch;
    }

    public int getAccount()
    {
        return account;
    }

    @Override
    public String toString()
    {
        return String.format("%s.%05d", branch, account);
    }

    @Override
    public boolean equals(Object that)
    {
        if (this == that)
            return true;

        if (!(that instanceof AccountNumber))
            return false;

        AccountNumber an = (AccountNumber) that;
        return branch == an.branch && account == an.account;
    }

    @Override
    public int hashCode() {
        return Integer.parseInt(branch) * 100000 + account;
    }
}
