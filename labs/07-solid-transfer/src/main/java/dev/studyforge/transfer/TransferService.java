package dev.studyforge.transfer;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public final class TransferService {
    private final AccountRepository accounts;
    private final TransactionRepository transactions;
    private final NotificationPort notifications;
    private final Clock clock;

    public TransferService(AccountRepository accounts, TransactionRepository transactions,
                           NotificationPort notifications, Clock clock) {
        this.accounts = Objects.requireNonNull(accounts);
        this.transactions = Objects.requireNonNull(transactions);
        this.notifications = Objects.requireNonNull(notifications);
        this.clock = Objects.requireNonNull(clock);
    }

    public Transaction transfer(String sourceId, String destinationId, BigDecimal amount) {
        Objects.requireNonNull(amount, "amount");
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        if (Objects.equals(sourceId, destinationId)) {
            throw new IllegalArgumentException("Source and destination must differ");
        }

        Account source = findAccount(sourceId);
        Account destination = findAccount(destinationId);
        Account debited = source.debit(amount);
        Account credited = destination.credit(amount);

        accounts.saveTransfer(debited, credited);
        Transaction transaction = new Transaction(UUID.randomUUID().toString(), sourceId,
                destinationId, amount, clock.now());
        transactions.save(transaction);
        notifications.transferCompleted(transaction);
        return transaction;
    }

    private Account findAccount(String id) {
        return accounts.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }
}

