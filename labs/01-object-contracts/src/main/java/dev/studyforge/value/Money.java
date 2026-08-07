package dev.studyforge.value;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/** An immutable monetary value. Amounts are canonicalized so 10.0 equals 10.00. */
public final class Money {
    private final BigDecimal amount;
    private final Currency currency;

    private Money(BigDecimal amount, Currency currency) {
        Objects.requireNonNull(amount, "amount must not be null");
        this.currency = Objects.requireNonNull(currency, "currency must not be null");
        this.amount = canonicalize(amount);
    }

    public static Money of(String amount, String currencyCode) {
        Objects.requireNonNull(amount, "amount must not be null");
        Objects.requireNonNull(currencyCode, "currencyCode must not be null");
        return new Money(new BigDecimal(amount), Currency.getInstance(currencyCode));
    }

    public BigDecimal amount() {
        return amount;
    }

    public Currency currency() {
        return currency;
    }

    public Money add(Money other) {
        Objects.requireNonNull(other, "other must not be null");
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add different currencies");
        }
        return new Money(amount.add(other.amount), currency);
    }

    private static BigDecimal canonicalize(BigDecimal amount) {
        if (amount.signum() == 0) {
            return BigDecimal.ZERO;
        }
        return amount.stripTrailingZeros();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Money money)) return false;
        return amount.equals(money.amount) && currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return amount.toPlainString() + " " + currency.getCurrencyCode();
    }
}
