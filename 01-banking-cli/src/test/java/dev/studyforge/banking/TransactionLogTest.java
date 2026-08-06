package dev.studyforge.banking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionLogTest {
    @Test
    void tryWithResourcesFlushesAndClosesTheLog(@TempDir Path directory) throws IOException {
        Path log = directory.resolve("transactions.txt");

        TransactionLog.writeOne(log, "alice -> bob: 10.00");

        assertEquals("alice -> bob: 10.00", Files.readString(log).trim());
    }
}
