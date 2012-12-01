package messaging;

public class InitializeMessage extends Message {

    public InitializeMessage(int branch, int replica) {
        this.branch = branch;
        this.replica = replica;
    }

}

