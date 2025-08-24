package zechs.zplex.auth.service;

import org.springframework.stereotype.Service;
import zechs.zplex.auth.exception.ExpiredRefreshToken;
import zechs.zplex.auth.model.RefreshToken;
import zechs.zplex.auth.model.User;
import zechs.zplex.auth.model.api.TokenAccessResponse;
import zechs.zplex.auth.model.api.TokenRefreshResponse;
import zechs.zplex.auth.repository.RefreshTokenRepository;
import zechs.zplex.auth.utils.JwtUtil;

import java.time.Instant;
import java.util.Optional;

@Service
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    public TokenService(RefreshTokenRepository refreshTokenRepository, JwtUtil jwtUtil) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
    }

    public TokenAccessResponse createAccessToken(User user) {
        return new TokenAccessResponse(jwtUtil.generateRefreshToken(user));
    }

    public TokenRefreshResponse createRefreshToken(User user) {
        String newRefreshToken = jwtUtil.generateRefreshToken(user);
        String newAccessToken = jwtUtil.generateAccessToken(user);
        Instant refreshExpiry = jwtUtil.extractExpiration(newRefreshToken).toInstant();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(newRefreshToken);
        refreshToken.setExpiryDate(refreshExpiry);

        refreshTokenRepository.save(refreshToken);
        return new TokenRefreshResponse(newAccessToken, newRefreshToken);
    }

    public boolean validateRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .map(rt -> rt.getExpiryDate().isAfter(Instant.now()))
                .orElse(false);
    }

    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public void verifyExpiration(RefreshToken token) throws ExpiredRefreshToken {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new ExpiredRefreshToken();
        }
    }

}
