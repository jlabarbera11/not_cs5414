package messaging;

public class DepositFromTransferMessage extends Message {
    
    private Integer acnt;
    private Float amt;
    private Integer ser_number;

    public DepositFromTransferMessage(Integer to_acnt, Integer acnt, Float amt, Integer ser_number) {
        this.sender = to_acnt;
        this.acnt = acnt;
        this.amt = amt;
        this.ser_number = ser_number;
    }

    public Integer getAcnt() {
        return this.acnt;
    }

    public Float getAmt() {
        return this.amt;
    }

    public Integer getSerNumber() {
        return this.ser_number;
    }
}
