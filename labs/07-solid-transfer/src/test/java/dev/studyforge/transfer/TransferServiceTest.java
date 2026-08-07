package dev.studyforge.transfer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TransferServiceTest {
    private static final Instant NOW = Instant.parse("2026-08-06T10:00:00Z");
    private InMemoryAccountRepository accounts;
    private InMemoryTransactionRepository transactions;
    private RecordingNotification notifications;
    private TransferService service;

    @BeforeEach
    void setUp() {
        accounts = new InMemoryAccountRepository(
                new Account("source", new BigDecimal("100.00")),
                new Account("destination", new BigDecimal("20.00")));
        transactions = new InMemoryTransactionRepository();
        notifications = new RecordingNotification();
        service = new TransferService(accounts, transactions, notifications, () -> NOW);
    }

    @Test
    void transfersMoneyAndRecordsTheBusinessEvent() {
        Transaction result = service.transfer("source", "destination", new BigDecimal("35.00"));

        assertAll(
                () -> assertEquals(new BigDecimal("65.00"), accounts.findById("source").orElseThrow().balance()),
                () -> assertEquals(new BigDecimal("55.00"), accounts.findById("destination").orElseThrow().balance()),
                () -> assertEquals(NOW, result.occurredAt()),
                () -> assertEquals(result, transactions.findAll().get(0)),
                () -> assertEquals(result, notifications.sent().get(0)));
    }

    @Test
    void rejectsInsufficientFundsWithoutSideEffects() {
        assertThrows(InsufficientFundsException.class,
                () -> service.transfer("source", "destination", new BigDecimal("101.00")));

        assertAll(
                () -> assertEquals(new BigDecimal("100.00"), accounts.findById("source").orElseThrow().balance()),
                () -> assertEquals(new BigDecimal("20.00"), accounts.findById("destination").orElseThrow().balance()),
                () -> assertTrue(transactions.findAll().isEmpty()),
                () -> assertTrue(notifications.sent().isEmpty()));
    }

    @Test
    void rejectsMissingAccountAndInvalidAmount() {
        assertAll(
                () -> assertThrows(AccountNotFoundException.class,
                        () -> service.transfer("missing", "destination", BigDecimal.ONE)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.transfer("source", "destination", BigDecimal.ZERO)),
                () -> assertThrows(IllegalArgumentException.class,
                        () -> service.transfer("source", "source", BigDecimal.ONE)));
    }
}

