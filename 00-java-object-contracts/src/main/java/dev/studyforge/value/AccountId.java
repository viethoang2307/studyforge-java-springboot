package dev.studyforge.value;

import java.util.Objects;
import java.util.UUID;

/** Type-safe, immutable account identifier. */
public record AccountId(UUID value) {
    public AccountId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static AccountId from(String value) {
        Objects.requireNonNull(value, "value must not be null");
        return new AccountId(UUID.fromString(value));
    }

    public static AccountId random() {
        return new AccountId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
