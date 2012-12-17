package messaging;

public class Ping extends Message {

  public Integer jid;
  public ReplicaID rid;
  public Integer fid;

  public Ping(Integer jid, Object id) {
    this.jid = jid;
    if (id instanceof ReplicaID)
      this.rid = (ReplicaID)id;
    else if (id instanceof Integer)
      this.fid = (Integer)id;
  }

}
