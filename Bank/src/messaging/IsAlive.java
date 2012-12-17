package messaging;

public class IsAlive extends Message {

  public ReplicaID rid;
  public Integer fid;

  public IsAlive(ReplicaID rid) {
    this.rid = rid;
  }

  public IsAlive(Integer fid) {
    this.fid = fid;
  }
}

