package server;

import messaging.ReplicaID;
import messaging.StillAlive;
import messaging.IsAlive;
import messaging.Alive;
import messaging.NotAlive;

import java.util.Map;
import java.util.HashMap;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FailureDetector implements Runnable {

  /*<JVM, <ReplicaID, Timestamp>>*/
  Map<Integer, Map<ReplicaID, Long>> rts = null;

  /*<FDSID, Timestamp>>*/
  Map<Integer, Long> fts = null;

  Integer fid = null;
  ServerSocket ss = null;

  public FailureDetector(Integer fid, Integer port) {
    this.fid = fid;
    this.rts = new HashMap<Integer, Map<ReplicaID, Long>>();
    this.fts = new HashMap<Integer, Long>();
    try {
      this.ss = new ServerSocket(port);
    } catch(Exception e) {System.out.println("ERROR: Could not create Failure Detector");}
  }

  public void run() {
    new Thread(new Pinger(fid)).start();

    while(true) {
      try {
        Socket s = this.ss.accept();
        Object o = new ObjectInputStream(s.getInputStream()).readObject();

        if(o instanceof messaging.StillAlive)
          this._stillAlive((StillAlive)o);
        else if(o instanceof messaging.IsAlive)
          this._isAlive(s, (IsAlive)o);
      } catch(Exception e) {System.out.println("ERROR: Could not accept message");}
    }
  }

  private void _stillAlive(StillAlive o) {
    if(o.rid != null) /*Replica has told us it's alive*/
      this.rts.get(o.rid.branchNum).put(o.rid, new Long(System.currentTimeMillis()));
    else if(o.fid != null) /*FDS has told us it's alive*/
      this.fts.put(o.fid, new Long(System.currentTimeMillis()));
  }

  private void _isAlive(Socket s, IsAlive o) throws Exception {

    boolean failed = false;

    /*check if everything in jvm is alive*/
    if(!this.rts.containsKey(o.rid.branchNum))
      failed = true;
    for(Long ts : this.rts.get(o.rid.branchNum).values())
      if( ts + 2*60*1000 > System.currentTimeMillis())
        failed = true;
    if(!this.fts.containsKey(o.rid.branchNum))
      failed = true;
    if(this.fts.get(o.rid.branchNum)+ 2*60*1000 > System.currentTimeMillis())
      failed = true;

    if(!failed)
      new ObjectOutputStream(s.getOutputStream()).writeObject(new Alive());
    else {
      this.rts.remove(o.rid.branchNum);
      this.fts.remove(o.rid.branchNum);
      new ObjectOutputStream(s.getOutputStream()).writeObject(new NotAlive());
    }
    s.close();
  }

}
