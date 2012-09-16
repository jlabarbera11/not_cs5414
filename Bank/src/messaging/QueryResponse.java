package messaging;

public class QueryResponse extends MessageResponse {

    public QueryResponse(Boolean success) {
        super(success);
    }

    public QueryResponse(Boolean success, Float balance) {
        super(success, balance);
    }
}
