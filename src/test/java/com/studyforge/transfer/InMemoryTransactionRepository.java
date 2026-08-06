package com.studyforge.transfer;

import java.util.ArrayList;
import java.util.List;

final class InMemoryTransactionRepository implements TransactionRepository {
    private final List<Transaction> transactions = new ArrayList<>();

    @Override
    public void save(Transaction transaction) {
        transactions.add(transaction);
    }

    List<Transaction> findAll() {
        return List.copyOf(transactions);
    }
}
