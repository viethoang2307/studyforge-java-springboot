package dev.studyforge.banking.domain;

@FunctionalInterface
public interface FeeStrategy {
    Money calculate(Money amount);
    static FeeStrategy noFee() { return amount -> Money.zero(amount.currency()); }
}
