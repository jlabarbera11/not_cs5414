package messaging;

public class TransferRequest extends RequestClient {
    
    private Integer acnt;
    private Float amt;
    private String dest_branch;
    private Integer dest_acnt;


    public TransferRequest(String dest_branch, Integer src_acnt, Integer dest_acnt, Float amt, Integer serial_number) {
        this.dest_branch = dest_branch;
        this.acnt = src_acnt;
        this.dest_acnt = dest_acnt;
        this.amt = amt;
        this.serial_number = serial_number;
    }

    public String GetDestBranch() {
        return this.dest_branch;
    }

    public Integer GetSrcAcnt() {
        return this.acnt;
    }

    public Integer GetDestAcnt() {
        return this.dest_acnt;
    }

    public Integer GetAcnt() {
        return this.acnt;
    }

    public Float GetAmt() {
        return this.amt;
    }
}

