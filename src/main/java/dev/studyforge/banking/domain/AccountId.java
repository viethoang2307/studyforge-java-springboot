package dev.studyforge.banking.domain;

import java.util.Objects;

public record AccountId(String value) {
    public AccountId {
        Objects.requireNonNull(value, "account id");
        if (value.isBlank()) throw new IllegalArgumentException("Account id must not be blank");
    }
}
