package com.studyforge.solid.transfer.bad;

import com.studyforge.solid.transfer.TransferException;
import com.studyforge.solid.transfer.TransferRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/** Locks the legacy behavior before refactoring; it does not claim the design is good. */
class BadTransferServiceCharacterizationTest {
    private BadTransferService service;

    @BeforeEach void setUp() {
        service = new BadTransferService();
        service.addAccount("A", new BigDecimal("100.00"));
        service.addAccount("B", new BigDecimal("20.00"));
    }

    @Test void walletTransferMovesMoneyAndSendsNotification() {
        var receipt = service.transfer(new TransferRequest("A", "B", new BigDecimal("30.00"), "WALLET"));
        assertAll(
                () -> assertEquals(new BigDecimal("70.00"), service.balanceOf("A")),
                () -> assertEquals(new BigDecimal("50.00"), service.balanceOf("B")),
                () -> assertEquals("WALLET", receipt.paymentMethod()),
                () -> assertEquals("Transferred 30.00 to B", service.notifications().get(0))
        );
    }

    @Test void failureDoesNotChangeBalancesOrNotify() {
        var error = assertThrows(TransferException.class, () -> service.transfer(
                new TransferRequest("A", "B", new BigDecimal("101.00"), "WALLET")));
        assertAll(
                () -> assertEquals("insufficient funds", error.getMessage()),
                () -> assertEquals(new BigDecimal("100.00"), service.balanceOf("A")),
                () -> assertEquals(new BigDecimal("20.00"), service.balanceOf("B")),
                () -> assertTrue(service.notifications().isEmpty())
        );
    }

    @Test void bankTransferHasExistingLimit() {
        service.addAccount("A", new BigDecimal("20000.00"));
        var error = assertThrows(TransferException.class, () -> service.transfer(
                new TransferRequest("A", "B", new BigDecimal("10000.01"), "BANK_TRANSFER")));
        assertEquals("bank transfer limit exceeded", error.getMessage());
    }
}
