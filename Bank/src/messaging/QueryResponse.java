package messaging;

public class QueryResponse extends MessageResponse {

    public QueryResponse(String failure_reason) {
        super(failure_reason);
    }

    public QueryResponse(Float balance) {
        super(balance);
    }
}
