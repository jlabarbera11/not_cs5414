package messaging;

public class WithdrawRequest extends Request {

    private Integer acnt;
    private Integer ser_number;
    private Float amt;

    public WithdrawRequest(Integer acnt, Float amt, Integer ser_number) {
        this.acnt = acnt;
        this.amt = amt;
        this.ser_number = ser_number;
    }

    public Integer getAcnt() {
        return this.acnt;
    }
    
    public Integer getSerNumber() {
        return this.ser_number;
    }

    public Float getAmt() {
        return this.amt;
    }
}
