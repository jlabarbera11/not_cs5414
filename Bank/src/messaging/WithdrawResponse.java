package messaging;

public class WithdrawResponse extends MessageResponse {

    public WithdrawResponse(Boolean success) {
        super(success);
    }

    public WithdrawResponse(Boolean success, Float balance) {
        super(success, balance);
    }
}
