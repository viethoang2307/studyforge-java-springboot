package dev.studyforge.service;

import dev.studyforge.notification.Notification;

import java.util.Objects;

/** An OrderService has a Notification; it is not a Notification. */
public final class OrderService {
    private final Notification notification;

    public OrderService(Notification notification) {
        this.notification = Objects.requireNonNull(notification);
    }

    public void confirm(String orderId, String recipient) {
        Objects.requireNonNull(orderId, "orderId must not be null");
        notification.sendIfPresent(recipient, "Order " + orderId + " confirmed");
    }
}
