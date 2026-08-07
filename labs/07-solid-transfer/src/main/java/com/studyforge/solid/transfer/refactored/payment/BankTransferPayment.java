package dev.studyforge.solid.transfer.refactored.payment;

import dev.studyforge.solid.transfer.TransferException;
import dev.studyforge.solid.transfer.TransferRequest;
import dev.studyforge.solid.transfer.refactored.PaymentMethod;

import java.math.BigDecimal;

public final class BankTransferPayment implements PaymentMethod {
    private static final BigDecimal LIMIT = new BigDecimal("10000.00");
    @Override public String id() { return "BANK_TRANSFER"; }
    @Override public void verify(TransferRequest request) {
        if (request.amount().compareTo(LIMIT) > 0) {
            throw new TransferException("bank transfer limit exceeded");
        }
    }
}

