package com.example.commonservice.cache;

import com.example.commonservice.factory.CacheFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final Duration DEFAULT_TTL = Duration.ofHours(1);

    public <T> Optional<T> getCache(String key, Class<T> type) {
        try{
            Object cachedValue = redisTemplate.opsForValue().get(key);
            if(cachedValue == null){
                return Optional.empty();
            }
            else {
                T value = objectMapper.convertValue(cachedValue, type);
                return Optional.of(value);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Get list from cache
     */
    public <T> Optional<List<T>> getList(String key, Class<T> type) {
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached == null) {

                return Optional.empty();
            }


            List<T> list = objectMapper.convertValue(
                    cached,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, type)
            );
            return Optional.of(list);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    /**
     * Set value in cache with default TTL
     */
    public void set(String key, Object value) {
        set(key, value, DEFAULT_TTL);
    }

    /**
     * Set value in cache with custom TTL
     */
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
     */
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
     */
    public void deletePattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Deleted {} keys matching pattern: {}", keys.size(), pattern);
            }
        } catch (Exception e) {
            log.error("Error deleting cache pattern: {}", pattern, e);
        }
    }
    /**
     * Check if key exists in cache
     */
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Error checking cache existence for key: {}", key, e);
            return false;
        }
    }

    /**
     * Clear all cache
     */
    public void clearAll() {
        try {
            Set<String> keys = redisTemplate.keys("*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("Cleared all cache, {} keys deleted", keys.size());
            }
        } catch (Exception e) {
            log.error("Error clearing all cache", e);
        }
    }
}
