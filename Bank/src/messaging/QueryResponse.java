package messaging;

public class QueryResponse extends ResponseClient {

    private Float balance;

    public QueryResponse(String failure_reason) {
        this.success = false;
        this.failure_reason = failure_reason;
    }

    public QueryResponse(Float balance) {
        this.success = true;
        this.balance = balance;
    }

    public Float getBalance() {
        return this.balance;
    }
}
