package com.studyforge.generics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class GenericAlgorithmsTest {
    @Test
    void boxKeepsItsDeclaredType() {
        Box<String> box = new Box<>("Java");
        box.set("Generics");
        assertEquals("Generics", box.get());
    }

    @Test
    void maxAcceptsAListOfComparableValues() {
        assertEquals(9, GenericAlgorithms.max(List.of(3, 9, 4)));
        assertThrows(IllegalArgumentException.class, () -> GenericAlgorithms.max(List.<Integer>of()));
    }

    @Test
    void copyUsesAProducerAndAConsumer() {
        List<Integer> source = List.of(1, 2, 3);
        List<Number> destination = new ArrayList<>(List.of(0.5));

        GenericAlgorithms.copy(source, destination);

        assertEquals(List.of(0.5, 1, 2, 3), destination);
    }
}
