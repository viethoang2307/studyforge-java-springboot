package dev.studyforge.banking.domain;

import java.util.Currency;

public final class CurrencyMismatchException extends BankingException {
    private static final long serialVersionUID = 1L;
    public CurrencyMismatchException(Currency expected, Currency actual) {
        super("Currency mismatch: expected " + expected + " but was " + actual);
    }
}
