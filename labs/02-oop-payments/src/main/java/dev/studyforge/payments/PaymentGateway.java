package dev.studyforge.payments;

@FunctionalInterface
public interface PaymentGateway {

    String charge(PaymentRequest request);
}
