package zechs.zplex.auth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/** Valkey-backed access-token jti deny-list plus a per-user token-version cache. */
@Component
public class TokenRevocationService {

    private static final String REVOKED_JTI_PREFIX = "revoked-jti:";
    private static final String TOKEN_VERSION_PREFIX = "token-version:";

    private final StringRedisTemplate redis;

    public TokenRevocationService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void revokeAccessJti(String jti, Duration ttl) {
        if (jti == null || ttl == null || ttl.isNegative() || ttl.isZero()) {
            return;
        }
        redis.opsForValue().set(REVOKED_JTI_PREFIX + jti, "1", ttl);
    }

    public boolean isAccessJtiRevoked(String jti) {
        return jti != null && Boolean.TRUE.equals(redis.hasKey(REVOKED_JTI_PREFIX + jti));
    }

    /** Cached current token version for a user, or null when not cached (filter fails open). */
    public Integer getCachedTokenVersion(String username) {
        String value = redis.opsForValue().get(TOKEN_VERSION_PREFIX + username);
        return value == null ? null : Integer.valueOf(value);
    }

    public void cacheTokenVersion(String username, int version, Duration ttl) {
        redis.opsForValue().set(TOKEN_VERSION_PREFIX + username, String.valueOf(version), ttl);
    }
}
