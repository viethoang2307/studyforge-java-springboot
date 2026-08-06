package dev.studyforge.banking;

import dev.studyforge.banking.exception.AccountNotFoundException;
import dev.studyforge.banking.exception.InsufficientBalanceException;
import dev.studyforge.banking.exception.InvalidAmountException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class BankTest {
    private Bank bank;
    private Account alice;
    private Account bob;

    @BeforeEach
    void setUp() {
        bank = new Bank();
        alice = new Account("alice", new BigDecimal("100.00"));
        bob = new Account("bob", new BigDecimal("25.00"));
        bank.add(alice);
        bank.add(bob);
    }

    @Test
    void transferUpdatesBothAccounts() throws Exception {
        bank.transfer("alice", "bob", new BigDecimal("40.00"));

        assertEquals(new BigDecimal("60.00"), alice.balance());
        assertEquals(new BigDecimal("65.00"), bob.balance());
    }

    @Test
    void invalidAmountHasExactTypeAndDoesNotMutateState() {
        assertThrowsExactly(InvalidAmountException.class,
                () -> bank.transfer("alice", "bob", BigDecimal.ZERO));
        assertBalancesUnchanged();
    }

    @Test
    void insufficientBalanceHasExactTypeAndDoesNotMutateState() {
        assertThrowsExactly(InsufficientBalanceException.class,
                () -> bank.transfer("alice", "bob", new BigDecimal("100.01")));
        assertBalancesUnchanged();
    }

    @Test
    void missingDestinationHasExactTypeAndDoesNotMutateSource() {
        assertThrowsExactly(AccountNotFoundException.class,
                () -> bank.transfer("alice", "missing", BigDecimal.TEN));
        assertBalancesUnchanged();
    }

    private void assertBalancesUnchanged() {
        assertEquals(new BigDecimal("100.00"), alice.balance());
        assertEquals(new BigDecimal("25.00"), bob.balance());
    }
}
