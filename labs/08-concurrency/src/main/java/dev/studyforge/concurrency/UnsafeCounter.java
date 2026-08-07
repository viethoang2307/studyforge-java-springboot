package dev.studyforge.concurrency;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/** A deliberately broken counter used to observe a read-modify-write race. */
public final class UnsafeCounter implements Counter {
    private long value;
    private final CyclicBarrier afterRead;

    public UnsafeCounter() {
        this.afterRead = null;
    }

    /**
     * Makes the race reproducible for a lab: every worker reads before any worker writes.
     */
    public UnsafeCounter(int concurrentWorkers) {
        if (concurrentWorkers < 2) {
            throw new IllegalArgumentException("concurrentWorkers must be at least 2");
        }
        this.afterRead = new CyclicBarrier(concurrentWorkers);
    }

    @Override
    public void increment() {
        long current = value;
        awaitWorkersAfterRead();
        value = current + 1;
    }

    @Override
    public long value() {
        return value;
    }

    private void awaitWorkersAfterRead() {
        if (afterRead == null) {
            return;
        }
        try {
            afterRead.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while coordinating the race", exception);
        } catch (BrokenBarrierException exception) {
            throw new IllegalStateException("Race coordination barrier was broken", exception);
        }
    }
}
