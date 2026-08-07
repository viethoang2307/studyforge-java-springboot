package dev.studyforge.solid.transfer.refactored;

import java.math.BigDecimal;

/** Persistence boundary; implementations must move both balances atomically or change neither. */
public interface AccountRepository {
    boolean exists(String accountId);
    BigDecimal balanceOf(String accountId);
    void transfer(String sourceAccountId, String targetAccountId, BigDecimal amount);
}

