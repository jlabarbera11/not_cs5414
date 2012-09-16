package messaging;

import java.io.Serializable;

public class MessageResponse implements Serializable {

    protected Boolean success = null;
    protected String failure_reason = null;
    protected Float balance = null;

    public MessageResponse(Boolean success) {
        this.success = success;
    }

    public MessageResponse(Boolean success, Float balance) {
        this.success = success;
        this.balance = balance;
    }

    public Boolean getSuccess() {
        return this.success;
    }

    public Float getBalance() {
        return this.balance;
    }

    public void addFailureReason(String failure_reason) {
        this.failure_reason = failure_reason;
}
