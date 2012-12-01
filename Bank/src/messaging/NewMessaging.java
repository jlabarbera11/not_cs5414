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

import oracle.Oracle;
import oracle.Oracle.replicaState;

/**
 * Things that new messaging will do:
 *  - read in topology and resolver, building data structures
 *  - include methods to update these data structures as nodes fail or recover
 *  - include methods to get head, maybe to return the data structure itself
 *  - provide methods to open a socket, send a packet, and close the socket
 */

public class NewMessaging {
	public static String topologyFilename = "topology.txt";
	public static String resolverFilename = "resolver.txt";
	public static String oracleFilename = "oracle.txt"; //TODO: remove for phase 4
	public static String clientResolverFilename = "clientResolver.txt";
	
    private HashMap<ReplicaID, ReplicaInfo> allReplicaInfo = new HashMap<ReplicaID, ReplicaInfo>();
    
    //maps branch number to list of reachable branch numbers
    private HashMap<Integer, Set<Integer>> topology = new HashMap<Integer, Set<Integer>>(); 
    
    //maps client number to address
    private HashMap<Integer, ReplicaInfo> clientInfo = new HashMap<Integer, ReplicaInfo>();
    
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
			e.printStackTrace();
			throw new MessagingException(MessagingException.Type.SEND_ERROR);
		}
    }
    
    public void setState(ReplicaID replicaID, Oracle.replicaState newState){
    	ReplicaInfo replicaInfo = allReplicaInfo.get(replicaID);
    	replicaInfo.state = newState;
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
		System.out.println("getting replica info for replicaID " + replicaID.toString());
		ReplicaInfo replicaInfo = allReplicaInfo.get(replicaID);
		try {
			System.out.println("sending to port " + replicaInfo.port + " on " + replicaInfo.host);
			Socket socket = new Socket(replicaInfo.host, replicaInfo.port);
			
			socket.setSoTimeout(5 * 1000);
            
            ObjectOutputStream o = new ObjectOutputStream(socket.getOutputStream());

            o.writeObject(message);
            System.out.println("written message to stream");
            o.close();
            socket.close();
            
		} catch (Exception e){
			System.out.println("failure in sendToReplicaNoResponse");
			e.printStackTrace();
			throw new MessagingException(MessagingException.Type.SEND_ERROR);
		}
		
	}
	
	public void sendToPrimaryNoResponse(int branchID, Message message) throws MessagingException{
		//look up address, call above
		ReplicaID headID = getHead(branchID);
		sendToReplicaNoResponse(headID, message);
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
			e.printStackTrace();
			throw new MessagingException(MessagingException.Type.SEND_ERROR);
		}
		
	}
	
	public Message sendToPrimaryAndReturnResponse(int branchID, Message message) throws MessagingException{
		//look up address, call above
		ReplicaID headID = getHead(branchID);
		return sendToReplicaAndReturnResponse(headID, message);
	}
	
	//Do ALL initializing here
	public NewMessaging(){
		System.out.println("intitializing NewMessaging");
		readResolver();
		readTopology();
		readClientResolver();
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
	
	//resolver.txt format: 01.01 localhost 4441
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
