package dev.studyforge.payments;

import java.util.Objects;

public final class WalletPayment implements PaymentMethod {

    private final PaymentGateway walletGateway;

    public WalletPayment(PaymentGateway walletGateway) {
        this.walletGateway = Objects.requireNonNull(walletGateway);
    }

    @Override
    public PaymentType type() {
        return PaymentType.WALLET;
    }

    @Override
    public PaymentReceipt pay(PaymentRequest request) {
        return new PaymentReceipt(walletGateway.charge(request), type());
    }
}
