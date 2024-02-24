package com.example.inventorysystem.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class RedisLockRepository {

    public static final String LOCK_VALUE = "lock";
    public static final int DURATION_TIMEOUT_MILLIS = 3_000;
    private final RedisTemplate<String, String> redisTemplate;

    public RedisLockRepository(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean lock(Long id) {
        return redisTemplate
                .opsForValue()
                .setIfAbsent(
                        generateKey(id),
                        LOCK_VALUE,
                        Duration.ofMillis(DURATION_TIMEOUT_MILLIS)
                );
    }

    private String generateKey(Long id) {
        return String.valueOf(id);
    }

    public boolean unlock(Long id) {
        return redisTemplate.delete(generateKey(id));
    }
}
