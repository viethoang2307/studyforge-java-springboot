package dev.studyforge.streams;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public record Transaction(
        String id,
        String accountId,
        TransactionType type,
        BigDecimal amount,
        LocalDate occurredOn,
        String category
) {
    public Transaction {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(accountId, "accountId must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(occurredOn, "occurredOn must not be null");
        Objects.requireNonNull(category, "category must not be null");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
