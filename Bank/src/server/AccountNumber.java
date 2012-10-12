package server;

import java.io.Serializable;

public class AccountNumber implements Serializable
{
    private int branch;
    private int account;

    public AccountNumber(int branch, int account)
    {
        assert branch >= 0 && branch <= 99 : "branch: " + branch;
        assert account >= 0 && account <= 99999 : "account: " + account;

        this.branch = branch;
        this.account = account;
    }

    public int getBranch()
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
        return String.format("%02d.%05d", branch, account);
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
        return branch * 100000 + account;
    }
}
