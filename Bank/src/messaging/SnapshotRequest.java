package messaging;

public class SnapshotRequest extends Request {

    private Integer ID;

    public SnapshotRequest(Integer branch, Integer ID) {
        this.ID = ID;
    }

    public Integer getID() {
        return this.ID;
    }
}

