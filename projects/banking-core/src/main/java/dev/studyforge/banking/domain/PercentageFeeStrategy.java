package dev.studyforge.banking.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PercentageFeeStrategy implements FeeStrategy {
    private final BigDecimal rate;
    public PercentageFeeStrategy(BigDecimal rate) {
        if (rate == null || rate.signum() < 0) throw new IllegalArgumentException("Rate must not be negative");
        this.rate = rate;
    }
    @Override public Money calculate(Money amount) {
        return new Money(amount.amount().multiply(rate).setScale(amount.currency().getDefaultFractionDigits(), RoundingMode.HALF_UP), amount.currency());
    }
}
