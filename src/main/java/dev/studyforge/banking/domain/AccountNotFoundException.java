package dev.studyforge.banking.domain;

public final class AccountNotFoundException extends BankingException {
    private static final long serialVersionUID = 1L;
    public AccountNotFoundException(AccountId id) { super("Account not found: " + id.value()); }
}
