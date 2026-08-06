package dev.studyforge.payments;

public final class UnsupportedPaymentTypeException extends RuntimeException {

    public UnsupportedPaymentTypeException(PaymentType type) {
        super("No payment method registered for " + type);
    }
}
