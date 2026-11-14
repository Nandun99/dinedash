package com.dinedash.restaurant.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/** Single place that knows which cache entries a catalog change invalidates. */
@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogCacheEvictor {

    private final CacheManager cacheManager;

    public void restaurantChanged(String restaurantId) {
        evict(CacheNames.RESTAURANTS, restaurantId);
    }

    public void menuChanged(String restaurantId) {
        evict(CacheNames.MENUS, restaurantId);
    }

    private void evict(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            try {
                cache.evict(key);
            } catch (RuntimeException e) {
                log.warn("Could not evict {}::{}: {}", cacheName, key, e.getMessage());
            }
        }
    }
}
