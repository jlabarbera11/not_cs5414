package messaging;

import java.io.Serializable;

public class MessageRequest implements Serializable {

    protected Integer acnt;
    protected Integer ser_number;

    public Integer getAcnt() {
        return this.acnt;
    }

    public Integer getSerNumber() {
        return this.ser_number;
    }
}
