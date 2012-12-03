package messaging;

public class StillAlive extends Message {

  public ReplicaID rid;
  public Integer fid;

  public StillAlive(Object id) {
    if (id instanceof ReplicaID)
      this.rid = rid;
    else if (id instanceof Integer)
      this.fid = fid;
  }

  public StillAlive(ReplicaID rid) {
    this.rid = rid;
  }

  public StillAlive(Integer fid) {
    this.fid = fid;
  }

}
