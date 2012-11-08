package messaging;

import java.util.Map;

public class ResponseBackup extends BackupMessage {
    Message message;

    public ResponseBackup(Integer r, Message m) {
        this.replica = r;
        this.message = m;
    }

    public Message GetMessage() {
        return this.message;
    }
}

