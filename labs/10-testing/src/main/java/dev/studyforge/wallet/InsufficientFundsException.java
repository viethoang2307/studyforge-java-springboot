package dev.studyforge.wallet;

public final class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String accountId) {
        super("Insufficient funds in account: " + accountId);
    }
}
