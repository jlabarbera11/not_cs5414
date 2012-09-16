package messaging;

public class DepositResponse {

    private Boolean success = null;
    private Float balance = null;

    public DepositResponse(Boolean success) {
        this.success = success;
    }

    public DepositResponse(Booloean success, Float balance) {
        this.success = success;
        this.balance = balance;
    }

    public Boolean getSuccess() {
        return this.success;
    }

    public Float getBalance() {
        return this.balance;
    }
}
