package messaging;

public class TransferResponse extends ResponseClient {

    private Float balance;

    public TransferResponse(String failure_reason) {
        this.success = false;
        this.failure_reason = failure_reason;
    }

    public TransferResponse(Float balance) {
        this.success = true;
        this.balance = balance;
    }
    
    public Float getBalance() {
        return this.balance;
    }
}
