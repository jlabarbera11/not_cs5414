package messaging;

public class StatusQueryResponse extends ReplicaStatusMessage{
	public Messaging.replicaState status;

	public StatusQueryResponse(Messaging.replicaState status, int jvmOfInterest){
		this.status = status;
		this.jvmOfInterest = jvmOfInterest;
	}
}
