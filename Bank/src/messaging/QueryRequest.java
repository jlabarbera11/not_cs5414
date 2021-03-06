package messaging;

public class QueryRequest extends RequestClient {
    
    private Integer acnt;
    
    public QueryRequest(Integer acnt, Integer serial_number) {
        this.acnt = acnt;
        this.serial_number = serial_number;
    }

    public Integer GetAcnt() {
        return this.acnt;
    }

    @Override
    public String toString()
    {
        return String.format("Query = Account: %d, Serial: %d", acnt, serial_number);
    }

}
