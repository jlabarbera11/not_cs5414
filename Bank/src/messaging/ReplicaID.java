package messaging;

/**
 * 
 * @author Ben
 * 
 * The ReplicaID class holds a branch id and replica id, and has functions to allow it to serve as a hashmap key
 * 
 */

public class ReplicaID {
	public Integer branchNum;
	public Integer replicaNum;
	
	public ReplicaID(int branchID, int replicaID){
		this.branchNum = branchID;
		this.replicaNum = replicaID;
	}
	
	public boolean equals(ReplicaID replica){
		return ((this.branchNum == replica.branchNum) && (this.replicaNum == replica.replicaNum));
	}
	
    @Override
    public boolean equals(Object replica) {
        if (replica instanceof ReplicaID) {
        	return ((this.branchNum == ((ReplicaID)replica).branchNum) && (this.replicaNum == ((ReplicaID)replica).replicaNum));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.branchNum.hashCode() + this.replicaNum.hashCode();
    }
	
}
