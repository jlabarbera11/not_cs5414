package messaging;

public class ResponseClient extends ClientMessage {

    protected Boolean success = null;
    protected String failure_reason= null;

    public boolean GetSuccess() {
        return this.success;
    }

    public String GetFailureReason() {
        return this.failure_reason;
    }
}
