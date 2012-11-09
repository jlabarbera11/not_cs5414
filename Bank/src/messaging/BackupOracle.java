package messaging;

public class BackupOracle extends OracleMessage {
	
	private String recoveredReplicaID;
	private String primaryForRecoveringReplica;

	public BackupOracle(String id){
		this.recoveredReplicaID = id;
	}

        public String GetRecoveredReplicaID() {
            return this.recoveredReplicaID;
        }

        public String GetPrimary() {
            return this.primaryForRecoveringReplica;
        }
	
}
