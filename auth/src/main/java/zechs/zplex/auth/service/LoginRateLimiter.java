package zechs.zplex.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/** Valkey-backed fixed-window brute-force guard for /login, keyed by client id. */
@Component
public class LoginRateLimiter {

    private static final String KEY_PREFIX = "login-attempts:";

    private final StringRedisTemplate redis;
    private final int maxAttempts;
    private final Duration window;

    public LoginRateLimiter(
            StringRedisTemplate redis,
            @Value("${zplex.login.rate-limit.max-attempts:10}") int maxAttempts,
            @Value("${zplex.login.rate-limit.window-seconds:900}") long windowSeconds) {
        this.redis = redis;
        this.maxAttempts = maxAttempts;
        this.window = Duration.ofSeconds(windowSeconds);
    }

    public boolean isAllowed(String clientId) {
        String value = redis.opsForValue().get(KEY_PREFIX + clientId);
        return value == null || Long.parseLong(value) < maxAttempts;
    }

    public void recordFailure(String clientId) {
        String key = KEY_PREFIX + clientId;
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redis.expire(key, window);
        }
    }

    public void reset(String clientId) {
        redis.delete(KEY_PREFIX + clientId);
    }

    public long retryAfterSeconds(String clientId) {
        Long ttl = redis.getExpire(KEY_PREFIX + clientId, TimeUnit.SECONDS);
        return ttl != null && ttl > 0 ? ttl : window.getSeconds();
    }
}
