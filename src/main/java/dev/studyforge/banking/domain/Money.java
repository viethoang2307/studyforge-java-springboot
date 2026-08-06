package dev.studyforge.banking.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) implements Comparable<Money> {
    public Money {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");
        if (amount.scale() > currency.getDefaultFractionDigits())
            throw new IllegalArgumentException("Too many fraction digits for " + currency);
        amount = amount.setScale(currency.getDefaultFractionDigits(), RoundingMode.UNNECESSARY);
    }

    public static Money of(String amount, String currency) {
        return new Money(new BigDecimal(amount), Currency.getInstance(currency));
    }

    public static Money zero(Currency currency) { return new Money(BigDecimal.ZERO, currency); }
    public boolean isPositive() { return amount.signum() > 0; }
    public Money add(Money other) { requireSameCurrency(other); return new Money(amount.add(other.amount), currency); }
    public Money subtract(Money other) { requireSameCurrency(other); return new Money(amount.subtract(other.amount), currency); }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "money");
        if (!currency.equals(other.currency)) throw new CurrencyMismatchException(currency, other.currency);
    }

    @Override public int compareTo(Money other) { requireSameCurrency(other); return amount.compareTo(other.amount); }
}
