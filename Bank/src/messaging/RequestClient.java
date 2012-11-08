package messaging;

public class RequestClient extends ClientMessage {
    protected Integer serial_number;

    public Integer GetSerialNumber() {
        return this.serial_number;
    }
}
