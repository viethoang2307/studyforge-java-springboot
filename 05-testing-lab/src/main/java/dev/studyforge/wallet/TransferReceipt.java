package dev.studyforge.wallet;

public record TransferReceipt(String sourceAccountId, String destinationAccountId, Money amount) {
}
