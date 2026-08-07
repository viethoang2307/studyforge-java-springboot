package dev.studyforge.transfer;

public final class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String accountId) {
        super("Account not found: " + accountId);
    }
}

