package xyz.haimianxiaozi.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String PREFIX = "auth:blacklist:";

    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.expiration}")
    private long expiration;

    public void blacklist(String token) {
        redisTemplate.opsForValue().set(PREFIX + token, "1", Duration.ofMillis(expiration));
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + token));
    }
}