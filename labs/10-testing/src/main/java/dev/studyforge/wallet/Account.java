package dev.studyforge.wallet;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public final class Account {
    private final String id;
    private final ReentrantLock lock = new ReentrantLock();
    private long balanceInCents;

    public Account(String id, Money openingBalance) {
        this.id = Objects.requireNonNull(id, "id");
        this.balanceInCents = Objects.requireNonNull(openingBalance, "openingBalance").cents();
    }

    public String id() {
        return id;
    }

    public Money balance() {
        lock.lock();
        try {
            return Money.ofCents(balanceInCents);
        } finally {
            lock.unlock();
        }
    }

    void lock() {
        lock.lock();
    }

    void unlock() {
        lock.unlock();
    }

    void withdraw(long cents) {
        if (balanceInCents < cents) {
            throw new InsufficientFundsException(id);
        }
        balanceInCents -= cents;
    }

    void requireDepositDoesNotOverflow(long cents) {
        Math.addExact(balanceInCents, cents);
    }

    void deposit(long cents) {
        balanceInCents += cents;
    }
}
