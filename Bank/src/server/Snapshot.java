package server;

import java.util.Map;
import java.util.Set;

import messaging.Message;

import server.SSInfo;
import server.Channel;
import server.BankAccount;

public class Snapshot extends Message {

    private Map<Integer, Channel> channels;
    private Set<BankAccount> branchState;

    public Snapshot(SSInfo ss) 
    {
        channels = ss.getChannels();
        branchState = ss.getBranchState();
    }

    public int getNumNonZeroAccounts()
    {
        return branchState.size();
    }
}
