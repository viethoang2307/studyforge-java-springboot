package dev.studyforge.payments;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class PaymentService {

    private final Map<PaymentType, PaymentMethod> methods;

    public PaymentService(List<PaymentMethod> methods) {
        Objects.requireNonNull(methods, "methods must not be null");
        EnumMap<PaymentType, PaymentMethod> indexedMethods = new EnumMap<>(PaymentType.class);
        for (PaymentMethod method : methods) {
            Objects.requireNonNull(method, "payment method must not be null");
            if (indexedMethods.putIfAbsent(method.type(), method) != null) {
                throw new IllegalArgumentException("duplicate payment method: " + method.type());
            }
        }
        this.methods = Map.copyOf(indexedMethods);
    }

    public PaymentReceipt pay(PaymentType type, PaymentRequest request) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(request, "request must not be null");
        PaymentMethod method = methods.get(type);
        if (method == null) {
            throw new UnsupportedPaymentTypeException(type);
        }
        return method.pay(request);
    }
}
