package messaging;

import oracle.Oracle;

public class StatusQueryResponse extends ReplicaStatusMessage{
	public Oracle.replicaState status;
	
	public StatusQueryResponse(Oracle.replicaState status, int jvmOfInterest){
		this.status = status;
		this.jvmOfInterest = jvmOfInterest;
	}
}
