package messaging;

public class ResponseBackup extends BackupMessage {
    private Map bank_accounts;
    private Map transactions;

    public ResponseBackup(Integer branch, Integer replica, Map bank_accounts, Map transactions) {
        this.branch = branch;
        this.replica = replica;
        this.bank_accounts = bank_accounts;
        this.transactions = transactions;
    }
}

