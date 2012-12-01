package messaging;

public class RequestBackup extends BackupMessage {
    Message message;

    public RequestBackup(int r, Message m) {
        this.replica = r;
        this.message = m;
    }

    public Message GetMessage() {
        return this.message;
    }
}

