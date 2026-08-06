package dev.studyforge.wallet;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MoneyTest {
    @ParameterizedTest(name = "{0} cents is a valid non-negative value")
    @ValueSource(longs = {0, 1, Long.MAX_VALUE})
    void acceptsBoundaryValues(long cents) {
        assertEquals(cents, Money.ofCents(cents).cents());
    }

    @Test
    @DisplayName("negative money violates the value-object invariant")
    void rejectsNegativeValue() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> Money.ofCents(-1));

        assertAll(
                () -> assertEquals("Money cannot be negative", error.getMessage()),
                () -> assertEquals(Money.ofCents(100), Money.ofCents(100)));
    }
}
