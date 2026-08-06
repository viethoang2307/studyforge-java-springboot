package dev.studyforge.collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import org.junit.jupiter.api.Test;

class IteratorBehaviorTest {
    @Test
    void structuralChangeOutsideIteratorIsDetectedOnBestEffortBasis() {
        List<Integer> values = new ArrayList<>(List.of(1, 2, 3));
        var iterator = values.iterator();
        iterator.next();
        values.add(4);

        assertThrows(ConcurrentModificationException.class, iterator::next);
    }

    @Test
    void iteratorRemoveIsTheSupportedMutationPath() {
        List<Integer> values = new ArrayList<>(List.of(1, 2, 3));
        var iterator = values.iterator();
        iterator.next();

        assertDoesNotThrow(iterator::remove);
        assertEquals(List.of(2, 3), values);
    }
}
