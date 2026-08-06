package dev.studyforge.concurrency;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class CounterConcurrencyTest {
    private static final int WORKERS = 8;
    private static final int INCREMENTS = 10_000;
    private static final Duration WORKER_TIMEOUT = Duration.ofSeconds(3);

    @Test
    @Timeout(5)
    void unsafeCounterReproduciblyLosesUpdates() throws InterruptedException {
        Counter counter = new UnsafeCounter(WORKERS);

        long actual = CounterRunner.run(counter, WORKERS, INCREMENTS, WORKER_TIMEOUT);

        assertEquals(INCREMENTS, actual,
                "all workers overwrite one another after reading the same value");
    }

    @ParameterizedTest(name = "{0} preserves every increment")
    @MethodSource("safeCounters")
    @Timeout(5)
    void threadSafeCountersPreserveEveryIncrement(NamedCounter named) throws InterruptedException {
        long actual = CounterRunner.run(named.counter(), WORKERS, INCREMENTS, WORKER_TIMEOUT);

        assertEquals((long) WORKERS * INCREMENTS, actual);
    }

    private static Stream<NamedCounter> safeCounters() {
        return Stream.of(
                new NamedCounter("synchronized", new SynchronizedCounter()),
                new NamedCounter("AtomicLong", new AtomicLongCounter()),
                new NamedCounter("LongAdder", new LongAdderCounter()));
    }

    private record NamedCounter(String name, Counter counter) {
        @Override
        public String toString() {
            return name;
        }
    }
}
