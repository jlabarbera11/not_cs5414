package messaging;

public class TransferResponse extends MessageResponse {

    public TransferResponse(Boolean success) {
        super(success);
    }

    public TransferResponse(Boolean success, Float balance) {
        super(success, balance);
    }
}
