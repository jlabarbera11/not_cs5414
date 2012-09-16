package messaging;

import java.io.Serializable;

public class MessageResponse implements Serializable {

    protected Boolean success = null;
    protected String failure_reason = null;
    protected Float balance = null;

    public MessageResponse(String failure_reason) {
        this.success = false;
        this.failure_reason = failure_reason;
    }

    public MessageResponse(Float balance) {
        this.success = true;
        this.balance = balance;
    }
    
    public Boolean getSuccess() {
        return this.success;
    }

    public Float getBalance() {
        return this.balance;
    }

    public String getFailureReason() {
        return this.failure_reason;
    }
}
