package jvm;

import messaging.ReplicaID;
import messaging.NewMessaging;
import messaging.StillAlive;

import java.net.Socket;

//Periodically sends a Still Alive packet to all failure detectors
public class Pinger extends Thread {

  Object id;
  NewMessaging nm;

  public Pinger(ReplicaID rid) {
    nm = new NewMessaging();
    this.id = rid;
  }

  public Pinger(Integer fid) {
    nm = new NewMessaging();
    this.id = fid;
  }

  public void run() {
    while(true) {
      try {
        Thread.sleep(60);
        nm.broadcastToAllFDS(new StillAlive(id));
      } catch(Exception e) {}
    }
  }
}

