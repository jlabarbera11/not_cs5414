package messaging;

import java.io.Serializable;

public class InitializeRequest extends Request {

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

