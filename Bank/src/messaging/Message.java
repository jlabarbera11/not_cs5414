package messaging;

import java.io.Serializable;

public class Message implements Serializable {

    //These refer to the sender of the message
    protected String branch;
    protected String replica;

    public String GetBranch() {
        return this.branch;
    }

    public String GetReplica() {
        return this.replica;
    }

}
