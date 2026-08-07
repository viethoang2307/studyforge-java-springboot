package com.studyforge.lab;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.Executors;

public final class BackendOperationsApplication {

    private static final int PORT = 8085;

    private BackendOperationsApplication() {
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/hello", BackendOperationsApplication::handleHello);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(1)));
        server.start();
        System.out.printf("Server listening on http://localhost:%d%n", PORT);
    }

    private static void handleHello(HttpExchange exchange) throws IOException {
        System.out.printf("%s Received %s %s%n", Instant.now(),
                exchange.getRequestMethod(), exchange.getRequestURI());
        if (!"GET".equals(exchange.getRequestMethod())) {
            send(exchange, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        String body = "{\"message\":\"Hello from StudyForge!\",\"timestamp\":\""
                + Instant.now() + "\"}";
        send(exchange, 200, body);
    }

    private static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var responseBody = exchange.getResponseBody()) {
            responseBody.write(bytes);
        }
    }
}
