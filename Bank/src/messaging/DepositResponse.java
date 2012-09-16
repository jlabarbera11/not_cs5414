package messaging;

public class DepositResponse extends MessageResponse {

    public DepositResponse(String failure_reason) {
        super(failure_reason);
    }

    public DepositResponse(Float balance) {
        super(balance);
    }
}
