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
class CardPaymentTest {

    @Mock
    private PaymentGateway gateway;

    @Test
    void delegatesChargeToCardGateway() {
        PaymentRequest request = request("card-token");
        when(gateway.charge(request)).thenReturn("card-tx-1");

        PaymentReceipt receipt = new CardPayment(gateway).pay(request);

        assertEquals(new PaymentReceipt("card-tx-1", PaymentType.CARD), receipt);
        verify(gateway).charge(request);
    }

    private PaymentRequest request(String source) {
        return new PaymentRequest(source, new BigDecimal("100.50"), Currency.getInstance("USD"));
    }
}
