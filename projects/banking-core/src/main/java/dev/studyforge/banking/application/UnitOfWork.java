package dev.studyforge.banking.application; public interface UnitOfWork{<T>T execute(Work<T> work); interface Work<T>{T apply(AccountRepository accounts,TransactionRepository transactions);}}
