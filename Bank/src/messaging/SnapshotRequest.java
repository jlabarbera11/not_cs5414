package messaging;

public class SnapshotRequest extends Request {

    private Integer branch;
    private Integer ID;

    public SnapshotRequest(Integer branch, Integer ID) {
        this.branch = branch;
        this.ID = ID;
    }

    public Integer getID() {
        return this.ID;
    }
}

