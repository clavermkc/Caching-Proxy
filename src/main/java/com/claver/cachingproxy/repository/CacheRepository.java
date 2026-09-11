package com.claver.cachingproxy.repository;

import com.claver.cachingproxy.entity.CachedResponseEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class CacheRepository {

    // Simule une base de données clé-valeur
    private final ConcurrentMap<String, CachedResponseEntity> cache = new ConcurrentHashMap<>();

    public Optional<CachedResponseEntity> findByRequestUrl(String requestUrl) {
        return Optional.ofNullable(cache.get(requestUrl));
    }

    public void save(CachedResponseEntity entity) {
        cache.put(entity.getRequestUrl(), entity);
    }

    public void clear() {
        cache.clear();
    }
}