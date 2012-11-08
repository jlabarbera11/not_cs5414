package messaging;

import java.util.Map;

public class ResponseBackup extends BackupMessage {
    Message message;

    public ResponseBackup(String r, Message m) {
        this.replica = r;
        this.message = m;
    }

    public Message GetMessage() {
        return this.message;
    }
}

