package dev.studyforge.concurrency;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class StopFlagTest {
    @Test
    @Timeout(2)
    void volatileWriteBecomesVisibleToReader() throws InterruptedException {
        StopFlag flag = new StopFlag();
        Thread worker = Thread.ofPlatform().start(() -> {
            while (!flag.isStopRequested()) {
                Thread.onSpinWait();
            }
        });

        flag.requestStop();
        worker.join(Duration.ofSeconds(1));

        assertFalse(worker.isAlive());
    }
}
