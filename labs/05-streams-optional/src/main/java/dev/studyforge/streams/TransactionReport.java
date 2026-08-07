package dev.studyforge.streams;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record TransactionReport(
        BigDecimal totalAmount,
        Map<TransactionType, BigDecimal> totalsByType,
        Map<String, List<Transaction>> transactionsByCategory,
        Map<Boolean, List<Transaction>> largeAmountPartition,
        Map<String, AccountSummary> summariesByAccount
) {
}
