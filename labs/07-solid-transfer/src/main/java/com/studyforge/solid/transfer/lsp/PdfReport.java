package dev.studyforge.solid.transfer.lsp;

import java.nio.charset.StandardCharsets;

public final class PdfReport implements ExportableReport {
    @Override public byte[] export() { return "PDF content".getBytes(StandardCharsets.UTF_8); }
}

