package jvm;

import java.awt.List;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import server.Server;

import messaging.Messaging;
import messaging.ReplicaID;
import messaging.ReplicaInfo;
import messaging.ShutdownMessage;
import messaging.StatusQuery;

//JVM is used to hold a bunch of executing branch replicas as well as a bunch of

public class JVM extends Thread {
	//jvmInfo maps a jvmID to set of replicaIDs running on that jvm
	Map<Integer, Set<ReplicaID>> jvmInfo = new HashMap<Integer, Set<ReplicaID>>();
	public static String jvmfile = "resolvers/jvmInfo.txt";
	public static String jvmResolverFile = "resolvers/jvmResolver.txt";
	int jvmID;
	private Messaging messaging;
	Set<Server> servers = new HashSet<Server>();
	private Map<Integer, ReplicaInfo> jvmResolver;
	private volatile boolean running = true;
	private FailureDetector fds;
    public boolean debug = false;
    
    public void printIfDebug(String s){
    	if (debug){
    		System.out.println(s);
    	}
    }
	
	public static Map<Integer, ReplicaInfo> readJvmResolver(){
		Map<Integer, ReplicaInfo> output = new HashMap<Integer, ReplicaInfo>();
        try {
            Scanner scanner = new Scanner(new File(jvmResolverFile));
            while (scanner.hasNextLine()) {
                String[] line = scanner.nextLine().split(" ");
                Integer currentjvmID = Integer.parseInt(line[0]);
                Integer port = Integer.parseInt(line[2]);
                output.put(currentjvmID, new ReplicaInfo(port, line[1]));
            }
            scanner.close();
        } catch (Exception e) {
        	System.out.println("reading jvmResolver failed");
        }
        return output;
	}

	public static Map<Integer, Set<ReplicaID>> readjvmInfo(){
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
        }
        return jvmInfo;
	}

	public JVM(int id){
		this.jvmID = id;
		this.jvmInfo = readjvmInfo();
		this.messaging = new Messaging();
		this.jvmResolver = readJvmResolver();
	}

	public void run(){
		Set<ReplicaID> myReplicas = jvmInfo.get(this.jvmID);

		for (ReplicaID entry : myReplicas){
			Server server = new Server(entry.branchNum, entry.replicaNum);
			server.start();
			servers.add(server);
			printIfDebug("started server " + entry.toString());
		}

		fds = new FailureDetector(jvmID, messaging.getFdsPort(jvmID).port); //jvmID = fdsID
		fds.start();
		
		ServerSocket serversocket = null;
		try {
			 serversocket = new ServerSocket(jvmResolver.get(jvmID).port);
			 printIfDebug("jvm " + jvmID + " listening on " + jvmResolver.get(jvmID).port);
		} catch (IOException e) {
			printIfDebug("error creating jvm serversocket");
			e.printStackTrace();
		}
		while(running){
			try {
				printIfDebug("jvm " + jvmID + "about to accept connections");
				Socket socket = serversocket.accept();
				printIfDebug("jvm accepted connection");
				ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
				printIfDebug("jvm got input stream");
				ShutdownMessage sm = (ShutdownMessage)ois.readObject();
				printIfDebug("jvm got shutdown message");
				kill();
				ois.close();
				serversocket.close();
			} catch (Exception e){
				printIfDebug("error in jvm loop");
				e.printStackTrace();
			}
		}
	}

    public static void main(String args[]) {
        JVM jvm = new JVM(Integer.parseInt(args[0]));
        jvm.run();
    }
    
	public void kill(){
		printIfDebug("jvm is shutting down");
		running = false;
    	for (Server server : servers){
    		server.kill();
    	}
    	fds.kill();
    }

}
