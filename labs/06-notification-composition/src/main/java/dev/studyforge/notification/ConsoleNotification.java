package dev.studyforge.notification;

import java.io.PrintStream;
import java.util.Objects;

public final class ConsoleNotification implements Notification {
    private final PrintStream output;

    public ConsoleNotification(PrintStream output) {
        this.output = Objects.requireNonNull(output);
    }

    @Override
    public void send(String recipient, String message) {
        output.printf("CONSOLE [%s] %s%n", recipient, message);
    }
}
