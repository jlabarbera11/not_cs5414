package messaging;

import server.AccountNumber;
import server.BankAccount;
import java.util.Map;
import java.util.Set;

public class RecoverReplicaResponse extends ResponseReplica {
    private Set<Integer> backups;
    private Map<AccountNumber, BankAccount> bankaccounts;
    private Map<Integer, RequestClient> waiting_clients;

    public RecoverReplicaResponse(String r, Set<String> backups, Map<AccountNumber, BankAccount> bankaccounts, Map<Integer, RequestClient> waiting_clients) {
        this.replica = r;
        this.backups = backups;
        this.bankaccounts = bankaccounts;
        this.waiting_clients = waiting_clients;
    }

    public Set<Integer> GetBackups() {
        return this.backups;
    }

    public Map<AccountNumber, BankAccount> GetBankAccounts() {
        return this.bankaccounts;
    }
    
    public Map<Integer, RequestClient> GetWaitingClients() {
        return this.waiting_clients;
    }
}

