package messaging;

public class SnapshotMessage extends Message {

    protected Integer sender;
    protected Integer ID;

    public SnapshotMessage(Integer branch, Integer ID) {
        this.sender = branch;
        this.ID = ID;
    }

    public Integer getSender() {
        return this.sender;
    }

    public Integer getID() {
        return this.ID;
    }
}

