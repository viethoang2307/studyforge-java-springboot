package dev.studyforge.banking.application;

import dev.studyforge.banking.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BankingCoreTest {
    private static final Currency USD = Currency.getInstance("USD");
    private static final AccountId ALICE = new AccountId("alice");
    private static final AccountId BOB = new AccountId("bob");
    private static final Instant NOW = Instant.parse("2026-01-02T03:04:05Z");
    private BankingCore bank;
    private Account alice;
    private Account bob;
    private List<Transaction> notifications;

    @BeforeEach void setUp() {
        alice = account(ALICE, "100.00"); bob = account(BOB, "20.00");
        notifications = new ArrayList<>();
        bank = new BankingCore(FeeStrategy.noFee(), new PercentageFeeStrategy(new BigDecimal("0.01")),
                new TransactionFactory(Clock.fixed(NOW, ZoneOffset.UTC), () -> UUID.fromString("00000000-0000-0000-0000-000000000001")),
                List.of(notifications::add));
        bank.open(alice); bank.open(bob);
    }

    @Test void depositCreditsAccountAndRecordsHistory() {
        Transaction transaction = bank.deposit(new Deposit(ALICE, usd("12.50")));
        assertEquals(usd("112.50"), alice.balance());
        assertAll(() -> assertEquals(TransactionType.DEPOSIT, transaction.type()),
                () -> assertEquals(NOW, transaction.occurredAt()),
                () -> assertEquals(List.of(transaction), bank.history(ALICE)),
                () -> assertEquals(List.of(transaction), notifications));
    }

    @Test void withdrawalDebitsAmountAndConfiguredFee() {
        BankingCore chargedBank = new BankingCore(new PercentageFeeStrategy(new BigDecimal("0.10")), FeeStrategy.noFee(),
                factory(), List.of());
        chargedBank.open(alice);
        Transaction transaction = chargedBank.withdraw(new Withdrawal(ALICE, usd("10.00")));
        assertEquals(usd("89.00"), alice.balance());
        assertEquals(usd("1.00"), transaction.fee());
    }

    @Test void transferMovesAmountAndChargesFeeOnlyToSource() {
        Transaction transaction = bank.transfer(new Transfer(ALICE, BOB, usd("50.00")));
        assertAll(() -> assertEquals(usd("49.50"), alice.balance()),
                () -> assertEquals(usd("70.00"), bob.balance()),
                () -> assertEquals(usd("0.50"), transaction.fee()),
                () -> assertEquals(List.of(transaction), bank.history(BOB)));
    }

    @Test void failedTransferDoesNotMutateEitherAccountOrHistory() {
        assertThrows(InsufficientFundsException.class, () -> bank.transfer(new Transfer(ALICE, BOB, usd("100.00"))));
        assertAll(() -> assertEquals(usd("100.00"), alice.balance()),
                () -> assertEquals(usd("20.00"), bob.balance()),
                () -> assertTrue(bank.history().isEmpty()),
                () -> assertTrue(notifications.isEmpty()));
    }

    @Test void rejectsMissingAccountWithoutMutation() {
        assertThrows(AccountNotFoundException.class,
                () -> bank.transfer(new Transfer(ALICE, new AccountId("missing"), usd("10.00"))));
        assertEquals(usd("100.00"), alice.balance());
    }

    @Test void rejectsNonPositiveCommandsAndSelfTransfer() {
        assertAll(() -> assertThrows(IllegalArgumentException.class, () -> new Deposit(ALICE, usd("0"))),
                () -> assertThrows(IllegalArgumentException.class, () -> new Withdrawal(ALICE, usd("-1"))),
                () -> assertThrows(IllegalArgumentException.class, () -> new Transfer(ALICE, ALICE, usd("1"))));
    }

    @Test void rejectsCurrencyMismatchBeforeChangingBalances() {
        Money euros = Money.of("2.00", "EUR");
        assertThrows(CurrencyMismatchException.class, () -> bank.transfer(new Transfer(ALICE, BOB, euros)));
        assertEquals(usd("100.00"), alice.balance());
        assertEquals(usd("20.00"), bob.balance());
    }

    @Test void historySnapshotsCannotBeModified() {
        bank.deposit(new Deposit(ALICE, usd("1")));
        assertThrows(UnsupportedOperationException.class, () -> bank.history().clear());
    }

    @Test void notificationFailureDoesNotChangeCompletedTransaction() {
        BankingCore bankWithBrokenAdapter = new BankingCore(FeeStrategy.noFee(), FeeStrategy.noFee(), factory(),
                List.of(transaction -> { throw new IllegalStateException("mail unavailable"); }));
        bankWithBrokenAdapter.open(alice);
        assertDoesNotThrow(() -> bankWithBrokenAdapter.deposit(new Deposit(ALICE, usd("1"))));
        assertEquals(usd("101.00"), alice.balance());
        assertEquals(1, bankWithBrokenAdapter.history().size());
    }

    @Test void accountBuilderRejectsInvalidOpeningBalance() {
        assertThrows(IllegalArgumentException.class,
                () -> Account.builder(new AccountId("bad"), "Bad", USD).openingBalance(usd("-0.01")).build());
    }

    private static Account account(AccountId id, String balance) {
        return Account.builder(id, id.value(), USD).openingBalance(usd(balance)).build();
    }
    private static Money usd(String value) { return Money.of(value, "USD"); }
    private static TransactionFactory factory() {
        return new TransactionFactory(Clock.fixed(NOW, ZoneOffset.UTC), UUID::randomUUID);
    }
}
