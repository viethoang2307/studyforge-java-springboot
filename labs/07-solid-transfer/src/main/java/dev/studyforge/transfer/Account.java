package dev.studyforge.transfer;

import java.math.BigDecimal;
import java.util.Objects;

public record Account(String id, BigDecimal balance) {
    public Account {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(balance, "balance");
        if (balance.signum() < 0) {
            throw new IllegalArgumentException("Balance cannot be negative");
        }
    }

    public Account debit(BigDecimal amount) {
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(id);
        }
        return new Account(id, balance.subtract(amount));
    }

    public Account credit(BigDecimal amount) {
        return new Account(id, balance.add(amount));
    }
}

