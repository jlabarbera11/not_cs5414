package messaging;

public class Ping extends Message {

  public Integer jid;
  public ReplicaID rid;
  public Integer fid;

  public Ping(Integer jid, Object id) {
    this.jid = jid;
    if (id instanceof ReplicaID)
      this.rid = rid;
    else if (id instanceof Integer)
      this.fid = fid;
  }

  public Ping(Integer jid, ReplicaID rid) {
    this.jid = jid;
    this.rid = rid;
  }

  public Ping(Integer jid, Integer fid) {
    this.jid = jid;
    this.fid = fid;
  }

}
