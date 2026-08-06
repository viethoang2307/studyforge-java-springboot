package com.studyforge.solid.transfer.refactored;

import com.studyforge.solid.transfer.TransferException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public final class InMemoryAccountRepository implements AccountRepository {
    private final Map<String, BigDecimal> balances = new HashMap<>();
    public void add(String id, BigDecimal balance) { balances.put(id, balance); }
    @Override public boolean exists(String id) { return balances.containsKey(id); }
    @Override public BigDecimal balanceOf(String id) { return balances.get(id); }

    @Override public void transfer(String sourceId, String targetId, BigDecimal amount) {
        BigDecimal source = balances.get(sourceId);
        BigDecimal target = balances.get(targetId);
        if (source.compareTo(amount) < 0) throw new TransferException("insufficient funds");
        balances.put(sourceId, source.subtract(amount));
        balances.put(targetId, target.add(amount));
    }
}
