package dev.studyforge.wallet;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class TransferConcurrencyTest {
    @Test
    @Timeout(10)
    void oppositeConcurrentTransfersPreserveTotalBalanceAndDoNotDeadlock() throws Exception {
        Account first = new Account("A", Money.ofCents(10_000));
        Account second = new Account("B", Money.ofCents(10_000));
        Map<String, Account> stored = Map.of("A", first, "B", second);
        AccountRepository repository = id -> Optional.ofNullable(stored.get(id));
        TransferService service = new TransferService(repository, ignored -> { });
        int transferCount = 200;
        CountDownLatch start = new CountDownLatch(1);

        try (ExecutorService workers = Executors.newFixedThreadPool(8)) {
            Future<?>[] tasks = new Future<?>[transferCount];
            for (int i = 0; i < transferCount; i++) {
                int index = i;
                tasks[i] = workers.submit(() -> {
                    start.await();
                    if (index % 2 == 0) {
                        service.transfer("A", "B", Money.ofCents(1));
                    } else {
                        service.transfer("B", "A", Money.ofCents(1));
                    }
                    return null;
                });
            }
            start.countDown();
            for (Future<?> task : tasks) {
                task.get();
            }
        }

        long total = first.balance().cents() + second.balance().cents();
        assertAll(
                () -> assertEquals(20_000, total, "money must neither appear nor disappear"),
                () -> assertTrue(first.balance().cents() >= 0),
                () -> assertTrue(second.balance().cents() >= 0),
                () -> assertEquals(Money.ofCents(10_000), first.balance()),
                () -> assertEquals(Money.ofCents(10_000), second.balance()));
    }
}
