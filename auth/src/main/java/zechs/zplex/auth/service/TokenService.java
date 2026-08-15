package zechs.zplex.auth.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zechs.zplex.auth.exception.ExpiredRefreshToken;
import zechs.zplex.auth.model.RefreshToken;
import zechs.zplex.auth.model.User;
import zechs.zplex.auth.model.api.TokenAccessResponse;
import zechs.zplex.auth.model.api.TokenRefreshResponse;
import zechs.zplex.auth.repository.RefreshTokenRepository;
import zechs.zplex.auth.utils.JwtUtil;

@Service
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final TokenRevocationService tokenRevocationService;
    private final UserService userService;

    public TokenService(RefreshTokenRepository refreshTokenRepository, JwtUtil jwtUtil,
                        TokenRevocationService tokenRevocationService, UserService userService) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
        this.tokenRevocationService = tokenRevocationService;
        this.userService = userService;
    }

    public TokenAccessResponse createAccessToken(User user) {
        return new TokenAccessResponse(jwtUtil.generateAccessToken(user));
    }

    public TokenRefreshResponse createRefreshToken(User user) {
        String newRefreshToken = jwtUtil.generateRefreshToken(user);
        String newAccessToken = jwtUtil.generateAccessToken(user);
        Instant refreshExpiry = jwtUtil.extractExpiration(newRefreshToken).toInstant();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setJti(jwtUtil.extractJti(newRefreshToken));
        refreshToken.setExpiryDate(refreshExpiry);

        refreshTokenRepository.save(refreshToken);
        tokenRevocationService.cacheTokenVersion(
                user.getUsername(), user.getTokenVersion(), jwtUtil.accessTokenValidity());
        return new TokenRefreshResponse(newAccessToken, newRefreshToken);
    }

    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    public Optional<RefreshToken> findToken(String token) {
        String jti = jwtUtil.extractJti(token);
        return refreshTokenRepository.findByJti(jti);
    }

    public void verifyExpiration(RefreshToken token) throws ExpiredRefreshToken {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new ExpiredRefreshToken();
        }
    }

    // True only when the presented refresh token's version matches the user's current version.
    public boolean isRefreshTokenVersionCurrent(String refreshToken, User user) {
        return jwtUtil.extractTokenVersion(refreshToken) == user.getTokenVersion();
    }

    // Single-device logout: deny the presented access token now and drop its refresh token.
    @Transactional
    public void logout(User user, String accessToken, String refreshToken) {
        revokeAccessToken(accessToken);
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenRepository.deleteByJtiAndUser(jwtUtil.extractJti(refreshToken), user);
        } else {
            refreshTokenRepository.deleteByUser(user);
        }
    }

    // All-device logout: bump the token version (invalidates every refresh token) and deny the access token.
    @Transactional
    public void logoutAll(User user, String accessToken) {
        int newVersion = userService.incrementTokenVersion(user);
        refreshTokenRepository.deleteByUser(user);
        tokenRevocationService.cacheTokenVersion(
                user.getUsername(), newVersion, jwtUtil.accessTokenValidity());
        revokeAccessToken(accessToken);
    }

    private void revokeAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank()) {
            return;
        }
        Instant expiry = jwtUtil.extractExpiration(accessToken).toInstant();
        Duration ttl = Duration.between(Instant.now(), expiry);
        tokenRevocationService.revokeAccessJti(jwtUtil.extractJti(accessToken), ttl);
    }

}
