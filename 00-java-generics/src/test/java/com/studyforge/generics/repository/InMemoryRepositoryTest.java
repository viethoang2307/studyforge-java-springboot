package com.studyforge.generics.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class InMemoryRepositoryTest {
    private record Student(UUID id, String name) implements Identifiable<UUID> {
    }

    @Test
    void supportsCreateUpdateReadAndDeleteWithoutCasts() {
        Repository<UUID, Student> repository = new InMemoryRepository<>();
        UUID id = UUID.randomUUID();

        repository.save(new Student(id, "An"));
        repository.save(new Student(id, "Binh"));

        assertEquals("Binh", repository.findById(id).orElseThrow().name());
        assertEquals(1, repository.findAll().size());
        assertTrue(repository.deleteById(id));
        assertFalse(repository.findById(id).isPresent());
    }
}
