package dev.studyforge.streams;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionServiceTest {
    private final TransactionService service = new TransactionService();
    private final List<Transaction> transactions = List.of(
            transaction("tx-1", "acc-1", TransactionType.DEPOSIT, "120.00", "salary"),
            transaction("tx-2", "acc-1", TransactionType.WITHDRAWAL, "20.00", "food"),
            transaction("tx-3", "acc-2", TransactionType.TRANSFER, "60.00", "family"));

    @Test
    void streamReportAnswersAllBusinessQuestions() {
        TransactionReport report = service.createReport(transactions, new BigDecimal("60.00"));

        assertEquals(new BigDecimal("200.00"), report.totalAmount());
        assertEquals(new BigDecimal("120.00"), report.totalsByType().get(TransactionType.DEPOSIT));
        assertEquals(1, report.transactionsByCategory().get("food").size());
        assertEquals(2, report.largeAmountPartition().get(true).size());
        assertEquals(new AccountSummary(2, new BigDecimal("140.00")),
                report.summariesByAccount().get("acc-1"));
    }

    @Test
    void loopAndStreamReportsHaveTheSameResult() {
        BigDecimal threshold = new BigDecimal("60.00");
        assertEquals(service.createReportWithLoops(transactions, threshold),
                service.createReport(transactions, threshold));
    }

    @Test
    void optionalMakesMissingResultExplicit() {
        assertTrue(service.findById(transactions, "tx-1").isPresent());
        assertFalse(service.findById(transactions, "unknown").isPresent());
    }

    @Test
    void intermediateOperationsAreLazyUntilTerminalOperationRuns() {
        AtomicInteger mappedItems = new AtomicInteger();
        Stream<Integer> pipeline = Stream.of(1, 2, 3)
                .map(number -> {
                    mappedItems.incrementAndGet();
                    return number * 2;
                });

        assertEquals(0, mappedItems.get());
        assertEquals(List.of(2, 4, 6), pipeline.toList());
        assertEquals(3, mappedItems.get());
    }

    @Test
    void flatMapCombinesNestedTransactionLists() {
        List<String> categories = service.categoriesForAccounts(
                List.of(transactions.subList(0, 2), transactions.subList(2, 3)));
        assertEquals(List.of("family", "food", "salary"), categories);
    }

    private static Transaction transaction(
            String id, String accountId, TransactionType type, String amount, String category) {
        return new Transaction(id, accountId, type, new BigDecimal(amount),
                LocalDate.of(2026, 8, 6), category);
    }
}
