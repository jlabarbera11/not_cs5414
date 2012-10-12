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

    public SSInfo(int numChannels, Set<BankAccount> branchState)
    {
        channels = new HashMap<Integer, Channel>();
        this.branchState = branchState;
        closedChannels = 0;
        this.numChannels = numChannels;
    }

    public boolean areAllChannelsClosed()
    {
        return closedChannels == numChannels;
    }

    public boolean closeChannel(Integer branchID)
    {
        if (channels.get(branchID).closeChannel())
            closedChannels++;

        return areAllChannelsClosed();
    }

    public void recordMessage(Integer branchID, Message m)
    {
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
}