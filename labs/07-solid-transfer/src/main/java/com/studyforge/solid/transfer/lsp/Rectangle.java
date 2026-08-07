package dev.studyforge.solid.transfer.lsp;

/** Mutable design used to demonstrate why Square is not a behavioral Rectangle. */
public class Rectangle {
    private int width;
    private int height;
    public Rectangle(int width, int height) { this.width = width; this.height = height; }
    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
    public int area() { return width * height; }
}

