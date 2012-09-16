package messaging;

public class TransferResponse extends MessageResponse {

    public TransferResponse(String failure_reason) {
        super(failure_reason);
    }

    public TransferResponse(Float balance) {
        super(balance);
    }
}
