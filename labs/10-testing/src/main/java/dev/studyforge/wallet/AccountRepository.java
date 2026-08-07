package dev.studyforge.wallet;

import java.util.Optional;

public interface AccountRepository {
    Optional<Account> findById(String accountId);
}
