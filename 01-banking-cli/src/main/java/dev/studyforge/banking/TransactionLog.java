package dev.studyforge.banking;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** A resource whose I/O failures propagate to the caller instead of being hidden. */
public final class TransactionLog implements AutoCloseable {
    private final BufferedWriter writer;

    public TransactionLog(Path path) throws IOException {
        writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public void append(String entry) throws IOException {
        writer.write(entry);
        writer.newLine();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    public static void writeOne(Path path, String entry) throws IOException {
        try (TransactionLog log = new TransactionLog(path)) {
            log.append(entry);
        }
    }
}
