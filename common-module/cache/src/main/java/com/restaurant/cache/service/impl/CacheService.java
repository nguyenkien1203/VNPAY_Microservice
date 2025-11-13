package com.restaurant.cache.service.impl;

import com.restaurant.cache.exception.CacheException;
import com.restaurant.cache.service.ICacheService;
import com.restaurant.utils.MapperUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


/**
 * The type Cache service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheService implements ICacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final Duration DEFAULT_TTL = Duration.ofHours(1);

    /**
     * Gets cache.
     *
     * @param <T>  the type parameter
     * @param key  the key
     * @param type the type
     * @return the cache
     */
    @Override
    public <T> T getCache(String key, Class<T> type) throws CacheException {
        try {
            Object cachedValue = redisTemplate.opsForValue().get(key);
            if (cachedValue == null) {
                return null;
            }
            return MapperUtil.convertValue(cachedValue, type);
        } catch (Exception e) {
            throw new CacheException(e.getMessage(), e.getMessage());
        }
    }

    /**
     * Get list from cache
     *
     * @param <T>  the type parameter
     * @param key  the key
     * @param type the type
     * @return the list
     */
    @Override
    public <T> List<T> getList(String key, Class<T> type) {
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached == null) {
                return Collections.emptyList();
            }

            return MapperUtil.convertValue(
                    cached,
                    MapperUtil.getTypeFactoryToConvertList(type)
            );
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Set value in cache with default TTL
     *
     * @param key   the key
     * @param value the value
     */
    @Override
    public void set(String key, Object value) {
        set(key, value, DEFAULT_TTL);
    }

    /**
     * Set value in cache with custom TTL
     *
     * @param key   the key
     * @param value the value
     * @param ttl   the ttl
     */
    @Override
    public void set(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl.toMillis(), TimeUnit.MILLISECONDS);
            log.debug("Cached value for key: {} with TTL: {}", key, ttl);
        } catch (Exception e) {
            log.error("Error setting cache for key: {}", key, e);
        }
    }

    /**
     * Delete a specific key from cache
     *
     * @param key the key
     */
    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Deleted cache for key: {}", key);
        } catch (Exception e) {
            log.error("Error deleting cache for key: {}", key, e);
        }
    }

    /**
     * Delete all keys matching a pattern
     *
     * @param pattern the pattern
     */
    @Override
    public void deletePattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Deleted {} keys matching pattern: {}", keys.size(), pattern);
            }
        } catch (Exception e) {
            log.error("Error deleting cache pattern: {}", pattern, e);
        }
    }

    /**
     * Check if key exists in cache
     *
     * @param key the key
     * @return the boolean
     */
    @Override
    public boolean exists(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("Error checking cache existence for key: {}", key, e);
            return false;
        }
    }

    /**
     * Clear all cache
     */
    @Override
    public void clearAll() {
        try {
            Set<String> keys = redisTemplate.keys("*");
            if (!keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Cleared all cache, {} keys deleted", keys.size());
            }
        } catch (Exception e) {
            log.error("Error clearing all cache", e);
        }
    }
}
