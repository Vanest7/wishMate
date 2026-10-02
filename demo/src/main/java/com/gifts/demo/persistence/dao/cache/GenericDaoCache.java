package com.gifts.demo.persistence.dao.cache;

public interface GenericDaoCache<T> {
    void save(T entity);
    void clear();
}
