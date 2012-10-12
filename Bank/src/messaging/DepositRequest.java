package messaging;

public class DepositRequest extends Request {

    private Integer acnt;
    private Integer ser_number;
    private Float amt;

    public DepositRequest(Integer acnt, Float amt, Integer ser_number) {
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

    @Override
    public String toString()
    {
        return String.format("Deposit = Account: %d, Amount: %.2f, Serial: %d", acnt, amt,ser_number);
    }

}
