package messaging;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;

import server.SSInfo;
import server.Channel;
import server.BankAccount;

public class Snapshot extends Message implements Serializable {

    public Map<Integer, List<Message>> channels;
    // Int, int, float
    public Set<List<Object>> branchState;

    public Snapshot(SSInfo ss) 
    {
        channels = new HashMap<Integer, List<Message>>();

        for (Map.Entry<Integer, Channel> entry : ss.getChannels().entrySet()) {
            channels.put(entry.getKey(), entry.getValue().getMessages());
        }

        branchState = new HashSet<List<Object>>();
        for (BankAccount b : ss.getBranchState()) {
            branchState.add(b.getObjects());
        }
    }
}
