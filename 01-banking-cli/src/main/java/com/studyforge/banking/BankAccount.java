package com.studyforge.banking;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * An account whose state can only be changed through its business operations.
 * The final class prevents subclasses from weakening its invariants.
 */
public final class BankAccount {
    public static final String BANK_NAME = "StudyForge Bank";

    private final String accountNumber;
    private BigDecimal balance;

    /** Creates an empty account by chaining to the complete constructor. */
    public BankAccount(String accountNumber) {
        this(accountNumber, BigDecimal.ZERO);
    }

    public BankAccount(String accountNumber, BigDecimal openingBalance) {
        if (accountNumber == null || accountNumber.isBlank()) {
            throw new IllegalArgumentException("Account number must not be blank");
        }
        requireNotNegative(openingBalance, "Opening balance");
        this.accountNumber = accountNumber;
        this.balance = openingBalance;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void deposit(BigDecimal amount) {
        requirePositive(amount);
        balance = balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        requirePositive(amount);
        if (balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }
        balance = balance.subtract(amount);
    }

    private static void requirePositive(BigDecimal amount) {
        Objects.requireNonNull(amount, "Amount must not be null");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    private static void requireNotNegative(BigDecimal amount, String fieldName) {
        Objects.requireNonNull(amount, fieldName + " must not be null");
        if (amount.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
    }
}
