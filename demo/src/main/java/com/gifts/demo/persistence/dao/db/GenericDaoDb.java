package com.gifts.demo.persistence.dao.db;

import com.gifts.demo.domain.model.ListWithCount;

import java.util.List;
import java.util.Optional;

public interface GenericDaoDb<T> {

    List<T> getAll();
    ListWithCount<T> getAll(int page, int size);
    Optional<T> findById(Integer id);
    Integer insert(T item);
    void update(T item);
    void delete(Integer id);
    long count();
    T save(T item);
}