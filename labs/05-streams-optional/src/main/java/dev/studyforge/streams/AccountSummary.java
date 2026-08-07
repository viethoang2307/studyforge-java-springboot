package dev.studyforge.streams;

import java.math.BigDecimal;

public record AccountSummary(long transactionCount, BigDecimal totalAmount) {
}
