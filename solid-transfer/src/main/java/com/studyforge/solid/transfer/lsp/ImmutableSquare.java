package com.studyforge.solid.transfer.lsp;

public record ImmutableSquare(int side) implements Shape {
    public ImmutableSquare {
        if (side <= 0) throw new IllegalArgumentException("side must be positive");
    }
    @Override public int area() { return side * side; }
}
