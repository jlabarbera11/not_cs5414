package jvm;

import messaging.ReplicaID;
import messaging.NewMessaging;
import messaging.Ping;

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
        nm.broadcastToAllFDS(new Ping(id));
        Thread.sleep(1000);
      } catch(Exception e) {}
    }
  }
}

