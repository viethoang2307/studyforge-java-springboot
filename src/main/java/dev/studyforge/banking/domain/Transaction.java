package dev.studyforge.banking.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public record Transaction(UUID id, TransactionType type, AccountId source, AccountId destination,
                          Money amount, Money fee, Instant occurredAt) {
    public Transaction {
        Objects.requireNonNull(id); Objects.requireNonNull(type); Objects.requireNonNull(amount);
        Objects.requireNonNull(fee); Objects.requireNonNull(occurredAt);
        if (!amount.isPositive()) throw new IllegalArgumentException("Transaction amount must be positive");
        if (fee.amount().signum() < 0) throw new IllegalArgumentException("Fee must not be negative");
        if (!amount.currency().equals(fee.currency())) throw new CurrencyMismatchException(amount.currency(), fee.currency());
    }
    public Optional<AccountId> sourceAccount() { return Optional.ofNullable(source); }
    public Optional<AccountId> destinationAccount() { return Optional.ofNullable(destination); }
}
