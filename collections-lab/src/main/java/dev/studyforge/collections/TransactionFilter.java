package dev.studyforge.collections;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionFilter(
        String accountId,
        TransactionType type,
        BigDecimal minimumAmount,
        Instant fromInclusive,
        Instant toExclusive
) {
    public static TransactionFilter all() {
        return new TransactionFilter(null, null, null, null, null);
    }

    public boolean matches(Transaction transaction) {
        return (accountId == null || accountId.equals(transaction.accountId()))
                && (type == null || type == transaction.type())
                && (minimumAmount == null || transaction.amount().compareTo(minimumAmount) >= 0)
                && (fromInclusive == null || !transaction.occurredAt().isBefore(fromInclusive))
                && (toExclusive == null || transaction.occurredAt().isBefore(toExclusive));
    }
}
