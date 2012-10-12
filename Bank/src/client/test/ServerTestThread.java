package client.test;

import server.Server;

public class ServerTestThread extends Thread {
	
	public void run() {
		Server server = new Server(1);
		server.run();
		
	}
	
	public static void main(String[] args){
		ServerTestThread thread = new ServerTestThread();
		thread.run();
	}
	
}
