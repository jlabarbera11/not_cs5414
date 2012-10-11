package messaging;

public class TransferRequest extends Request {
    
    private Integer acnt;
    private Integer ser_number;
    private Float amt;
    private Integer dest_branch;
    private Integer dest_acnt;

    public Integer getAcnt() {
        return this.acnt;
    }

    public Integer getSerNumber() {
        return this.ser_number;
    }

    public Float getAmt() {
        return this.amt;
    }

    public TransferRequest(Integer dest_branch, Integer src_acnt, Integer dest_acnt, Float amt, Integer ser_number) {
        this.dest_branch = dest_branch;
        this.acnt = src_acnt;
        this.dest_acnt = dest_acnt;
        this.amt = amt;
        this.ser_number = ser_number;
    }

    public Integer getDestBranch() {
        return this.dest_branch;
    }

    public Integer getSrcAcnt() {
        return this.acnt;
    }

    public Integer getDestAcnt() {
        return this.dest_acnt;
    }

}

