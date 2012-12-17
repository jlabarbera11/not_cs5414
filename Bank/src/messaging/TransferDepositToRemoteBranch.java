package messaging;

public class TransferDepositToRemoteBranch extends RequestClient {
    
    private Integer acnt = null;
    private Float amt = null;

    public TransferDepositToRemoteBranch(Integer acnt, Float amt, Integer serial_number) {
        this.acnt = acnt;
        this.amt = amt;
        this.serial_number = serial_number;
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
        return String.format("DepositFromTransfer = Sender: %d, Account: %d, Amount: %.2f, Serial: %d", branch, acnt, amt, serial_number);
    }
}
