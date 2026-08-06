package dev.studyforge.wallet;

import java.util.Objects;

public final class TransferService {
    private final AccountRepository accounts;
    private final TransferNotifier notifier;

    public TransferService(AccountRepository accounts, TransferNotifier notifier) {
        this.accounts = Objects.requireNonNull(accounts, "accounts");
        this.notifier = Objects.requireNonNull(notifier, "notifier");
    }

    public TransferReceipt transfer(String sourceId, String destinationId, Money amount) {
        Objects.requireNonNull(sourceId, "sourceId");
        Objects.requireNonNull(destinationId, "destinationId");
        Objects.requireNonNull(amount, "amount");
        if (sourceId.equals(destinationId)) {
            throw new IllegalArgumentException("Source and destination must differ");
        }
        if (amount.cents() == 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        Account source = find(sourceId);
        Account destination = find(destinationId);
        Account first = source.id().compareTo(destination.id()) < 0 ? source : destination;
        Account second = first == source ? destination : source;

        first.lock();
        second.lock();
        try {
            destination.requireDepositDoesNotOverflow(amount.cents());
            source.withdraw(amount.cents());
            destination.deposit(amount.cents());
        } finally {
            second.unlock();
            first.unlock();
        }

        TransferReceipt receipt = new TransferReceipt(sourceId, destinationId, amount);
        notifier.transferCompleted(receipt);
        return receipt;
    }

    private Account find(String id) {
        return accounts.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }
}
