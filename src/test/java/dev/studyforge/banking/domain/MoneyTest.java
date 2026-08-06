package dev.studyforge.banking.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {
    @Test void valueArithmeticIsExact() {
        assertEquals(Money.of("0.30", "USD"), Money.of("0.10", "USD").add(Money.of("0.20", "USD")));
    }
    @Test void rejectsUnsupportedPrecision() {
        assertThrows(IllegalArgumentException.class, () -> Money.of("1.001", "USD"));
    }
    @Test void rejectsCrossCurrencyArithmetic() {
        assertThrows(CurrencyMismatchException.class, () -> Money.of("1", "USD").add(Money.of("1", "EUR")));
    }
}
