package dev.studyforge.concurrency;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public final class CounterRunner {
    private CounterRunner() {
    }

    public static long run(Counter counter, int workers, int incrementsPerWorker, Duration timeout)
            throws InterruptedException {
        CountDownLatch ready = new CountDownLatch(workers);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(workers);
        List<Thread> threads = new ArrayList<>();

        for (int worker = 0; worker < workers; worker++) {
            Thread thread = Thread.ofPlatform().name("counter-worker-" + worker).unstarted(() -> {
                ready.countDown();
                try {
                    start.await();
                    for (int i = 0; i < incrementsPerWorker; i++) {
                        counter.increment();
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
            threads.add(thread);
            thread.start();
        }

        ready.await();
        start.countDown();
        if (!done.await(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS)) {
            threads.forEach(Thread::interrupt);
            throw new IllegalStateException("Workers did not finish within " + timeout);
        }
        return counter.value();
    }
}
