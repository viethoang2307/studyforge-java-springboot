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
class WalletPaymentTest {

    @Mock
    private PaymentGateway gateway;

    @Test
    void delegatesDebitToWalletGateway() {
        PaymentRequest request = request("wallet-42");
        when(gateway.charge(request)).thenReturn("wallet-tx-1");

        PaymentReceipt receipt = new WalletPayment(gateway).pay(request);

        assertEquals(new PaymentReceipt("wallet-tx-1", PaymentType.WALLET), receipt);
        verify(gateway).charge(request);
    }

    private PaymentRequest request(String source) {
        return new PaymentRequest(source, new BigDecimal("75000"), Currency.getInstance("VND"));
    }
}
