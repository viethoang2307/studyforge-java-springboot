package dev.studyforge.payments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.Test;

class PaymentServiceTest {

    @Test
    void dispatchesToRegisteredImplementationWithoutTypeConditionals() {
        PaymentMethod card = mock(PaymentMethod.class);
        PaymentRequest request = request();
        PaymentReceipt expected = new PaymentReceipt("tx-1", PaymentType.CARD);
        when(card.type()).thenReturn(PaymentType.CARD);
        when(card.pay(request)).thenReturn(expected);
        PaymentService service = new PaymentService(List.of(card));

        PaymentReceipt actual = service.pay(PaymentType.CARD, request);

        assertEquals(expected, actual);
        verify(card).pay(request);
    }

    @Test
    void rejectsAnUnregisteredType() {
        PaymentService service = new PaymentService(List.of());

        assertThrows(UnsupportedPaymentTypeException.class,
                () -> service.pay(PaymentType.WALLET, request()));
    }

    @Test
    void rejectsDuplicateImplementationsDuringConfiguration() {
        PaymentMethod first = mock(PaymentMethod.class);
        PaymentMethod second = mock(PaymentMethod.class);
        when(first.type()).thenReturn(PaymentType.CARD);
        when(second.type()).thenReturn(PaymentType.CARD);

        assertThrows(IllegalArgumentException.class,
                () -> new PaymentService(List.of(first, second)));
    }

    private PaymentRequest request() {
        return new PaymentRequest("token", BigDecimal.TEN, Currency.getInstance("USD"));
    }
}
