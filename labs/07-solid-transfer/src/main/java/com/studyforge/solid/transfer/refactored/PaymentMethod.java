package dev.studyforge.solid.transfer.refactored;

import dev.studyforge.solid.transfer.TransferRequest;

/** Extension point for rules that vary by payment method. */
public interface PaymentMethod {
    String id();
    void verify(TransferRequest request);
}

