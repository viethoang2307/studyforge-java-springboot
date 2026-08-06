package com.studyforge.transfer;

import java.util.Optional;

public interface AccountRepository {
    Optional<Account> findById(String accountId);

    void saveTransfer(Account source, Account destination);
}
