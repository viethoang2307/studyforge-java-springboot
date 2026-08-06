package dev.studyforge.banking;

import java.math.BigDecimal;
import java.util.Objects;

public final class Account {
    private final String id;
    private BigDecimal balance;

    public Account(String id, BigDecimal openingBalance) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.balance = Objects.requireNonNull(openingBalance, "openingBalance must not be null");
        if (id.isBlank() || openingBalance.signum() < 0) {
            throw new IllegalArgumentException("id must not be blank and opening balance must not be negative");
        }
    }

    public String id() {
        return id;
    }

    public BigDecimal balance() {
        return balance;
    }

    void debit(BigDecimal amount) {
        balance = balance.subtract(amount);
    }

    void credit(BigDecimal amount) {
        balance = balance.add(amount);
    }
}
