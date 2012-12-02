package messaging;

import oracle.Oracle;

public class StatusQueryResponse extends ReplicaStatusMessage{
	public Oracle.replicaState status;
	
	public StatusQueryResponse(Oracle.replicaState status, ReplicaID replicaOfInterest){
		this.status = status;
		this.replicaIDOfInterest = replicaOfInterest.replicaNum;
		this.branchIDOfInterest = replicaOfInterest.branchNum;
	}
}
