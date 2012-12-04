package messaging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jvm.JVM;

import messaging.MessagingException.Type;

/**
 * Things that new messaging will do:
 *  - read in topology and resolver, building data structures
 *  - include methods to update these data structures as nodes fail or recover
 *  - include methods to get head, maybe to return the data structure itself
 *  - provide methods to open a socket, send a message, and close the socket
 */

public class Messaging {
	public static String topologyFilename = "resolvers/topology.txt";
	public static String resolverFilename = "resolvers/replicaResolver.txt";
	public static String clientResolverFilename = "resolvers/clientResolver.txt";
	public static String fdsResolverFilename = "resolvers/fdsResolver.txt";

  public enum replicaState {running, failed}

    private HashMap<ReplicaID, ReplicaInfo> allReplicaInfo = new HashMap<ReplicaID, ReplicaInfo>();

    //maps branch number to list of reachable branch numbers
    private HashMap<Integer, Set<Integer>> topology = new HashMap<Integer, Set<Integer>>();

    //maps client number to address
    private HashMap<Integer, ReplicaInfo> clientInfo = new HashMap<Integer, ReplicaInfo>();

    private HashMap<Integer, ReplicaInfo> fdsInfo = new HashMap<Integer, ReplicaInfo>();

    Map<Integer, Set<ReplicaID>> jvmInfo = new HashMap<Integer, Set<ReplicaID>>();

    private void readFDSResolver(){
		System.out.println("reading FDS resolver");
        try {
            Scanner scanner = new Scanner(new File(fdsResolverFilename));
            while (scanner.hasNextLine()) {
                String[] line = scanner.nextLine().split(" ");
                int fdsID = Integer.parseInt(line[0]);
                int port = Integer.parseInt(line[1]);
                String host = line[2];
                fdsInfo.put(fdsID, new ReplicaInfo(port, host));
            }
            scanner.close();
        } catch (Exception e) {
        	System.out.println("reading FDS resolver failed");
        	e.printStackTrace();
        	return;
        }
    }

    public HashMap<Integer, ReplicaInfo> getFdsInfo(){
    	return fdsInfo;
    }

    public ReplicaInfo getFdsPort(int fdsID){
    	return fdsInfo.get(fdsID);
    }



    public void sendToClientNoResponse(Integer clientNum, Message message) throws MessagingException{
		ReplicaInfo replicaInfo = clientInfo.get(clientNum);
		try {
			Socket socket = new Socket(replicaInfo.host, replicaInfo.port);

			socket.setSoTimeout(5 * 1000);

            ObjectOutputStream o = new ObjectOutputStream(socket.getOutputStream());

            o.writeObject(message);

            o.close();
            socket.close();

		} catch (Exception e){
			System.out.println("failure in send to sendToClientNoResponse");
			//e.printStackTrace();
			throw new MessagingException(MessagingException.Type.SEND_ERROR);
		}
    }

    private void setState(ReplicaID replicaID, replicaState newState){
    	ReplicaInfo replicaInfo = allReplicaInfo.get(replicaID);
    	replicaInfo.state = newState;
    }
    
    public void sendShutdown(int jvmID){
    	System.out.println("sending shutdown to fds " + jvmID);
    	ReplicaInfo info = JVM.readJvmResolver().get(jvmID);
    	try {
    		Socket socket = new Socket(info.host, info.port);
    		ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
    		oos.writeObject(new ShutdownMessage());
    		oos.close();
    		socket.close();
    		System.out.println("sent shutdown message");
    	} catch (Exception e){
    		System.out.println("error sending shutdown");
    		//e.printStackTrace();
    	}
    	
    }

    public void recordJvmFailure(ReplicaID replicaID){
    	int jvmID = getJvmID(replicaID);
    	sendShutdown(jvmID);
    	fdsInfo.remove(jvmID); //record failure of associated failure detection service
    	Set<ReplicaID> replicasToFail = jvmInfo.get(jvmID);
    	for (ReplicaID currentID : replicasToFail){
    		setState(currentID, replicaState.failed);
    	}
    	
    }

    public void recordPreviousPrimaryFailures(int branchID, int replicaID){
    	//TODO
		Set<Integer> notBackups = new HashSet<Integer>();
    	for (Map.Entry<ReplicaID, ReplicaInfo> entry : allReplicaInfo.entrySet())
    	{
    	    if (entry.getKey().branchNum == branchID && entry.getKey().replicaNum < replicaID){
    	    	notBackups.add(entry.getKey().replicaNum);
    	    }
    	}
    	for (Integer entry : notBackups){
    		recordJvmFailure(new ReplicaID(branchID, entry));
    	}
    }

