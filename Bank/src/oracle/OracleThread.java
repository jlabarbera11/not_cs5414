package oracle;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import messaging.NewMessaging;
import messaging.ReplicaID;
import messaging.StatusQuery;
import messaging.StatusQueryResponse;

public class OracleThread extends Thread {
	NewMessaging newMessaging;
	/**
	public OracleThread(NewMessaging nm){
		this.newMessaging = nm;
	}

    public void run() {
    	ServerSocket serversocket = null;
    	try {
			 serversocket = new ServerSocket(newMessaging.getOracleInfo().port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	while(true){
    		try{
    			Socket clientSocket = serversocket.accept();
    			ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
    			StatusQuery sq = (StatusQuery)ois.readObject();
    			System.out.println("got status query for " + sq.branchIDOfInterest + "." + sq.replicaIDOfInterest);
    			ReplicaID replicaOfInterest = new ReplicaID(sq.branchIDOfInterest, sq.replicaIDOfInterest);
    			Oracle.replicaState status = newMessaging.getStatus(replicaOfInterest);
    			StatusQueryResponse response = new StatusQueryResponse(status, replicaOfInterest);
    			ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
    			System.out.println("returned status " + response.status);
    			oos.writeObject(response);
    			oos.close();
    			ois.close();
    			clientSocket.close();
    		} catch (Exception e){
    			e.printStackTrace();
    		}
    		
    	}
    }*/
}
