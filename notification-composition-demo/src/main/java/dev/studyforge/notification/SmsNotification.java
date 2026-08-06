package dev.studyforge.notification;

import java.io.PrintStream;
import java.util.Objects;

public final class SmsNotification implements Notification {
    private final PrintStream output;

    public SmsNotification(PrintStream output) {
        this.output = Objects.requireNonNull(output);
    }

    @Override
    public void send(String recipient, String message) {
        output.printf("SMS to=%s: %s%n", recipient, message);
    }
}
