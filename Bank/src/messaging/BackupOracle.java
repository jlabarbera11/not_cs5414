package messaging;

public class BackupOracle extends OracleMessage {
	
	public String recoveredReplicaID;
	public String primaryForRecoveringReplica;

	public BackupOracle(String id){
		this.recoveredReplicaID = id;
	}
	
}