package messaging;

import java.io.Serializable;

public class Message implements Serializable {
    protected Integer sender;

    public Integer getSender() {
        return this.sender;
    }
}
