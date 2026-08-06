package dev.studyforge.banking.domain;

import java.util.Currency;
import java.util.Objects;

public final class Account {
    private final AccountId id;
    private final String holderName;
    private Money balance;

    private Account(Builder builder) {
        id = Objects.requireNonNull(builder.id, "id");
        holderName = Objects.requireNonNull(builder.holderName, "holder name");
        if (holderName.isBlank()) throw new IllegalArgumentException("Holder name must not be blank");
        balance = builder.openingBalance == null ? Money.zero(builder.currency) : builder.openingBalance;
        if (balance.amount().signum() < 0) throw new IllegalArgumentException("Opening balance must not be negative");
        if (!balance.currency().equals(builder.currency)) throw new CurrencyMismatchException(builder.currency, balance.currency());
    }

    public static Builder builder(AccountId id, String holderName, Currency currency) {
        return new Builder(id, holderName, currency);
    }
    public AccountId id() { return id; }
    public String holderName() { return holderName; }
    public Money balance() { return balance; }
    public Currency currency() { return balance.currency(); }

    public void credit(Money amount) { requirePositive(amount); balance = balance.add(amount); }
    public void debit(Money amount) {
        requirePositive(amount);
        if (balance.compareTo(amount) < 0) throw new InsufficientFundsException(id);
        balance = balance.subtract(amount);
    }
    public void ensureCanDebit(Money amount) {
        requirePositive(amount);
        if (balance.compareTo(amount) < 0) throw new InsufficientFundsException(id);
    }
    private void requirePositive(Money amount) {
        Objects.requireNonNull(amount, "amount");
        if (!amount.isPositive()) throw new IllegalArgumentException("Amount must be positive");
        if (!currency().equals(amount.currency())) throw new CurrencyMismatchException(currency(), amount.currency());
    }

    public static final class Builder {
        private final AccountId id; private final String holderName; private final Currency currency;
        private Money openingBalance;
        private Builder(AccountId id, String holderName, Currency currency) {
            this.id = id; this.holderName = holderName; this.currency = Objects.requireNonNull(currency, "currency");
        }
        public Builder openingBalance(Money balance) { this.openingBalance = balance; return this; }
        public Account build() { return new Account(this); }
    }
}
