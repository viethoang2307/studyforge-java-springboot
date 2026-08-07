package dev.studyforge.solid.transfer.refactored;

import dev.studyforge.solid.transfer.TransferReceipt;

/** Boundary for a side effect whose delivery mechanism can change independently. */
public interface NotificationGateway {
    void transferCompleted(TransferReceipt receipt);
}

