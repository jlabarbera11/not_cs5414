package messaging;

import java.io.Serializable;

public class InitializeRequest implements Serializable {

    private Messaging.Type type;

    public InitializeRequest(Messaging.Type type) {
        this.type = type;
    }

    public Messaging.Type getType() {
        return this.type;
    }
}

