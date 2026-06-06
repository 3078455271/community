package xyz.haimianxiaozi.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class AuthRateLimiter {

    private static final String PREFIX = "auth:rate:";

    private final StringRedisTemplate redisTemplate;

    @Value("${security.rate-limit.auth.max-attempts:10}")
    private int maxAttempts;

    @Value("${security.rate-limit.auth.window-seconds:60}")
    private long windowSeconds;

    public boolean allow(String action, String identity) {
        String key = PREFIX + action + ":" + identity;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }
        return count == null || count <= maxAttempts;
    }
}