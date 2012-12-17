package messaging;

import java.io.Serializable;

public class Message implements Serializable {

    //These refer to the sender of the message
    protected int branch;
    protected int replica;

    public int GetBranch() {
        return this.branch;
    }

    public int GetReplica() {
        return this.replica;
    }

}
