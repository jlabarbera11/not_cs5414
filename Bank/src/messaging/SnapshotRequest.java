package messaging;

public class SnapshotRequest extends RequestClient {

    private Integer ID;

    public SnapshotRequest(Integer ID) {
        this.ID = branch*1000000 + ID;
    }

    public Integer getID() {
        return this.ID;
    }
}