    public ReplicaID getHead(int branchNum){
    	ArrayList<ReplicaID> replicas = new ArrayList<ReplicaID>();
    	for (Map.Entry<ReplicaID, ReplicaInfo> entry : allReplicaInfo.entrySet())
    	{
    	    if (entry.getKey().branchNum == branchNum){
    	    	replicas.add(entry.getKey());
    	    }
    	}

    	Collections.sort(replicas, new Comparator<ReplicaID>() {
            public int compare(ReplicaID replicaID1, ReplicaID replicaID2) {
                return replicaID1.replicaNum.compareTo(replicaID2.replicaNum);
            }
        });

    	for (ReplicaID entry : replicas){
    		if (allReplicaInfo.get(entry).state == replicaState.running){
    			return entry;
    		}
    	}
    	System.out.println("error in getHead");
    	return null;
    }

    //assumes replica is up
	public void sendToReplicaNoResponse(ReplicaID replicaID, Message message) throws MessagingException{
		ReplicaInfo replicaInfo = allReplicaInfo.get(replicaID);
		try {
			System.out.println("sending to port " + replicaInfo.port + " on " + replicaInfo.host);
			Socket socket = new Socket(replicaInfo.host, replicaInfo.port);

			socket.setSoTimeout(5 * 1000);

            ObjectOutputStream o = new ObjectOutputStream(socket.getOutputStream());

            o.writeObject(message);
            o.close();
            socket.close();

		} catch (Exception e){
			System.out.println("failure in sendToReplicaNoResponse");
			//e.printStackTrace();
			throw new MessagingException(MessagingException.Type.SEND_ERROR);
		}

	}

	public void sendToPrimaryNoResponse(int branchID, Message message) throws MessagingException{
		//look up address, call above
		ReplicaID headID = null;
		//System.out.println("sendToPrimary got branchID " + branchID);
		while(true){
			headID = getHead(branchID);
			replicaState status = checkReplicaStatus(headID);
			if (status != replicaState.running){
				System.out.println("old head FAILURE detected");
				recordJvmFailure(headID);
			} else {
				break;
			}
		}
		sendToReplicaNoResponse(headID, message);
	}

	/*
	 * checkReplicaStatus opens a socket to each FDS and sends a status query.
	 * This class is used to handle such a socket and record the response.
	 */
	private class CheckStatusThread extends Thread {
		ConcurrentHashMap<replicaState, Integer> responses;
		Socket socket;

		public CheckStatusThread(ConcurrentHashMap<replicaState, Integer> responses, Socket socket){
			this.responses = responses;
			this.socket = socket;
		}

		public void run(){
			try {
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
				StatusQueryResponse response = (StatusQueryResponse) ois.readObject();
				socket.close();
				synchronized(responses){
					int currentCount = responses.get(response.status);
					responses.put(response.status, currentCount+1);
					System.out.println("check status thread recorded status " + response.status);
				}
			} catch (Exception e){
				System.out.println("error while waiting for response from FDS");
				//e.printStackTrace();
			}
		}

	}

	//return the jvm id associated with a given replicaID
	public int getJvmID(ReplicaID replicaID){
		for (Map.Entry<Integer, Set<ReplicaID>> entry : jvmInfo.entrySet()){
			for (ReplicaID currentID : entry.getValue()){
				if (replicaID.equals(currentID)){
					return entry.getKey();
				}
			}
		}
		System.out.println("ERROR: replicaID not found during getJvmID");
		return -1;
	}

	public void broadcastToAllFDS(Message message){
    	for (Map.Entry<Integer, ReplicaInfo> entry: fdsInfo.entrySet()){
    		try {
    			Socket socket = new Socket(entry.getValue().host, entry.getValue().port);
    			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
    			oos.writeObject(message);
    			socket.close();
    		} catch (Exception e){
    			//System.out.println("error sending to fds " + entry.getKey());
    			//e.printStackTrace();
    		}
    	}
	}

	/**
	 * 1) send a Status query to all FDSs
	 * 2) make decision based on majority
	 * @throws MessagingException
	*/
    public replicaState checkReplicaStatus(ReplicaID replicaID) throws MessagingException {
    	ConcurrentHashMap<replicaState, Integer> responses = new ConcurrentHashMap<replicaState, Integer>();
    	responses.put(replicaState.failed, 0);
    	responses.put(replicaState.running, 0);
    	System.out.println("checking status of replica " + replicaID.toString());
    	for (Map.Entry<Integer, ReplicaInfo> entry: fdsInfo.entrySet()){
    		try {
    			Socket socket = new Socket(entry.getValue().host, entry.getValue().port);
    			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
    			StatusQuery sq = new StatusQuery(getJvmID(replicaID));
    			oos.writeObject(sq);
    			CheckStatusThread thread = new CheckStatusThread(responses, socket);
    			thread.run();
    		} catch (Exception e){
    			System.out.println("error sending to fds " + entry.getKey());
    			//e.printStackTrace();
    		}
    	}
    	System.out.println("HERE!!!\n\n\n");
    	//check responses in a loop. timeout at 5 seconds
    	for (int i=0; i<5; i++){
			int numFailure = responses.get(replicaState.failed);
			int numRunning = responses.get(replicaState.running);
			if (numRunning > fdsInfo.size()/2){
				System.out.println("check status is concluding that state is running");
				return replicaState.running;
			} else if (numFailure > fdsInfo.size()/2){
				System.out.println("check status is concluding that state is failed");
				return replicaState.failed;
			} else {
				System.out.println("check status has not received enough responses. numfailed is " + numFailure + " and numRunning is " + numRunning);
				System.out.println("num fds is " + fdsInfo.size());
			}
    		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
    	}
    	throw new MessagingException(Type.SEND_ERROR);
	}

