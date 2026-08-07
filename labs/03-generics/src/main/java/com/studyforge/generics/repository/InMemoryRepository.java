package dev.studyforge.generics.repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class InMemoryRepository<ID, E extends Identifiable<ID>>
        implements Repository<ID, E> {
    private final Map<ID, E> entities = new LinkedHashMap<>();

    @Override
    public E save(E entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        entities.put(Objects.requireNonNull(entity.id(), "id must not be null"), entity);
        return entity;
    }

    @Override
    public Optional<E> findById(ID id) {
        return Optional.ofNullable(entities.get(Objects.requireNonNull(id)));
    }

    @Override
    public List<E> findAll() {
        return List.copyOf(entities.values());
    }

    @Override
    public boolean deleteById(ID id) {
        return entities.remove(Objects.requireNonNull(id)) != null;
    }
}

