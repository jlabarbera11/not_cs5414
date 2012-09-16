package messaging;

public class WithdrawResponse extends MessageResponse {

    public WithdrawResponse(String failure_reason) {
        super(failure_reason);
    }

    public WithdrawResponse(Float balance) {
        super(balance);
    }
}
