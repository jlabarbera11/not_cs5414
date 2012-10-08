package messaging;

public class DepositResponse extends Response {

    private Float balance;

    public DepositResponse(String failure_reason) {
        this.success = false;
        this.failure_reason = failure_reason;
    }

    public DepositResponse(Float balance) {
        this.success = true;
        this.balance = balance;
    }
    
    public Float getBalance() {
        return this.balance;
    }
}
