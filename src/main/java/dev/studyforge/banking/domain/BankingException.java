package dev.studyforge.banking.domain;

public class BankingException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public BankingException(String message) { super(message); }
}
