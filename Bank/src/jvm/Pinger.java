package jvm;

import messaging.ReplicaID;
import messaging.Messaging;
import messaging.Ping;

import java.net.Socket;

//Periodically sends a Still Alive packet to all failure detectors
public class Pinger extends Thread {

  Integer jid;
  Object id;
  Messaging nm;
  volatile boolean running = true;

  public Pinger(Integer jid, ReplicaID rid) {
    nm = new Messaging();
    this.jid = jid;
    this.id = rid;
  }

  public Pinger(Integer jid, Integer fid) {
    nm = new Messaging();
    this.jid = jid;
    this.id = fid;
  }
  
  public void kill(){
	  System.out.println("killing pinger");
	  running = false;
  }

  public void run() {
    while(running) {
      try {
        nm.broadcastToAllFDS(new Ping(jid, id));
        Thread.sleep(1000);
      } catch(Exception e) {}
    }
    System.out.println("pinger has quit!");
  }
}

