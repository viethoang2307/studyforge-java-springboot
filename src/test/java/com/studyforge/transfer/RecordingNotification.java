package com.studyforge.transfer;

import java.util.ArrayList;
import java.util.List;

final class RecordingNotification implements NotificationPort {
    private final List<Transaction> sent = new ArrayList<>();

    @Override
    public void transferCompleted(Transaction transaction) {
        sent.add(transaction);
    }

    List<Transaction> sent() {
        return List.copyOf(sent);
    }
}
