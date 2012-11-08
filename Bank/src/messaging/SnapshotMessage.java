package messaging;

public class SnapshotMessage extends Message {

    private Integer ID;

    public SnapshotMessage(Integer branch, Integer ID) {
        this.branch = branch;
        this.ID = ID;
    }

    public Integer getID() {
        return this.ID;
    }
}

