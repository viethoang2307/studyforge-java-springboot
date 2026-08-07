package dev.studyforge.solid.transfer;

import java.math.BigDecimal;

public record TransferRequest(String sourceAccountId, String targetAccountId,
                              BigDecimal amount, String paymentMethod) {
}

