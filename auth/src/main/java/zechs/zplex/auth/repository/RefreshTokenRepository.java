package zechs.zplex.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zechs.zplex.auth.model.RefreshToken;
import zechs.zplex.auth.model.User;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByJti(String jti);

    void deleteByUser(User user);

    void deleteAllByExpiryDateBefore(Instant now);
}

