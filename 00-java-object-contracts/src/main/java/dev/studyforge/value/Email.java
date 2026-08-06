package dev.studyforge.value;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/** Immutable email value object with deliberately small, application-level validation. */
public final class Email {
    private static final Pattern SIMPLE_EMAIL =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final String value;

    private Email(String value) {
        this.value = value;
    }

    public static Email of(String rawValue) {
        Objects.requireNonNull(rawValue, "email must not be null");
        String normalized = rawValue.strip().toLowerCase(Locale.ROOT);
        if (!SIMPLE_EMAIL.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid email address");
        }
        return new Email(normalized);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        return other instanceof Email email && value.equals(email.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
