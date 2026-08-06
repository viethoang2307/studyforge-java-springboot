package com.studyforge.solid.transfer.refactored;

import com.studyforge.solid.transfer.TransferException;
import com.studyforge.solid.transfer.TransferReceipt;
import com.studyforge.solid.transfer.TransferRequest;
import com.studyforge.solid.transfer.refactored.payment.BankTransferPayment;
import com.studyforge.solid.transfer.refactored.payment.WalletPayment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransferServiceTest {
    private InMemoryAccountRepository accounts;
    private List<TransferReceipt> sent;
    private TransferService service;

    @BeforeEach void setUp() {
        accounts = new InMemoryAccountRepository();
        accounts.add("A", new BigDecimal("100.00"));
        accounts.add("B", new BigDecimal("20.00"));
        sent = new ArrayList<>();
        service = new TransferService(new TransferValidator(), accounts, sent::add,
                List.of(new WalletPayment(), new BankTransferPayment()));
    }

    @Test void preservesCharacterizedWalletBehavior() {
        var receipt = service.transfer(new TransferRequest("A", "B", new BigDecimal("30.00"), "WALLET"));
        assertAll(
                () -> assertEquals(new BigDecimal("70.00"), accounts.balanceOf("A")),
                () -> assertEquals(new BigDecimal("50.00"), accounts.balanceOf("B")),
                () -> assertEquals(receipt, sent.get(0))
        );
    }

    @Test void addingPaymentMethodDoesNotModifyTransferService() {
        PaymentMethod instantPayment = new PaymentMethod() {
            @Override public String id() { return "INSTANT"; }
            @Override public void verify(TransferRequest request) { }
        };
        service = new TransferService(new TransferValidator(), accounts, sent::add, List.of(instantPayment));
        assertDoesNotThrow(() -> service.transfer(
                new TransferRequest("A", "B", BigDecimal.ONE, "INSTANT")));
    }

    @Test void methodSpecificRuleIsPolymorphic() {
        accounts.add("A", new BigDecimal("20000.00"));
        var error = assertThrows(TransferException.class, () -> service.transfer(
                new TransferRequest("A", "B", new BigDecimal("10000.01"), "BANK_TRANSFER")));
        assertEquals("bank transfer limit exceeded", error.getMessage());
    }

    @Test void invalidAmountChangesNothing() {
        assertThrows(TransferException.class, () -> service.transfer(
                new TransferRequest("A", "B", BigDecimal.ZERO, "WALLET")));
        assertAll(() -> assertEquals(new BigDecimal("100.00"), accounts.balanceOf("A")),
                () -> assertEquals(new BigDecimal("20.00"), accounts.balanceOf("B")),
                () -> assertTrue(sent.isEmpty()));
    }
}
