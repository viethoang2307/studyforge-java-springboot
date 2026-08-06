package com.studyforge.generics;

import java.util.List;
import java.util.Objects;

public final class GenericAlgorithms {
    private GenericAlgorithms() {
    }

    /** Generic method with a recursive upper bound. */
    public static <T extends Comparable<? super T>> T max(List<? extends T> values) {
        Objects.requireNonNull(values);
        if (values.isEmpty()) {
            throw new IllegalArgumentException("values must not be empty");
        }

        T result = Objects.requireNonNull(values.get(0));
        for (T value : values) {
            if (Objects.requireNonNull(value).compareTo(result) > 0) {
                result = value;
            }
        }
        return result;
    }

    /**
     * Copies producers into consumers (PECS). The source may produce T or a
     * subtype; the destination may consume T or any of its supertypes.
     */
    public static <T> void copy(List<? extends T> source, List<? super T> destination) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(destination);
        destination.addAll(source);
    }
}
