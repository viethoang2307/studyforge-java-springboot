package dev.studyforge.payments;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/** A valid, immutable description of money to collect from a payment source. */
public record PaymentRequest(String sourceReference, BigDecimal amount, Currency currency) {

    public PaymentRequest {
        if (sourceReference == null || sourceReference.isBlank()) {
            throw new IllegalArgumentException("sourceReference must not be blank");
        }
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currency, "currency must not be null");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be greater than zero");
        }

        sourceReference = sourceReference.trim();
        amount = amount.stripTrailingZeros();
    }
}
