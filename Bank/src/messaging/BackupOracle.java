package messaging;

public class BackupOracle extends OracleMessage {
	
	public String recoveredReplicaID;

	public BackupOracle(String id){
		this.recoveredReplicaID = id;
	}
	
}