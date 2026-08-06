package com.studyforge.generics.repository;

import java.util.List;
import java.util.Optional;

public interface Repository<ID, E extends Identifiable<ID>> {
    E save(E entity);

    Optional<E> findById(ID id);

    List<E> findAll();

    boolean deleteById(ID id);
}
