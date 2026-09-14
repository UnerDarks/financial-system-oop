package io.github.unerdarks.utils;

import java.util.List;

public interface CrudService<T, ID> {

    T create(T entity);

    T findById(ID id);

    List<T> findAll();

    T update(ID id, T entity);

    void delete(ID id);
}