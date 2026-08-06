package dev.studyforge.banking.domain;

public final class InsufficientFundsException extends BankingException {
    private static final long serialVersionUID = 1L;
    public InsufficientFundsException(AccountId id) { super("Insufficient funds in account " + id.value()); }
}
