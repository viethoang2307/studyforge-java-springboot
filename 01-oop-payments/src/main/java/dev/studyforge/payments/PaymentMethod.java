package dev.studyforge.payments;

/**
 * The stable abstraction needed by the business service. Implementations hide
 * provider-specific communication and are selected through their type.
 */
public interface PaymentMethod {

    PaymentType type();

    PaymentReceipt pay(PaymentRequest request);
}
