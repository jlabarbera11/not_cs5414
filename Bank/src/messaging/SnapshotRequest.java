package messaging;

import java.io.Serializable;

public class SnapshotRequest extends Request {

    private Integer branch;
    private Integer ID;

    public SnapshotRequest(Integer branch, Integer ID) {
        this.branch = branch;
        this.ID = ID;
    }
}

