package com.studyforge.transfer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

final class InMemoryAccountRepository implements AccountRepository {
    private final Map<String, Account> accounts = new HashMap<>();

    InMemoryAccountRepository(Account... initialAccounts) {
        for (Account account : initialAccounts) {
            accounts.put(account.id(), account);
        }
    }

    @Override
    public Optional<Account> findById(String accountId) {
        return Optional.ofNullable(accounts.get(accountId));
    }

    @Override
    public void saveTransfer(Account source, Account destination) {
        accounts.put(source.id(), source);
        accounts.put(destination.id(), destination);
    }
}
