package dev.studyforge.concurrency;

public final class SynchronizedCounter implements Counter {
    private long value;

    @Override
    public synchronized void increment() {
        value++;
    }

    @Override
    public synchronized long value() {
        return value;
    }
}
