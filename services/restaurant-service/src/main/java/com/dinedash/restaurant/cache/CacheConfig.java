package com.dinedash.restaurant.cache;

import com.dinedash.restaurant.dto.MenuItemResponse;
import com.dinedash.restaurant.dto.RestaurantResponse;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import java.time.Duration;
import java.util.List;

/**
 * Read-through Redis cache for the hottest public endpoints (restaurant page and menu).
 * Entries are typed JSON so they stay readable in redis-cli, and are evicted on every write.
 */
@Slf4j
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    @Bean
    public RedisCacheManagerBuilderCustomizer dinedashCaches(ObjectMapper objectMapper) {
        JavaType menuType = objectMapper.getTypeFactory().constructCollectionType(List.class, MenuItemResponse.class);
        return builder -> builder
                .withCacheConfiguration(CacheNames.RESTAURANTS, typed(new Jackson2JsonRedisSerializer<>(
                        objectMapper, RestaurantResponse.class), Duration.ofMinutes(10)))
                .withCacheConfiguration(CacheNames.MENUS, typed(new Jackson2JsonRedisSerializer<>(
                        objectMapper, menuType), Duration.ofMinutes(10)));
    }

    private static RedisCacheConfiguration typed(Jackson2JsonRedisSerializer<?> serializer, Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith("dinedash:")
                .entryTtl(ttl)
                .disableCachingNullValues()
                .serializeValuesWith(SerializationPair.fromSerializer(serializer));
    }

    /** If Redis is down, fall back to MongoDB instead of failing the request. */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
                log.warn("Cache get failed on {}::{}: {}", cache.getName(), key, e.getMessage());
            }

            @Override
            public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
                log.warn("Cache put failed on {}::{}: {}", cache.getName(), key, e.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
                log.warn("Cache evict failed on {}::{}: {}", cache.getName(), key, e.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException e, Cache cache) {
                log.warn("Cache clear failed on {}: {}", cache.getName(), e.getMessage());
            }
        };
    }
}
