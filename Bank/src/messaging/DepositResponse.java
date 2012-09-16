package messaging;

public class DepositResponse extends MessageResponse {

    public DepositResponse(Boolean success) {
        super(success);
    }

    public DepositResponse(Boolean success, Float balance) {
        super(success, balance);
    }
}
