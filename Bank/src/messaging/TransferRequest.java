package messaging;

public class TransferRequest extends MessageRequest {

    private Integer dest_acnt;
    private Float amt;

    public TransferRequest(Integer src_acnt, Integer dest_acnt, Float amt, Integer ser_number) {
        this.acnt = src_acnt;
        this.dest_acnt = dest_acnt;
        this.amt = amt;
        this.ser_number = ser_number;
    }

    public Integer getSrcAcnt() {
        return this.acnt;
    }

    public Integer getDestAcnt() {
        return this.dest_acnt;
    }

    public Float getAmt() {
        return this.amt;
    }
}

