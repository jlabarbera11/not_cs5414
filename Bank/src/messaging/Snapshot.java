package messaging;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import server.SSInfo;
import server.Channel;
import server.BankAccount;

public class Snapshot extends Message implements Serializable {

    public Map<Integer, Channel> channels;
    public Set<BankAccount> branchState;

    public Snapshot(SSInfo ss) 
    {
        channels = ss.getChannels();
        branchState = ss.getBranchState();
    }
}
