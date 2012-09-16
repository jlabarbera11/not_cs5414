package messaging;

public class DepositRequest extends MessageRequest {

    private Float amt;

    public DepositRequest(Integer acnt, Float amt, Integer ser_number) {
        this.acnt = acnt;
        this.amt = amt;
        this.ser_number = ser_number;
    }

    public Float getAmt() {
        return this.amt;
    }
}
