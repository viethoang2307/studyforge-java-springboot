package dev.studyforge.banking.application;

import dev.studyforge.banking.domain.AccountId;
import dev.studyforge.banking.domain.Money;
import java.util.Objects;

public record Withdrawal(AccountId accountId, Money amount) {
    public Withdrawal {
        Objects.requireNonNull(accountId);
        if (amount == null || !amount.isPositive()) throw new IllegalArgumentException("Withdrawal amount must be positive");
    }
}
