package dev.studyforge.concurrency;

/** Demonstrates that volatile visibility does not make value++ atomic. */
public final class VolatileCounter implements Counter {
    private volatile long value;

    @Override
    public void increment() {
        value++;
    }

    @Override
    public long value() {
        return value;
    }
}
