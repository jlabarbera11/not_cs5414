package messaging;

public class TransferBranch extends BranchMessage {
    
    private Integer acnt = null;
    private Float amt = null;
    private Integer ser_number = null;

    public TransferBranch(Integer acnt, Float amt, Integer ser_number) {
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

    public Integer GetSerialNumber() {
        return this.ser_number;
    }

    @Override
    public String toString()
    {
        return String.format("DepositFromTransfer = Sender: %d, Account: %d, Amount: %.2f, Serial: %d", branch, acnt, amt, ser_number);
    }
}
