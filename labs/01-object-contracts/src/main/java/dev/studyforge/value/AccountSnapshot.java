package dev.studyforge.value;

import java.util.List;
import java.util.Objects;

/** Immutable aggregate demonstrating defensive copying of a collection. */
public final class AccountSnapshot {
    private final AccountId accountId;
    private final List<Money> transactions;

    public AccountSnapshot(AccountId accountId, List<Money> transactions) {
        this.accountId = Objects.requireNonNull(accountId, "accountId must not be null");
        this.transactions = List.copyOf(
                Objects.requireNonNull(transactions, "transactions must not be null"));
    }

    public AccountId accountId() {
        return accountId;
    }

    public List<Money> transactions() {
        return transactions;
    }
}
