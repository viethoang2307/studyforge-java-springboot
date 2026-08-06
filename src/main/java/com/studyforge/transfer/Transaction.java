package com.studyforge.transfer;

import java.math.BigDecimal;
import java.time.Instant;

public record Transaction(
        String id, String sourceAccountId, String destinationAccountId,
        BigDecimal amount, Instant occurredAt) {
}
