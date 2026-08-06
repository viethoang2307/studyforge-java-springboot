package dev.studyforge.concurrency;

/** A volatile flag is appropriate because stopping is one independent read/write. */
public final class StopFlag {
    private volatile boolean stopRequested;

    public void requestStop() {
        stopRequested = true;
    }

    public boolean isStopRequested() {
        return stopRequested;
    }
}
