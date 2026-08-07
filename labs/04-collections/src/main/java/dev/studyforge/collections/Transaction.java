package dev.studyforge.collections;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;

/** An immutable transaction whose identity is its id. */
public record Transaction(
        UUID id,
        String accountId,
        TransactionType type,
        BigDecimal amount,
        Instant occurredAt
) implements Comparable<Transaction> {

    public static final Comparator<Transaction> BY_AMOUNT_DESCENDING =
            Comparator.comparing(Transaction::amount).reversed()
                    .thenComparing(Transaction::occurredAt)
                    .thenComparing(Transaction::id);

    public static final Comparator<Transaction> BY_TIME =
            Comparator.comparing(Transaction::occurredAt)
                    .thenComparing(Transaction::id);

    public Transaction {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("accountId must not be blank");
        }
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }

    /** Natural order follows business identity and is therefore consistent with equals. */
    @Override
    public int compareTo(Transaction other) {
        return id.compareTo(other.id);
    }

    /** Monetary equality is numeric, so 10.0 and 10.00 match. */
    public boolean hasAmount(BigDecimal expected) {
        return amount.compareTo(expected) == 0;
    }

    @Override
    public boolean equals(Object candidate) {
        return this == candidate
                || candidate instanceof Transaction other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
