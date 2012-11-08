package messaging;

public class InitializeRequest extends BranchMessage {

    private Messaging.Type type;
    private Integer branch;

    public InitializeRequest(Messaging.Type type, Integer branch) {
        this.type = type;
        this.branch = branch;
    }

    public Messaging.Type getType() {
        return this.type;
    }

    public Integer getBranch() {
        return this.branch;
    }
}

