package dev.studyforge.payments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Currency;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BankTransferPaymentTest {

    @Mock
    private PaymentGateway gateway;

    @Test
    void delegatesTransferToBankGateway() {
        PaymentRequest request = request("BANK-ACCOUNT-01");
        when(gateway.charge(request)).thenReturn("bank-tx-1");

        PaymentReceipt receipt = new BankTransferPayment(gateway).pay(request);

        assertEquals(new PaymentReceipt("bank-tx-1", PaymentType.BANK_TRANSFER), receipt);
        verify(gateway).charge(request);
    }

    private PaymentRequest request(String source) {
        return new PaymentRequest(source, new BigDecimal("125000"), Currency.getInstance("VND"));
    }
}
