package server;

import java.util.Set;
import java.util.Map;
import messaging.Message;
import java.util.HashMap;
import java.util.HashSet;

public class Snapshots
{
    private Map<Integer, SSInfo> ssInfoMap;
    private Set<Integer> ongoingSnapshots;
    private int numNeighbors;

    public Snapshots(int numNeighbors)
    {
        this.numNeighbors = numNeighbors;
        ssInfoMap = new HashMap<Integer, SSInfo>();
        ongoingSnapshots = new HashSet<Integer>();
    }

    public Set<Integer> getOngoingSnapshots()
    {
        return ongoingSnapshots;
    }

    public boolean startSnapshot(Integer ssID, Set<BankAccount> branchState, Integer ignore)
    {
        if (ssInfoMap.get(ssID) != null)
            return false;

        SSInfo info = new SSInfo(numNeighbors, branchState, ignore);
        ssInfoMap.put(ssID, info);
        return true;
    }

    public boolean snapshotExists(Integer ssID)
    {
        return ssInfoMap.get(ssID) != null;
    }

    public void recordMessage(Integer branchID, Message m)
    {
        for (Integer ssID : ongoingSnapshots) {
            ssInfoMap.get(ssID).recordMessage(branchID, m);
        }
    }

    public void removeOngoingSnapshot(Integer ssID)
    {
        ongoingSnapshots.remove(ssID);
    }

    public boolean closeChannel(Integer ssID, Integer branchID)
    {
        if (ssInfoMap.get(ssID).closeChannel(branchID)) {
            ongoingSnapshots.remove(ssID);
            return true;
        }

        return false;
    }

    public SSInfo getSSInfo(Integer ssID)
    {
        SSInfo info = ssInfoMap.get(ssID);
        assert info != null;

        return info;
    }

    public int getNumNeighbors()
    {
        return numNeighbors;
    }
}
