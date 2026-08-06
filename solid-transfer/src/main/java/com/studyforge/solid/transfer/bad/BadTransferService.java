package com.studyforge.solid.transfer.bad;

import com.studyforge.solid.transfer.TransferException;
import com.studyforge.solid.transfer.TransferReceipt;
import com.studyforge.solid.transfer.TransferRequest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Deliberately bad teaching example: validation, rules, storage and notification all change this class. */
public final class BadTransferService {
    private final Map<String, BigDecimal> balances = new HashMap<>();
    private final List<String> notifications = new ArrayList<>();

    public void addAccount(String id, BigDecimal balance) { balances.put(id, balance); }

    public TransferReceipt transfer(TransferRequest request) {
        if (request == null || request.amount() == null || request.amount().signum() <= 0) {
            throw new TransferException("amount must be positive");
        }
        if (request.sourceAccountId().equals(request.targetAccountId())) {
            throw new TransferException("accounts must be different");
        }
        BigDecimal source = balances.get(request.sourceAccountId());
        BigDecimal target = balances.get(request.targetAccountId());
        if (source == null || target == null) throw new TransferException("account not found");
        if (source.compareTo(request.amount()) < 0) throw new TransferException("insufficient funds");

        // OCP violation: every new method requires another branch in this service.
        if ("WALLET".equals(request.paymentMethod())) {
            // no extra rule
        } else if ("BANK_TRANSFER".equals(request.paymentMethod())) {
            if (request.amount().compareTo(new BigDecimal("10000.00")) > 0) {
                throw new TransferException("bank transfer limit exceeded");
            }
        } else {
            throw new TransferException("unsupported payment method");
        }

        balances.put(request.sourceAccountId(), source.subtract(request.amount()));
        balances.put(request.targetAccountId(), target.add(request.amount()));
        notifications.add("Transferred " + request.amount() + " to " + request.targetAccountId());
        return new TransferReceipt(request.sourceAccountId(), request.targetAccountId(),
                request.amount(), request.paymentMethod());
    }

    public BigDecimal balanceOf(String id) { return balances.get(id); }
    public List<String> notifications() { return List.copyOf(notifications); }
}
