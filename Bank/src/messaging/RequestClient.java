package messaging;

public class RequestClient extends ClientMessage {
    private Integer serial_number;

    public Integer GetSerialNumber() {
        return this.serial_number;
    }
}
