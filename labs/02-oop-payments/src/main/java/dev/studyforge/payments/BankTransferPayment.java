package dev.studyforge.payments;

import java.util.Objects;

public final class BankTransferPayment implements PaymentMethod {

    private final PaymentGateway bankGateway;

    public BankTransferPayment(PaymentGateway bankGateway) {
        this.bankGateway = Objects.requireNonNull(bankGateway);
    }

    @Override
    public PaymentType type() {
        return PaymentType.BANK_TRANSFER;
    }

    @Override
    public PaymentReceipt pay(PaymentRequest request) {
        return new PaymentReceipt(bankGateway.charge(request), type());
    }
}
