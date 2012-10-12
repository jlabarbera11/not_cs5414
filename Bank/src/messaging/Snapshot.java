package messaging;

import java.util.Map;
import java.util.Set;

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
}
