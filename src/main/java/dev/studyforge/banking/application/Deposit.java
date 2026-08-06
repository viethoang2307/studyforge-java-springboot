package dev.studyforge.banking.application;

import dev.studyforge.banking.domain.AccountId;
import dev.studyforge.banking.domain.Money;
import java.util.Objects;

public record Deposit(AccountId accountId, Money amount) {
    public Deposit { Objects.requireNonNull(accountId); requirePositive(amount); }
    private static void requirePositive(Money value) {
        if (value == null || !value.isPositive()) throw new IllegalArgumentException("Deposit amount must be positive");
    }
}
