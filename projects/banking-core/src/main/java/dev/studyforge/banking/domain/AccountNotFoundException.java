package dev.studyforge.banking.domain; public final class AccountNotFoundException extends BankingException{public AccountNotFoundException(AccountId id){super("Account not found: "+id.value());}}
