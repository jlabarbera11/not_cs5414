package messaging;

public class Response extends Message {

    protected Boolean success = null;
    protected String failure_reason = null;

    public Boolean getSuccess() {
        return this.success;
    }

    public String getFailureReason() {
        return this.failure_reason;
    }
}
