package dev.studyforge.solid.transfer.lsp;

public record ImmutableRectangle(int width, int height) implements Shape {
    public ImmutableRectangle {
        if (width <= 0 || height <= 0) throw new IllegalArgumentException("dimensions must be positive");
    }
    @Override public int area() { return width * height; }
}

