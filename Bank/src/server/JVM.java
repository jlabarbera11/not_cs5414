package server;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import messaging.ReplicaID;
import messaging.ReplicaInfo;

//JVM is used to hold a bunch of executing branch replicas as well as a bunch of 

public class JVM {
	//jvmInfo maps a jvmID to set of replicaIDs running on that jvm
	Map<Integer, Set<ReplicaID>> jvmInfo = new HashMap<Integer, Set<ReplicaID>>();
	public static String jvmfile = "jvmInfo.txt";
	int jvmID;
	
	//used fields:
	Set<ReplicaID> myReplicas = new HashSet<ReplicaID>();
	
	//TODO: this is probably unnecessary and should be removed
	private void readjvmInfo(){
		System.out.println("reading jvmInfo...");
        try {
            Scanner scanner = new Scanner(new File(jvmfile));
            while (scanner.hasNextLine()) {
                String[] line = scanner.nextLine().split(" ");
                Integer currentjvmID = Integer.parseInt(line[0]);
                Set<ReplicaID> replicas = new HashSet<ReplicaID>();
                for (int i=1; i<line.length; i++){
                	String entry = line[i];
                	ReplicaID currentReplica = new ReplicaID(Integer.parseInt(entry.substring(0,2)), Integer.parseInt(entry.substring(3,5)));
                	replicas.add(currentReplica);
                }
                jvmInfo.put(currentjvmID, replicas);
            }
            scanner.close();
        } catch (Exception e) {
        	System.out.println("reading jvmInfo failed");
        	e.printStackTrace();
        	return;
        } 
	}
	
	public JVM(String[] args){
		//this.jvmID = id;
		//readjvmInfo();
		for (String entry : args){
			myReplicas.add(new ReplicaID(Integer.parseInt(entry.substring(0,2)), Integer.parseInt(entry.substring(3,5))));
		}
	}
	
	public void run(){
		for (ReplicaID entry : myReplicas){
			Server server = new Server(entry.branchNum, entry.replicaNum);
			server.run();
			System.out.println("started server " + entry.toString());
		}
	}
	
    public static void main(String args[]) {
        JVM jvm = new JVM(args);
        jvm.run();
    }
	
}
