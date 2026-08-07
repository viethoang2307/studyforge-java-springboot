package dev.studyforge.legacy;

import java.io.PrintStream;

/**
 * Deliberately bad example: the service extends a sender only to reuse one method.
 * This falsely claims that an order service "is an" email sender and fixes the
 * delivery channel at compile time. Prefer the composition-based OrderService.
 */
@Deprecated(forRemoval = false)
public final class BadEmailOrderService extends EmailSenderBase {
    public BadEmailOrderService(PrintStream output) {
        super(output);
    }

    public void confirm(String orderId, String recipient) {
        sendEmail(recipient, "Order " + orderId + " confirmed");
    }
}

abstract class EmailSenderBase {
    private final PrintStream output;

    protected EmailSenderBase(PrintStream output) {
        this.output = output;
    }

    protected void sendEmail(String recipient, String message) {
        output.printf("EMAIL to=%s: %s%n", recipient, message);
    }
}
