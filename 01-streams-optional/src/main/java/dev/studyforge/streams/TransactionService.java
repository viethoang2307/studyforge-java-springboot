package dev.studyforge.streams;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.toList;

public final class TransactionService {

    /**
     * Stream version: each business question is visible as one declarative pipeline.
     * Pipelines are side-effect free; all results are produced by terminal collectors.
     */
    public TransactionReport createReport(List<Transaction> transactions, BigDecimal largeAmountThreshold) {
        List<Transaction> snapshot = List.copyOf(transactions);

        BigDecimal total = snapshot.stream()
                .map(Transaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<TransactionType, BigDecimal> totalsByType = snapshot.stream()
                .collect(groupingBy(
                        Transaction::type,
                        () -> new EnumMap<>(TransactionType.class),
                        reducing(BigDecimal.ZERO, Transaction::amount, BigDecimal::add)));

        Map<String, List<Transaction>> byCategory = snapshot.stream()
                .collect(groupingBy(Transaction::category));

        Map<Boolean, List<Transaction>> largeAmountPartition = snapshot.stream()
                .collect(partitioningBy(transaction ->
                        transaction.amount().compareTo(largeAmountThreshold) >= 0));

        Map<String, AccountSummary> byAccount = snapshot.stream()
                .collect(groupingBy(Transaction::accountId))
                .entrySet().stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> new AccountSummary(
                                entry.getValue().size(),
                                entry.getValue().stream()
                                        .map(Transaction::amount)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add))));

        return new TransactionReport(total, totalsByType, byCategory, largeAmountPartition, byAccount);
    }

    /** Loop equivalent, kept deliberately to compare readability with createReport. */
    public TransactionReport createReportWithLoops(
            List<Transaction> transactions, BigDecimal largeAmountThreshold) {
        BigDecimal total = BigDecimal.ZERO;
        Map<TransactionType, BigDecimal> totalsByType = new EnumMap<>(TransactionType.class);
        Map<String, List<Transaction>> byCategory = new HashMap<>();
        Map<Boolean, List<Transaction>> partitions = new HashMap<>();
        partitions.put(true, new ArrayList<>());
        partitions.put(false, new ArrayList<>());
        Map<String, List<Transaction>> byAccount = new HashMap<>();

        for (Transaction transaction : transactions) {
            total = total.add(transaction.amount());
            totalsByType.merge(transaction.type(), transaction.amount(), BigDecimal::add);
            byCategory.computeIfAbsent(transaction.category(), ignored -> new ArrayList<>()).add(transaction);
            boolean isLarge = transaction.amount().compareTo(largeAmountThreshold) >= 0;
            partitions.get(isLarge).add(transaction);
            byAccount.computeIfAbsent(transaction.accountId(), ignored -> new ArrayList<>()).add(transaction);
        }

        Map<String, AccountSummary> summaries = new HashMap<>();
        for (Map.Entry<String, List<Transaction>> entry : byAccount.entrySet()) {
            BigDecimal accountTotal = BigDecimal.ZERO;
            for (Transaction transaction : entry.getValue()) {
                accountTotal = accountTotal.add(transaction.amount());
            }
            summaries.put(entry.getKey(), new AccountSummary(entry.getValue().size(), accountTotal));
        }
        return new TransactionReport(total, totalsByType, byCategory, partitions, summaries);
    }

    public Optional<Transaction> findById(List<Transaction> transactions, String id) {
        return transactions.stream()
                .filter(transaction -> transaction.id().equals(id))
                .findFirst();
    }

    public List<String> categoriesForAccounts(List<List<Transaction>> accountTransactions) {
        return accountTransactions.stream()
                .flatMap(List::stream)
                .map(Transaction::category)
                .distinct()
                .sorted()
                .collect(toList());
    }

    public List<Transaction> largestFirst(List<Transaction> transactions, BigDecimal minimumAmount) {
        return transactions.stream()
                .filter(transaction -> transaction.amount().compareTo(minimumAmount) >= 0)
                .sorted(comparing(Transaction::amount).reversed())
                .toList();
    }
}
