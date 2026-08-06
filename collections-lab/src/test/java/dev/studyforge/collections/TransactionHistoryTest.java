package dev.studyforge.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransactionHistoryTest {
    private final UUID firstId = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private final UUID secondId = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private final UUID thirdId = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private Transaction first;
    private Transaction second;
    private Transaction third;
    private TransactionHistory history;

    @BeforeEach
    void setUp() {
        first = transaction(firstId, "checking", TransactionType.DEPOSIT, "100.00", "2026-01-01T10:00:00Z");
        second = transaction(secondId, "savings", TransactionType.TRANSFER, "25.00", "2026-01-01T11:00:00Z");
        third = transaction(thirdId, "checking", TransactionType.WITHDRAWAL, "40.00", "2026-01-01T12:00:00Z");
        history = new TransactionHistory();
        history.add(second);
        history.add(first);
        history.add(third);
    }

    @Test
    void preservesInsertionOrder() {
        assertEquals(List.of(second, first, third), history.insertionOrder());
    }

    @Test
    void filtersAndSortsWithoutChangingStoredOrder() {
        var checkingOnly = new TransactionFilter("checking", null, new BigDecimal("40"), null, null);

        assertEquals(List.of(third, first), history.find(checkingOnly, Transaction.BY_AMOUNT_DESCENDING));
        assertEquals(List.of(second, first, third), history.insertionOrder());
    }

    @Test
    void groupsByEnumAndTotalsAccountsInFirstSeenOrder() {
        assertEquals(List.of(first), history.groupByType().get(TransactionType.DEPOSIT));
        assertEquals(List.of(TransactionType.DEPOSIT, TransactionType.WITHDRAWAL, TransactionType.TRANSFER),
                history.groupByType().keySet().stream().toList());
        assertEquals(Map.of("checking", new BigDecimal("140.00"), "savings", new BigDecimal("25.00")),
                history.totalByAccount());
        assertEquals(List.of("savings", "checking"), history.totalByAccount().keySet().stream().toList());
    }

    @Test
    void rejectsDuplicateIdentity() {
        var sameIdentityWithDifferentData = transaction(
                firstId, "other", TransactionType.TRANSFER, "999", "2027-01-01T00:00:00Z");

        assertThrows(IllegalArgumentException.class, () -> history.add(sameIdentityWithDifferentData));
        assertEquals(3, history.size());
    }

    private static Transaction transaction(
            UUID id, String account, TransactionType type, String amount, String occurredAt) {
        return new Transaction(id, account, type, new BigDecimal(amount), Instant.parse(occurredAt));
    }
}
