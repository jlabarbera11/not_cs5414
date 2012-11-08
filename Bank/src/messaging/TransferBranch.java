package messaging;

public class TransferBranch extends BranchMessage {
    
    private Integer acnt = null;
    private Float amt = null;
    private Integer ser_number = null;

    public TransferBranch(Integer to_acnt, Integer acnt, Float amt, Integer ser_number) {
        this.branch = acnt;
        this.acnt = to_acnt;
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
        return String.format("DepositFromTransfer = Sender: %d, Account: %d, Amount: %.2f, Serial: %d", branch, acnt, amt, ser_number);
    }
}
