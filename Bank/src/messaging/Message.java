package messaging;

import java.io.Serializable;

public class Message implements Serializable {

    //These refer to the sender of the message
    protected Integer branch;
    protected Integer replica;

    public Integer GetBranch() {
        return this.branch;
    }

    public Integer GetReplica() {
        return this.replica;
    }

}
