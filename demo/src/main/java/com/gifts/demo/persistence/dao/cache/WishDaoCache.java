package com.gifts.demo.persistence.dao.cache;

import com.gifts.demo.domain.model.Wish;

import java.util.List;
import java.util.Optional;

public interface WishDaoCache extends GenericDaoCache {

    Optional<Wish> findById(Long wishId);

    List<Wish> getByUserId(Long userId);

    void save(Wish entity);
}
