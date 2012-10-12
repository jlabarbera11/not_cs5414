package server;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import messaging.Message;

public class SSInfo
{
    // Integer corresponds to branchID
    private Map<Integer, Channel> channels;
    private Set<BankAccount> branchState;
    private int closedChannels;
    private int numChannels;
    private Integer ignore;

    public SSInfo(boolean isRequest, int numChannels, Set<BankAccount> branchState, Integer ignore)
    {
        channels = new HashMap<Integer, Channel>();
        this.branchState = branchState;
        closedChannels = 0;
        this.numChannels = numChannels;
        this.ignore = ignore;

        if (!isRequest)
            this.numChannels = numChannels - 1;
    }

    public boolean areAllChannelsClosed()
    {
        return closedChannels == numChannels;
    }

    public boolean closeChannel(Integer branchID)
    {
        if (channels.get(branchID).closeChannel()) {
            System.out.println("closed channel");
            closedChannels++;
        }

        return areAllChannelsClosed();
    }

    public void recordMessage(Integer branchID, Message m)
    {
        if (branchID.equals(ignore))
            return;

        Channel c = channels.get(branchID);

        if (c == null) {
            c = new Channel();
            channels.put(branchID, c);
        }

        c.add(m);
    }

    public Map<Integer, Channel> getChannels()
    {
        return channels;
    }

    public Set<BankAccount> getBranchState()
    {
        return branchState;
    }

    public Integer getNumChannels()
    {
        return numChannels;
    }
}