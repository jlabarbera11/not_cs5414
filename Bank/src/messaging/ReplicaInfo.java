package messaging;

import java.util.ArrayList;
import java.util.Set;

import oracle.Oracle;

public class ReplicaInfo {
	public int port = -1;
	public String host = null;
    public Oracle.replicaState state = null;
    public ArrayList<ReplicaID> neighbors = new ArrayList<ReplicaID>();
	
    public ReplicaInfo(int port, String host){
    	this.port = port;
    	this.host = host;
    	this.state = Oracle.replicaState.running; //assumes no processor starts failed
    }
    
}
