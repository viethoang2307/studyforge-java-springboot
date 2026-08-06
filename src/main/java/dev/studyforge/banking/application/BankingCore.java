package dev.studyforge.banking.application;

import dev.studyforge.banking.domain.*;
import java.util.*;

/**
 * In-memory banking aggregate. All validation occurs before a transfer mutates either account,
 * so a rejected transfer is atomic. Persistence/locking are deliberately outside this Java core.
 */
public final class BankingCore {
    private final Map<AccountId, Account> accounts = new HashMap<>();
    private final List<Transaction> history = new ArrayList<>();
    private final FeeStrategy withdrawalFee;
    private final FeeStrategy transferFee;
    private final TransactionFactory transactions;
    private final List<NotificationPort> observers;

    public BankingCore(FeeStrategy withdrawalFee, FeeStrategy transferFee,
                       TransactionFactory transactions, List<NotificationPort> observers) {
        this.withdrawalFee = Objects.requireNonNull(withdrawalFee);
        this.transferFee = Objects.requireNonNull(transferFee);
        this.transactions = Objects.requireNonNull(transactions);
        this.observers = List.copyOf(observers);
    }

    public void open(Account account) {
        Objects.requireNonNull(account);
        if (accounts.putIfAbsent(account.id(), account) != null)
            throw new IllegalArgumentException("Account already exists: " + account.id().value());
    }

    public Transaction deposit(Deposit command) {
        Account account = account(command.accountId());
        account.credit(command.amount());
        return record(transactions.deposit(account.id(), command.amount()));
    }

    public Transaction withdraw(Withdrawal command) {
        Account account = account(command.accountId());
        Money fee = withdrawalFee.calculate(command.amount());
        Money total = command.amount().add(fee);
        account.debit(total);
        return record(transactions.withdrawal(account.id(), command.amount(), fee));
    }

    public Transaction transfer(Transfer command) {
        Account source = account(command.source());
        Account destination = account(command.destination());
        // Calculate and validate everything before the first write: either both balances change or neither does.
        Money fee = transferFee.calculate(command.amount());
        Money totalDebit = command.amount().add(fee);
        source.ensureCanDebit(totalDebit);
        if (!destination.currency().equals(command.amount().currency()))
            throw new CurrencyMismatchException(destination.currency(), command.amount().currency());
        source.debit(totalDebit);
        destination.credit(command.amount());
        return record(transactions.transfer(source.id(), destination.id(), command.amount(), fee));
    }

    public Account account(AccountId id) { return Optional.ofNullable(accounts.get(id)).orElseThrow(() -> new AccountNotFoundException(id)); }

    public List<Transaction> history(AccountId id) {
        account(id);
        return history.stream().filter(t -> t.sourceAccount().filter(id::equals).isPresent()
                || t.destinationAccount().filter(id::equals).isPresent()).toList();
    }

    public List<Transaction> history() { return List.copyOf(history); }

    private Transaction record(Transaction transaction) {
        history.add(transaction);
        // Observer failures must not undo or hide a completed financial operation.
        observers.forEach(observer -> {
            try { observer.transactionCompleted(transaction); }
            catch (RuntimeException ignored) { /* adapters own retry/error reporting policy */ }
        });
        return transaction;
    }
}
