package messaging;

public class InitializeMessage extends Message {

    public InitializeMessage(String branch, String replica) {
        this.branch = branch;
        this.replica = replica;
    }

}

