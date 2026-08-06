package dev.studyforge.notification;

import java.io.PrintStream;
import java.util.Objects;

public final class EmailNotification implements Notification {
    private final PrintStream output;

    public EmailNotification(PrintStream output) {
        this.output = Objects.requireNonNull(output);
    }

    @Override
    public void send(String recipient, String message) {
        output.printf("EMAIL to=%s: %s%n", recipient, message);
    }
}
