package dev.studyforge.value;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ValueObjectTest {
    @Test
    void moneyFulfilsEqualsContractAndHashCodeContract() {
        Money first = Money.of("10.0", "USD");
        Money second = Money.of("10.00", "USD");
        Money third = Money.of("10", "USD");

        assertEquals(first, first, "reflexive");
        assertEquals(first, second, "symmetric (forward)");
        assertEquals(second, first, "symmetric (backward)");
        assertEquals(second, third);
        assertEquals(first, third, "transitive");
        assertEquals(first, second, "consistent across repeated calls");
        assertNotEquals(first, null, "must be unequal to null");
        assertNotEquals(first, Money.of("10", "EUR"));
        assertEquals(first.hashCode(), second.hashCode(),
                "logically equal objects must have equal hash codes");
        assertNotSame(first, second, "logical equality does not imply object identity");
    }

    @Test
    void moneyOperationsReturnNewValuesWithoutChangingOperands() {
        Money original = Money.of("10", "USD");

        Money result = original.add(Money.of("2.50", "USD"));

        assertEquals(Money.of("10", "USD"), original);
        assertEquals(Money.of("12.5", "USD"), result);
        assertThrows(IllegalArgumentException.class,
                () -> original.add(Money.of("1", "EUR")));
    }

    @Test
    void accountIdUsesItsUuidAsLogicalIdentity() {
        UUID uuid = UUID.fromString("8cfb9299-d62d-4a79-9753-80ac64b14f51");

        assertEquals(new AccountId(uuid), AccountId.from(uuid.toString()));
        assertEquals(new AccountId(uuid).hashCode(), AccountId.from(uuid.toString()).hashCode());
        assertThrows(NullPointerException.class, () -> new AccountId(null));
    }

    @Test
    void emailNormalizesBeforeEqualityComparison() {
        Email first = Email.of("  Learner@Example.COM ");
        Email second = Email.of("learner@example.com");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertEquals("learner@example.com", first.value());
        assertThrows(IllegalArgumentException.class, () -> Email.of("not-an-email"));
    }

    @Test
    void accountSnapshotDefensivelyCopiesInputAndDoesNotExposeMutableCollection() {
        List<Money> source = new ArrayList<>();
        source.add(Money.of("1", "USD"));
        AccountSnapshot snapshot = new AccountSnapshot(AccountId.random(), source);

        source.add(Money.of("2", "USD"));

        assertEquals(List.of(Money.of("1", "USD")), snapshot.transactions());
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.transactions().add(Money.of("3", "USD")));
    }

    @Test
    void returnedBigDecimalCannotMutateMoney() {
        Money money = Money.of("10", "USD");
        BigDecimal returnedAmount = money.amount();

        returnedAmount = returnedAmount.add(BigDecimal.ONE);

        assertEquals(Money.of("10", "USD"), money);
        assertEquals(new BigDecimal("11"), returnedAmount);
    }
}
