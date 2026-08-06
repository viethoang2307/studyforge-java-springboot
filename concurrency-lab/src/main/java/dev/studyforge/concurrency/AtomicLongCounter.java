package dev.studyforge.concurrency;

import java.util.concurrent.atomic.AtomicLong;

public final class AtomicLongCounter implements Counter {
    private final AtomicLong value = new AtomicLong();

    @Override
    public void increment() {
        value.incrementAndGet();
    }

    @Override
    public long value() {
        return value.get();
    }
}
