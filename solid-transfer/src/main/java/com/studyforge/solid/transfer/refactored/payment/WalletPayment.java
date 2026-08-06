package com.studyforge.solid.transfer.refactored.payment;

import com.studyforge.solid.transfer.TransferRequest;
import com.studyforge.solid.transfer.refactored.PaymentMethod;

public final class WalletPayment implements PaymentMethod {
    @Override public String id() { return "WALLET"; }
    @Override public void verify(TransferRequest request) { /* Wallet has no method-specific limit. */ }
}
