package dev.studyforge.wallet;

public interface TransferNotifier {
    void transferCompleted(TransferReceipt receipt);
}
