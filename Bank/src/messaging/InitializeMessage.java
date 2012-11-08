package messaging;

public class InitializeMessage extends Message {

    public InitializeMessage(Integer branch, Integer replica) {
        this.branch = branch;
        this.replica = replica;
    }

}

