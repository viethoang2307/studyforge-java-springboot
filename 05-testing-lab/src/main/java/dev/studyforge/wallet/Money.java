package dev.studyforge.wallet;

/** Value object expressed in the smallest currency unit; deliberately not mocked in tests. */
public record Money(long cents) {
    public Money {
        if (cents < 0) {
            throw new IllegalArgumentException("Money cannot be negative");
        }
    }

    public static Money ofCents(long cents) {
        return new Money(cents);
    }
}
