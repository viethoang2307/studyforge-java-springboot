package dev.studyforge.service;

import dev.studyforge.notification.ConsoleNotification;
import dev.studyforge.notification.EmailNotification;
import dev.studyforge.notification.Notification;
import dev.studyforge.notification.SmsNotification;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderServiceTest {

    @Test
    void canSwapNotificationBehaviorWithoutChangingOrderService() {
        var deliveries = new ArrayList<String>();
        Notification fake = (recipient, message) -> deliveries.add(recipient + ":" + message);

        new OrderService(fake).confirm("A-42", "customer-1");

        assertEquals(List.of("customer-1:Order A-42 confirmed"), deliveries);
    }

    @Test
    void defaultMethodSkipsBlankMessages() {
        var deliveries = new ArrayList<String>();
        Notification fake = (recipient, message) -> deliveries.add(message);

        fake.sendIfPresent("customer-1", "  ");

        assertTrue(deliveries.isEmpty());
    }

    @Test
    void allThreeImplementationsHonorTheContract() {
        assertOutput(EmailNotification::new, "EMAIL to=user: hello");
        assertOutput(SmsNotification::new, "SMS to=user: hello");
        assertOutput(ConsoleNotification::new, "CONSOLE [user] hello");
    }

    private static void assertOutput(
            Function<PrintStream, Notification> factory, String expected) {
        var output = new ByteArrayOutputStream();
        var stream = new PrintStream(output, true, StandardCharsets.UTF_8);
        var notification = factory.apply(stream);
        notification.send("user", "hello");
        assertEquals(expected + System.lineSeparator(), output.toString(StandardCharsets.UTF_8));
    }
}
