package messaging;

public class WithdrawResponse extends ResponseClient {

    private Float balance;

    public WithdrawResponse(String failure_reason) {
        this.success = false;
        this.failure_reason = failure_reason;
    }

    public WithdrawResponse(Float balance) {
        this.success = true;
        this.balance = balance;
    }
    
    public Float getBalance() {
        return this.balance;
    }
}
