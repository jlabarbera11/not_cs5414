package client.test;

import server.Server;

public class ServerTestThread extends Thread {
	
	private int number;
	
	public ServerTestThread(int i) {
		this.number=i;
	}

	public void run() {
		Server server = new Server(number);
		server.run();
		
	}
	
	public static void main(String[] args){
		ServerTestThread thread = new ServerTestThread(1);
		thread.run();
	}
	
}
