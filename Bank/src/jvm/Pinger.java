package jvm;

import messaging.ReplicaID;
import messaging.NewMessaging;
import messaging.Ping;

import java.net.Socket;

//Periodically sends a Still Alive packet to all failure detectors
public class Pinger extends Thread {

  Integer jid;
  Object id;
  NewMessaging nm;

  public Pinger(Integer jid, ReplicaID rid) {
    nm = new NewMessaging();
    this.jid = jid;
    this.id = rid;
  }

  public Pinger(Integer jid, Integer fid) {
    nm = new NewMessaging();
    this.jid = jid;
    this.id = fid;
  }

  public void run() {
    while(true) {
      try {
        nm.broadcastToAllFDS(new Ping(jid, id));
        Thread.sleep(1000);
      } catch(Exception e) {}
    }
  }
}

