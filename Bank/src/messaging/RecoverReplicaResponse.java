package messaging;

import server.AccountNumber;
import server.BankAccount;
import java.util.Map;
import java.util.Set;

public class RecoverReplicaResponse extends ResponseReplica {
    private Set<String> backups;
    private Map<AccountNumber, BankAccount> bankaccounts;
    private Map<Integer, RequestClient> waiting_clients;

    public RecoverReplicaResponse(Set<String> backups, Map<AccountNumber, BankAccount> bankaccounts, Map<Integer, RequestClient> waiting_clients) {
        this.backups = backups;
        this.bankaccounts = bankaccounts;
        this.waiting_clients = waiting_clients;
    }

    public Set<String> GetBackups() {
        return this.backups;
    }

    public Map<AccountNumber, BankAccount> GetBankAccounts() {
        return this.bankaccounts;
    }
    
    public Map<Integer, RequestClient> GetWaitingClients() {
        return this.waiting_clients;
    }
}
