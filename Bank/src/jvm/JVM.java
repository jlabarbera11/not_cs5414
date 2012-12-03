package jvm;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import server.Server;

import messaging.ReplicaID;
import messaging.ReplicaInfo;

//JVM is used to hold a bunch of executing branch replicas as well as a bunch of 

public class JVM {
	//jvmInfo maps a jvmID to set of replicaIDs running on that jvm
	Map<Integer, Set<ReplicaID>> jvmInfo = new HashMap<Integer, Set<ReplicaID>>();
	public static String jvmfile = "jvmInfo.txt";
	int jvmID;

	public Map<Integer, Set<ReplicaID>> readjvmInfo(){
		System.out.println("reading jvmInfo...");
		Map<Integer, Set<ReplicaID>> jvmInfo = new HashMap<Integer, Set<ReplicaID>>();
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
        } 
        return jvmInfo;
	}
	
	public JVM(int id){
		this.jvmID = id;
		this.jvmInfo = readjvmInfo();
	}
	
	public void run(){
		Set<ReplicaID> myReplicas = jvmInfo.get(this.jvmID);
		
		for (ReplicaID entry : myReplicas){
			Server server = new Server(entry.branchNum, entry.replicaNum);
			server.start();
			System.out.println("started server " + entry.toString());
		}
	}
	
    public static void main(String args[]) {
        JVM jvm = new JVM(Integer.parseInt(args[0]));
        jvm.run();
    }
	
}
