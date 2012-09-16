package messaging;

public class QueryRequest extends MessageRequest {

    public QueryRequest(Integer acnt, Integer ser_number) {
        this.acnt = acnt;
        this.ser_number = ser_number;
    }
}
