package messaging;

public class StatusQuery extends ReplicaStatusMessage {

	public StatusQuery(int jvmOfInterest){
		this.jvmOfInterest = jvmOfInterest;
	}
}