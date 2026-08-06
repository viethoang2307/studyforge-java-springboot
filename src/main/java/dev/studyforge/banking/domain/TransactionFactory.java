package dev.studyforge.banking.domain;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public final class TransactionFactory {
    private final Clock clock; private final Supplier<UUID> ids;
    public TransactionFactory(Clock clock, Supplier<UUID> ids) { this.clock = Objects.requireNonNull(clock); this.ids = Objects.requireNonNull(ids); }
    public Transaction deposit(AccountId to, Money amount) { return create(TransactionType.DEPOSIT, null, to, amount, Money.zero(amount.currency())); }
    public Transaction withdrawal(AccountId from, Money amount, Money fee) { return create(TransactionType.WITHDRAWAL, from, null, amount, fee); }
    public Transaction transfer(AccountId from, AccountId to, Money amount, Money fee) { return create(TransactionType.TRANSFER, from, to, amount, fee); }
    private Transaction create(TransactionType type, AccountId from, AccountId to, Money amount, Money fee) {
        return new Transaction(ids.get(), type, from, to, amount, fee, Instant.now(clock));
    }
}
