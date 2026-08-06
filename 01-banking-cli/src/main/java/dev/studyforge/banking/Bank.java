package dev.studyforge.banking;

import dev.studyforge.banking.exception.AccountNotFoundException;
import dev.studyforge.banking.exception.BankingException;
import dev.studyforge.banking.exception.InsufficientBalanceException;
import dev.studyforge.banking.exception.InvalidAmountException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Bank {
    private final Map<String, Account> accounts = new HashMap<>();

    public void add(Account account) {
        Objects.requireNonNull(account, "account must not be null");
        if (accounts.putIfAbsent(account.id(), account) != null) {
            throw new IllegalArgumentException("Duplicate account: " + account.id());
        }
    }

    public Account find(String accountId) throws AccountNotFoundException {
        Account account = accounts.get(accountId);
        if (account == null) {
            throw new AccountNotFoundException(accountId);
        }
        return account;
    }

    /** Validates every failure condition before changing either account. */
    public void transfer(String sourceId, String destinationId, BigDecimal amount)
            throws BankingException {
        validateAmount(amount);
        Account source = find(sourceId);
        Account destination = find(destinationId);
        if (source == destination) {
            throw new InvalidAmountException("Source and destination must be different");
        }
        if (source.balance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(sourceId);
        }

        source.debit(amount);
        destination.credit(amount);
    }

    private static void validateAmount(BigDecimal amount) throws InvalidAmountException {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }
    }
}
