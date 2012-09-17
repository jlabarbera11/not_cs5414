package massaging;

public class DepositFromMessageResponse extends MessageResponse {

    public DepositFromMessageResponse(String failure_reason) {
        super(failure_reason);
    }

    public DepositFromMessageResponse(Float balance) {
        super(balance);
    }
}

