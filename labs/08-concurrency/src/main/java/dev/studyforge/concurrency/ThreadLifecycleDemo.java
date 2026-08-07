package dev.studyforge.concurrency;

import java.util.concurrent.CountDownLatch;

public final class ThreadLifecycleDemo {
    private ThreadLifecycleDemo() {
    }

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch releaseWorker = new CountDownLatch(1);
        Thread worker = Thread.ofPlatform().name("lifecycle-worker").unstarted(() -> {
            try {
                releaseWorker.await();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        });

        print(worker);                 // NEW
        worker.start();
        waitUntilWaiting(worker);
        print(worker);                 // WAITING
        releaseWorker.countDown();
        worker.join();                 // join creates a happens-before edge
        print(worker);                 // TERMINATED
    }

    private static void waitUntilWaiting(Thread worker) {
        while (worker.getState() != Thread.State.WAITING) {
            Thread.onSpinWait();
        }
    }

    private static void print(Thread thread) {
        System.out.printf("%-20s %s%n", thread.getName(), thread.getState());
    }
}
