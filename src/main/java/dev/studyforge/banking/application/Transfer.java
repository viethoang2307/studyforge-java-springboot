package dev.studyforge.banking.application;

import dev.studyforge.banking.domain.AccountId;
import dev.studyforge.banking.domain.Money;
import java.util.Objects;

public record Transfer(AccountId source, AccountId destination, Money amount) {
    public Transfer {
        Objects.requireNonNull(source); Objects.requireNonNull(destination);
        if (source.equals(destination)) throw new IllegalArgumentException("Source and destination must differ");
        if (amount == null || !amount.isPositive()) throw new IllegalArgumentException("Transfer amount must be positive");
    }
}
