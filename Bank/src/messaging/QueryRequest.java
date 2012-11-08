package messaging;

public class QueryRequest extends RequestClient {
    
    private Integer acnt;
    private Integer ser_number;
    
    public QueryRequest(Integer acnt, Integer ser_number) {
        this.acnt = acnt;
        this.ser_number = ser_number;
    }

    public Integer getAcnt() {
        return this.acnt;
    }

    public Integer getSerNumber() {
        return this.ser_number;
    }

    @Override
    public String toString()
    {
        return String.format("Query = Account: %d, Serial: %d", acnt, ser_number);
    }

}
