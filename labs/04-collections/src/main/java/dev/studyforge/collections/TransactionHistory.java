package dev.studyforge.collections;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** In-memory history preserving insertion order and rejecting duplicate ids. */
public final class TransactionHistory {
    private final Map<UUID, Transaction> transactions = new LinkedHashMap<>();

    public void add(Transaction transaction) {
        Objects.requireNonNull(transaction, "transaction must not be null");
        if (transactions.putIfAbsent(transaction.id(), transaction) != null) {
            throw new IllegalArgumentException("duplicate transaction id: " + transaction.id());
        }
    }

    public List<Transaction> find(TransactionFilter filter, Comparator<Transaction> order) {
        Objects.requireNonNull(filter, "filter must not be null");
        Objects.requireNonNull(order, "order must not be null");
        return transactions.values().stream()
                .filter(filter::matches)
                .sorted(order)
                .toList();
    }

    public Map<TransactionType, List<Transaction>> groupByType() {
        Map<TransactionType, List<Transaction>> grouped = new EnumMap<>(TransactionType.class);
        transactions.values().forEach(transaction ->
                grouped.computeIfAbsent(transaction.type(), ignored -> new ArrayList<>()).add(transaction));
        grouped.replaceAll((type, values) -> List.copyOf(values));
        return Collections.unmodifiableMap(grouped);
    }

    public Map<String, BigDecimal> totalByAccount() {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        transactions.values().forEach(transaction -> totals.merge(
                transaction.accountId(), transaction.amount(), BigDecimal::add));
        return Collections.unmodifiableMap(totals);
    }

    public List<Transaction> insertionOrder() {
        return List.copyOf(transactions.values());
    }

    public int size() {
        return transactions.size();
    }
}
