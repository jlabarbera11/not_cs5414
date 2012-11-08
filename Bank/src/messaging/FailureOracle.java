package messaging;

public class FailureOracle extends OracleMessage {
	
	public String failedReplicaID;

	public FailureOracle(String id){
		this.failedReplicaID = id;
	}
}
