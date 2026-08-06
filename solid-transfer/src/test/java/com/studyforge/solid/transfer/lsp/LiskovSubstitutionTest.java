package com.studyforge.solid.transfer.lsp;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LiskovSubstitutionTest {
    private static void resizeRectangle(Rectangle rectangle) {
        rectangle.setWidth(5);
        rectangle.setHeight(4);
    }

    @Test void squareCannotSatisfyRectanglePostconditions() {
        Rectangle rectangle = new Rectangle(1, 1);
        resizeRectangle(rectangle);
        assertEquals(20, rectangle.area());

        Rectangle squareAsRectangle = new Square(1);
        resizeRectangle(squareAsRectangle);
        assertNotEquals(20, squareAsRectangle.area(),
                "substitution changes the observable Rectangle behavior");
    }

    @Test void honestShapeContractSupportsBothImplementations() {
        Shape rectangle = new ImmutableRectangle(5, 4);
        Shape square = new ImmutableSquare(4);
        assertAll(() -> assertEquals(20, rectangle.area()), () -> assertEquals(16, square.area()));
    }

    @Test void capabilityInterfaceNeedsNoUnsupportedOperation() {
        ExportableReport report = new PdfReport();
        assertTrue(report.export().length > 0);
    }
}
