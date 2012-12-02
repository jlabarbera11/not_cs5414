package messaging;

public class StatusQuery extends ReplicaStatusMessage {

	public StatusQuery(ReplicaID replicaOfInterest){
		this.replicaIDOfInterest = replicaOfInterest.replicaNum;
		this.branchIDOfInterest = replicaOfInterest.branchNum;
	}
}
