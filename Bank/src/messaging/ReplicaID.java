package messaging;

import java.io.Serializable;
/**
 *
 * @author Ben
 *
 * The ReplicaID class holds a branch id and replica id, and has functions to allow it to serve as a hashmap key
 *
 */

public class ReplicaID implements Serializable {
	public Integer branchNum;
	public Integer replicaNum;

	public ReplicaID(int branchID, int replicaID){
		this.branchNum = branchID;
		this.replicaNum = replicaID;
	}

    @Override
    public boolean equals(Object replica) {
        if (replica instanceof ReplicaID) {
        	return ((this.branchNum.equals(((ReplicaID)replica).branchNum)) && (this.replicaNum.equals(((ReplicaID)replica).replicaNum)));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.branchNum.hashCode() + this.replicaNum.hashCode();
    }

    @Override
    public String toString(){
    	return this.branchNum + "." + this.replicaNum;
    }

}
