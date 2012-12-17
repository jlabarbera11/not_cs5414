package messaging;

import java.util.ArrayList;
import java.util.Set;

import messaging.Messaging.replicaState;

public class ReplicaInfo {
	public int port = -1;
	public String host = null;
    public replicaState state = null;
    public ArrayList<ReplicaID> neighbors = new ArrayList<ReplicaID>();

    public ReplicaInfo(int port, String host){
    	this.port = port;
    	this.host = host;
    	this.state = replicaState.running; //assumes no processor starts failed
    }

}
