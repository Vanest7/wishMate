package com.gifts.demo.persistence.dao.cache.impl;

import com.gifts.demo.domain.model.Wish;
import com.gifts.demo.persistence.dao.cache.WishDaoCache;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WishDaoCacheImpl implements WishDaoCache {

    private final Map<String, Wish> cache = new ConcurrentHashMap<>();
    private final Map<String, List<Wish>> userWishesCache = new ConcurrentHashMap<>();
    private final Map<String, Long> expiration = new ConcurrentHashMap<>();
    private static final long TTL = 600_000L;

    @Override
    public Optional<Wish> findById(Long id) {
        Long expirationTime = expiration.get(String.valueOf(id));
        if(expirationTime != null && expirationTime >= System.currentTimeMillis()) {
            System.out.println("Retrieved from cache: " + id);
            return Optional.ofNullable(cache.get(String.valueOf(id)));
        }
        cache.remove(String.valueOf(id));
        expiration.remove(String.valueOf(id));
        return Optional.empty();
    }

    @Override
    public List<Wish> getByUserId(Long userId) {
        String key = "user_" + userId;
        Long expirationTime = expiration.get(key);
        if (expirationTime != null && expirationTime >= System.currentTimeMillis()) {
            System.out.println("Retrieved from cache: user wishes for userId=" + userId);
            return userWishesCache.getOrDefault(key, java.util.Collections.emptyList());
        }
        userWishesCache.remove(key);
        expiration.remove(key);
        return java.util.Collections.emptyList();
    }

    public void saveUserWishes(Long userId, List<Wish> wishes) {
        String key = "user_" + userId;
        userWishesCache.put(key, wishes);
        expiration.put(key, System.currentTimeMillis() + TTL);
    }

    @Override
    public void save(Wish entity) {
        cache.put(String.valueOf(entity.getId()), entity);
        expiration.put(String.valueOf(entity.getId()), System.currentTimeMillis() + TTL);
    }

    @Override
    public void save(Object entity) {

    }

    @Override
    public void clear() {
        cache.clear();
        userWishesCache.clear();
        expiration.clear();
    }
}

