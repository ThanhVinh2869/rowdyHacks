package engine;

import model.Account;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class AccountRegistry {
    private final HashMap<String, Account> accounts = new HashMap<>();

    public Account getOrCreate(String userId) {
        return accounts.computeIfAbsent(userId, Account::new);
    }

    public Collection<Account> getAll() {
        return Collections.unmodifiableCollection(accounts.values());
    }

    public int size() { return accounts.size(); }
}