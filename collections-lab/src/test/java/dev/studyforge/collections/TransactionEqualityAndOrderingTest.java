package dev.studyforge.collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TransactionEqualityAndOrderingTest {
    private static final Instant TIME = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void equalityAndHashCodeUseBusinessIdentity() {
        UUID id = UUID.randomUUID();
        Transaction original = transaction(id, "10.0", TIME);
        Transaction updatedSnapshot = transaction(id, "99.00", TIME.plusSeconds(1));

        assertEquals(original, updatedSnapshot);
        assertEquals(original.hashCode(), updatedSnapshot.hashCode());
        assertEquals(1, new HashSet<>(List.of(original, updatedSnapshot)).size());
        assertNotEquals(original, transaction(UUID.randomUUID(), "10.0", TIME));
    }

    @Test
    void naturalOrderingDoesNotDropDifferentTransactionIdentities() {
        Transaction first = transaction(UUID.fromString("00000000-0000-0000-0000-000000000001"), "10", TIME);
        Transaction second = transaction(UUID.fromString("00000000-0000-0000-0000-000000000002"), "10", TIME);

        assertEquals(List.of(first, second), new TreeSet<>(List.of(second, first)).stream().toList());
    }

    @Test
    void amountComparisonIgnoresBigDecimalScale() {
        assertEquals(true, transaction(UUID.randomUUID(), "10.00", TIME).hasAmount(new BigDecimal("10.0")));
    }

    private static Transaction transaction(UUID id, String amount, Instant time) {
        return new Transaction(id, "account", TransactionType.DEPOSIT, new BigDecimal(amount), time);
    }
}
