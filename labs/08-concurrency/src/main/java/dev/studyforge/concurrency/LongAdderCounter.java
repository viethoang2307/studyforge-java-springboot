package dev.studyforge.concurrency;

import java.util.concurrent.atomic.LongAdder;

public final class LongAdderCounter implements Counter {
    private final LongAdder value = new LongAdder();

    @Override
    public void increment() {
        value.increment();
    }

    @Override
    public long value() {
        return value.sum();
    }
}
