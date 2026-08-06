package dev.studyforge.banking.exception;

public final class InsufficientBalanceException extends BankingException {
    public InsufficientBalanceException(String accountId) {
        super("Insufficient balance in account: " + accountId);
    }
}
