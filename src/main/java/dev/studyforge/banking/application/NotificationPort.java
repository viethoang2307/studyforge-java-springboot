package dev.studyforge.banking.application;

import dev.studyforge.banking.domain.Transaction;

/** Outbound port. Implementations may send email, publish an event, or do nothing. */
@FunctionalInterface
public interface NotificationPort {
    void transactionCompleted(Transaction transaction);
}
