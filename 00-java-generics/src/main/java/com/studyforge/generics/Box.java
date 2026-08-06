package com.studyforge.generics;

import java.util.Objects;

/** A minimal generic class: the caller chooses the type stored in the box. */
public final class Box<T> {
    private T value;

    public Box(T value) {
        this.value = Objects.requireNonNull(value);
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = Objects.requireNonNull(value);
    }
}
