package messaging;

public class Ping extends Message {

  public ReplicaID rid;
  public Integer fid;

  public Ping(Object id) {
    if (id instanceof ReplicaID)
      this.rid = rid;
    else if (id instanceof Integer)
      this.fid = fid;
  }

  public Ping(ReplicaID rid) {
    this.rid = rid;
  }

  public Ping(Integer fid) {
    this.fid = fid;
  }

}
