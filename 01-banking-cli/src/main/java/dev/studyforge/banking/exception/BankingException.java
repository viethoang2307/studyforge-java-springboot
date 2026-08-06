package dev.studyforge.banking.exception;

/** Base type for expected, recoverable banking errors. */
public abstract class BankingException extends Exception {
    protected BankingException(String message) {
        super(message);
    }
}
