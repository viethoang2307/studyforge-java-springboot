package com.studyforge.banking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class BankAccountTest {
    @Test
    void oneArgumentConstructorCreatesAccountWithZeroBalance() {
        BankAccount account = new BankAccount("ACC-001");

        assertEquals("ACC-001", account.getAccountNumber());
        assertEquals(0, BigDecimal.ZERO.compareTo(account.getBalance()));
    }

    @Test
    void constructorRejectsBlankAccountNumberAndInvalidBalance() {
        assertThrows(IllegalArgumentException.class, () -> new BankAccount("  "));
        assertThrows(IllegalArgumentException.class,
                () -> new BankAccount("ACC-001", new BigDecimal("-0.01")));
        assertThrows(NullPointerException.class, () -> new BankAccount("ACC-001", null));
    }

    @Test
    void depositAndWithdrawChangeBalanceThroughBusinessMethods() {
        BankAccount account = new BankAccount("ACC-001", new BigDecimal("100.00"));

        account.deposit(new BigDecimal("25.00"));
        account.withdraw(new BigDecimal("40.00"));

        assertEquals(0, new BigDecimal("85.00").compareTo(account.getBalance()));
    }

    @Test
    void invalidAmountsAreRejectedWithoutChangingState() {
        BankAccount account = new BankAccount("ACC-001", new BigDecimal("100.00"));

        assertThrows(IllegalArgumentException.class, () -> account.deposit(BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> account.withdraw(new BigDecimal("-1")));
        assertThrows(NullPointerException.class, () -> account.deposit(null));
        assertEquals(0, new BigDecimal("100.00").compareTo(account.getBalance()));
    }

    @Test
    void overdraftIsRejectedWithoutChangingState() {
        BankAccount account = new BankAccount("ACC-001", new BigDecimal("50.00"));

        assertThrows(IllegalStateException.class,
                () -> account.withdraw(new BigDecimal("50.01")));

        assertEquals(0, new BigDecimal("50.00").compareTo(account.getBalance()));
    }

    @Test
    void balanceHasNoPublicSetter() {
        boolean hasBalanceSetter = Arrays.stream(BankAccount.class.getMethods())
                .map(Method::getName)
                .anyMatch("setBalance"::equals);

        assertFalse(hasBalanceSetter);
    }
}
