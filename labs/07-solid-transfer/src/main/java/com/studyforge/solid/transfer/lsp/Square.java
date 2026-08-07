package dev.studyforge.solid.transfer.lsp;

/** Deliberate LSP violation: strengthening Square's invariant breaks Rectangle setters' postconditions. */
public final class Square extends Rectangle {
    public Square(int side) { super(side, side); }
    @Override public void setWidth(int width) { super.setWidth(width); super.setHeight(width); }
    @Override public void setHeight(int height) { super.setWidth(height); super.setHeight(height); }
}

