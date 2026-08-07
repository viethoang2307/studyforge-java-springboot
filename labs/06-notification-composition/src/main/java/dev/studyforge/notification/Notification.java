package dev.studyforge.notification;

import java.util.Objects;

/** A capability that can be supplied to any service needing notifications. */
public interface Notification {

    void send(String recipient, String message);

    /**
     * A default method evolves the contract without forcing every implementation
     * to duplicate common input validation. Implementations may still override it.
     */
    default void sendIfPresent(String recipient, String message) {
        Objects.requireNonNull(recipient, "recipient must not be null");
        if (message != null && !message.isBlank()) {
            send(recipient, message);
        }
    }
}
