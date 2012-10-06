package messaging;

import java.io.Serializable;

public class Snapshot implements Serializable {

    protected Integer sender;
    protected Integer ID;

    public Integer getSender() {
        return this.sender;
    }

    public Integer getID() {
        return this.ID;
    }
}