	//assumes replica is up
	public Message sendToReplicaAndReturnResponse(ReplicaID replicaID, Message message) throws MessagingException{
		ReplicaInfo replicaInfo = allReplicaInfo.get(replicaID);
		try {
			Socket socket = new Socket(replicaInfo.host, replicaInfo.port);
			System.out.println("sending to port " + replicaInfo.port + " and host " + replicaInfo.host);
			socket.setSoTimeout(1000);

            ObjectOutputStream o = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream i = new ObjectInputStream(socket.getInputStream());

            o.writeObject(message);
            System.out.println("written message");
            Message response = (Message)i.readObject();

            i.close();
            o.close();
            socket.close();

            return response;

		} catch (Exception e){
			System.out.println("failure in sendToAddressAndReturnResult");
			//e.printStackTrace();
			throw new MessagingException(MessagingException.Type.SEND_ERROR);
		}

	}

	public Message sendToPrimaryAndReturnResponse(int branchID, Message message) throws MessagingException{
		//look up address, call above
		ReplicaID headID = null;
		while(true){
			headID = getHead(branchID);
			replicaState status = checkReplicaStatus(headID);
			if (status != replicaState.running){
				System.out.println("old head FAILURE detected");
				recordJvmFailure(headID);
			} else {
				break;
			}
		}
		return sendToReplicaAndReturnResponse(headID, message);
	}

	//Do ALL initializing here
	public Messaging(){
		System.out.println("intitializing Messaging");
		readResolver();
		readTopology();
		readClientResolver();
		readFDSResolver();
		this.jvmInfo = JVM.readjvmInfo();
	}

	public void readClientResolver(){
		System.out.println("reading client resolver");
        try {
            Scanner scanner = new Scanner(new File(clientResolverFilename));
            while (scanner.hasNextLine()) {
                String[] line = scanner.nextLine().split(" ");
                Integer branchNum = Integer.parseInt(line[0]);

                Integer port = Integer.parseInt(line[2]);
                String hostname = line[1];
                ReplicaInfo replicaInfo = new ReplicaInfo(port, hostname);
                clientInfo.put(branchNum, replicaInfo);
            }
            scanner.close();
        } catch (Exception e) {
        	System.out.println("reading client resolver failed");
        	e.printStackTrace();
        	return;
        }
	}

	//replicaResolver.txt format: 01.01 localhost 4441
	public void readResolver(){
		System.out.println("reading resolver");
        try {
            Scanner scanner = new Scanner(new File(resolverFilename));
            while (scanner.hasNextLine()) {
                String[] line = scanner.nextLine().split(" ");
                Integer branchNum = Integer.parseInt(line[0].substring(0,2));
                Integer replicaNum = Integer.parseInt(line[0].substring(3,5));
                ReplicaID replicaID = new ReplicaID(branchNum, replicaNum);

                Integer port = Integer.parseInt(line[2]);
                String hostname = line[1];
                ReplicaInfo replicaInfo = new ReplicaInfo(port, hostname);
                allReplicaInfo.put(replicaID, replicaInfo);
            }
            scanner.close();
        } catch (Exception e) {
        	System.out.println("reading resolver failed");
        	e.printStackTrace();
        	return;
        }
	}

	//TODO
	public void readTopology(){
		System.out.println("reading topology");
        try {
            Scanner scanner = new Scanner(new File(topologyFilename));
            while (scanner.hasNextLine()) {

                String[] a = scanner.nextLine().split(" ");
                Integer key = Integer.parseInt(a[0]);
                Integer val = Integer.parseInt(a[1]);

                if (topology.containsKey(key)) {
                    topology.get(key).add(val);
                }

                else {
                    Set<Integer> s = new HashSet<Integer>();
                    s.add(val);
                    topology.put(key, s);
                }
            }
            scanner.close();
        } catch (Exception e) {
            System.out.println("reading topology failed");
            e.printStackTrace();
        }

	}

	public ReplicaInfo getReplicaInfo(ReplicaID replicaID) {
		return allReplicaInfo.get(replicaID);
	}

	public ReplicaInfo getClientInfo(int clientNumber) {
		return clientInfo.get(clientNumber);
	}

	public Set<Integer> initBackups(int branchID, int replicaID) {
		Set<Integer> output = new HashSet<Integer>();
    	for (Map.Entry<ReplicaID, ReplicaInfo> entry : allReplicaInfo.entrySet())
    	{
    	    if (entry.getKey().branchNum == branchID && entry.getKey().replicaNum > replicaID){
    	    	output.add(entry.getKey().replicaNum);
    	    }
    	}
    	return output;
	}

}
