package messaging;

public class DepositRequest extends Message {

    private Integer acnt;
    private Float amt;
    private Integer ser_number;

    public DepositRequest(Integer acnt, Float amt, Integer ser_number) {
        this.acnt = acnt;
        this.amt = amt;
        this.ser_number = ser_number;
    }

    public Integer geAcnt() {
        return this.acnt;
    }

    public Integer getAmt() {
        return this.amt;
    }

    public Integer getSerNumber() {
        return this.ser_number;
    }
}
