package dev.studyforge.banking.exception;

public final class InvalidAmountException extends BankingException {
    public InvalidAmountException(String message) {
        super(message);
    }
}
