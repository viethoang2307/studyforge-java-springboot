package dev.studyforge.solid.transfer.refactored;

import dev.studyforge.solid.transfer.TransferException;
import dev.studyforge.solid.transfer.TransferRequest;

public final class TransferValidator {
    public void validate(TransferRequest request) {
        if (request == null || request.amount() == null || request.amount().signum() <= 0) {
            throw new TransferException("amount must be positive");
        }
        if (request.sourceAccountId() == null || request.targetAccountId() == null
                || request.sourceAccountId().equals(request.targetAccountId())) {
            throw new TransferException("accounts must be different");
        }
        if (request.paymentMethod() == null || request.paymentMethod().isBlank()) {
            throw new TransferException("payment method is required");
        }
    }
}

