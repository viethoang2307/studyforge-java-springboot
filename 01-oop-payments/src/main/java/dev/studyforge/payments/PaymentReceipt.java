package dev.studyforge.payments;

import java.util.Objects;

public record PaymentReceipt(String transactionId, PaymentType paymentType) {

    public PaymentReceipt {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("transactionId must not be blank");
        }
        Objects.requireNonNull(paymentType, "paymentType must not be null");
    }
}
