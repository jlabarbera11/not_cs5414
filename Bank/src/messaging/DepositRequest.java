package messaging;

public class DepositRequest extends RequestClient {

    private Integer acnt;
    private Float amt;

    public DepositRequest(Integer acnt, Float amt, Integer ser_number) {
        this.acnt = acnt;
        this.amt = amt;
        this.ser_number = ser_number;
    }

    public Integer GetAcnt() {
        return this.acnt;
    }

    public Float GetAmt() {
        return this.amt;
    }

    @Override
    public String toString()
    {
        return String.format("Deposit = Account: %d, Amount: %.2f, Serial: %d", acnt, amt,ser_number);
    }

}
