package com.studyforge.solid.transfer.refactored;

import com.studyforge.solid.transfer.TransferException;
import com.studyforge.solid.transfer.TransferReceipt;
import com.studyforge.solid.transfer.TransferRequest;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Application orchestration only; rules and external effects live behind focused collaborators. */
public final class TransferService {
    private final TransferValidator validator;
    private final AccountRepository accounts;
    private final NotificationGateway notifications;
    private final Map<String, PaymentMethod> paymentMethods;

    public TransferService(TransferValidator validator, AccountRepository accounts,
                           NotificationGateway notifications, Collection<PaymentMethod> methods) {
        this.validator = validator;
        this.accounts = accounts;
        this.notifications = notifications;
        this.paymentMethods = methods.stream().collect(Collectors.toUnmodifiableMap(
                PaymentMethod::id, Function.identity()));
    }

    public TransferReceipt transfer(TransferRequest request) {
        validator.validate(request);
        if (!accounts.exists(request.sourceAccountId()) || !accounts.exists(request.targetAccountId())) {
            throw new TransferException("account not found");
        }
        PaymentMethod method = paymentMethods.get(request.paymentMethod());
        if (method == null) throw new TransferException("unsupported payment method");
        method.verify(request);
        accounts.transfer(request.sourceAccountId(), request.targetAccountId(), request.amount());
        var receipt = new TransferReceipt(request.sourceAccountId(), request.targetAccountId(),
                request.amount(), method.id());
        notifications.transferCompleted(receipt);
        return receipt;
    }
}
