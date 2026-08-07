package dev.studyforge.payments;

import java.util.Objects;

public final class CardPayment implements PaymentMethod {

    private final PaymentGateway cardGateway;

    public CardPayment(PaymentGateway cardGateway) {
        this.cardGateway = Objects.requireNonNull(cardGateway);
    }

    @Override
    public PaymentType type() {
        return PaymentType.CARD;
    }

    @Override
    public PaymentReceipt pay(PaymentRequest request) {
        return new PaymentReceipt(cardGateway.charge(request), type());
    }
}
