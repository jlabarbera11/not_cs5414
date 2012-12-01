package messaging;

public class RequestBackup extends BackupMessage {
    Message message;

    public RequestBackup(int branch, int replica, Message m) {
        this.replica = replica;
        this.branch = branch;
        this.message = m;
    }

    public Message GetMessage() {
        return this.message;
    }
}

