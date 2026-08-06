package dev.studyforge.payments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;

class PaymentRequestTest {

    @Test
    void protectsPositiveAmountInvariant() {
        assertThrows(IllegalArgumentException.class,
                () -> request(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class,
                () -> request(new BigDecimal("-0.01")));
    }

    @Test
    void normalizesSourceAndAmountAtConstructionBoundary() {
        PaymentRequest request = new PaymentRequest(
                "  wallet-42  ", new BigDecimal("10.00"), Currency.getInstance("USD"));

        assertEquals("wallet-42", request.sourceReference());
        assertEquals(new BigDecimal("1E+1"), request.amount());
    }

    private PaymentRequest request(BigDecimal amount) {
        return new PaymentRequest("source", amount, Currency.getInstance("USD"));
    }
}
