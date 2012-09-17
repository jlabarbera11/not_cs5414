package messaging;

public class DepositFromTransferResponse extends MessageResponse {

    public DepositFromTransferResponse(String failure_reason) {
        super(failure_reason);
    }

    public DepositFromTransferResponse(Float balance) {
        super(balance);
    }
}

