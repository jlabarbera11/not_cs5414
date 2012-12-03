package jvm;

import messaging.ReplicaID;
import messaging.Ping;
import messaging.StatusQuery;
import messaging.StatusQueryResponse;

import oracle.Oracle;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.SocketException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class FailureDetector extends Thread {

  /*<JVM, <ReplicaID, Timestamp>>*/
  Map<Integer, Map<ReplicaID, Long>> rts = null;

  /*<FDSID, Timestamp>>*/
  Map<Integer, Long> fts = null;

  Integer portnum = null;
  Integer fid = null;
  ServerSocket ss = null;

  public FailureDetector(Integer fid, Integer port) {
    this.fid = fid;
    this.rts = new HashMap<Integer, Map<ReplicaID, Long>>();
    this.fts = new HashMap<Integer, Long>();
    this.portnum = port;

    for(Map.Entry<Integer, Set<ReplicaID>> entry : JVM.readjvmInfo().entrySet()) {
      Map<ReplicaID, Long> lm = new HashMap<ReplicaID, Long>();
      for(ReplicaID rid : entry.getValue())
        lm.put(rid, new Long(0));

      rts.put(entry.getKey(), lm);
      fts.put(entry.getKey(), new Long(0));

    }

    try {
      this.ss = new ServerSocket(port);
      ss.setReuseAddress(true);
    } catch(Exception e) {System.out.println("ERROR: Could not create Failure Detector");}
  }

  public void run() {
    Pinger pinger = new Pinger(fid, fid);
    pinger.start();
    System.out.println("Starting FDS " + fid + " on " + this.portnum);

    while(true) {
      try {
        Socket s = this.ss.accept();
        Object o = new ObjectInputStream(s.getInputStream()).readObject();

        if(o instanceof Ping)
          this._stillAlive((Ping)o);
        else if(o instanceof StatusQuery)
          this._isAlive(s, (StatusQuery)o);
      } catch(Exception e) {System.out.println("ERROR: " + e.toString()); e.printStackTrace();}
    }
  }

  private void _stillAlive(Ping o) {
    if(!this.rts.containsKey(o.jid)) /* Old pings from machines we've already considered failed */
      return;
    System.out.println("Got a ping from " + (o.rid != null ? "replica  " + (o.rid) : "FDS " + (o.fid)) + " on " + o.jid + " at " + System.currentTimeMillis());
    if(o.rid != null) /*Replica has told us it's alive*/
      this.rts.get(o.jid).put(o.rid, new Long(System.currentTimeMillis()));
    else if(o.fid != null) /*FDS has told us it's alive*/
      this.fts.put(o.fid, new Long(System.currentTimeMillis()));
  }

  private void _isAlive(Socket s, StatusQuery o) throws Exception {

    boolean failed = false;

    /*check if everything in jvm is alive*/
    if(!this.rts.containsKey(o.jvmOfInterest)) {
      System.out.println("No record of jvm " + o.jvmOfInterest + " in map");
      failed = true;
    } else {
      for(Map.Entry<ReplicaID, Long> entry : this.rts.get(o.jvmOfInterest).entrySet())
        if( entry.getValue() + 5*1000 < System.currentTimeMillis()) {
          System.out.println("replica " + entry.getKey() +  "on " + o.jvmOfInterest + " is too slow; last timestamp is " + entry.getValue() + ", now is " + System.currentTimeMillis());
          failed = true;
        }
    }
    if(!this.fts.containsKey(o.jvmOfInterest)) {
      System.out.println("No record of FDS " + o.jvmOfInterest + " in map");
      failed = true;
    } else if(this.fts.get(o.jvmOfInterest)+ 5*1000 < System.currentTimeMillis()) {
      System.out.println("FDS on " +  o.jvmOfInterest + " is too slow");
      failed = true;
    }

    System.out.println("FDS " + this.fid + " says " + o.jvmOfInterest +  " is " + (failed ? "failed" : "running"));

    if(!failed)
      new ObjectOutputStream(s.getOutputStream()).writeObject(new StatusQueryResponse(Oracle.replicaState.running, o.jvmOfInterest));
    else {
      this.rts.remove(o.jvmOfInterest);
      this.fts.remove(o.jvmOfInterest);
      new ObjectOutputStream(s.getOutputStream()).writeObject(new StatusQueryResponse(Oracle.replicaState.failed, o.jvmOfInterest));
    }
    s.close();
  }

}
