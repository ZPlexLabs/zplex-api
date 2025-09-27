package zechs.zplex.auth.tasks;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import zechs.zplex.auth.repository.RefreshTokenRepository;

import java.time.Instant;

@Component
public class RefreshTokenCleanupTask {

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenCleanupTask(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    // Runs every day at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanUpExpiredTokens() {
        refreshTokenRepository.deleteAllByExpiryDateBefore(Instant.now());
    }
}
