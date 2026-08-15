package zechs.zplex.userdata.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zechs.zplex.userdata.model.WatchProgress;
import zechs.zplex.userdata.model.api.ContinueWatchingItem;
import zechs.zplex.userdata.model.api.ProgressUpdateRequest;
import zechs.zplex.userdata.repository.WatchProgressRepository;

import java.time.Instant;
import java.util.List;

@Service
public class WatchProgressService {

    private static final double COMPLETION_THRESHOLD = 0.90;

    private final WatchProgressRepository watchProgressRepository;
    private final PlayedService playedService;

    public WatchProgressService(WatchProgressRepository watchProgressRepository, PlayedService playedService) {
        this.watchProgressRepository = watchProgressRepository;
        this.playedService = playedService;
    }

    @Transactional
    public void upsertProgress(String username, ProgressUpdateRequest request) {
        int season = request.seasonNumberOrZero();
        int episode = request.episodeNumberOrZero();
        WatchProgress progress = watchProgressRepository
                .findByUsernameAndTmdbIdAndMediaTypeAndSeasonNumberAndEpisodeNumber(
                        username, request.tmdbId(), request.mediaType(), season, episode)
                .orElseGet(WatchProgress::new);
        progress.setUsername(username);
        progress.setTmdbId(request.tmdbId());
        progress.setMediaType(request.mediaType());
        progress.setSeasonNumber(season);
        progress.setEpisodeNumber(episode);
        progress.setProgressMs(request.progressMs());
        progress.setDurationMs(request.durationMs());
        progress.setUpdatedAt(Instant.now());
        watchProgressRepository.save(progress);

        if (!isInProgress(progress)) {
            playedService.markPlayed(username, request.mediaType(), request.tmdbId(), season, episode);
        }
    }

    public List<ContinueWatchingItem> getContinueWatching(String username) {
        return watchProgressRepository.findByUsernameOrderByUpdatedAtDesc(username).stream()
                .filter(this::isInProgress)
                .map(ContinueWatchingItem::from)
                .toList();
    }

    public List<ContinueWatchingItem> getHistory(String username) {
        return watchProgressRepository.findByUsernameOrderByUpdatedAtDesc(username).stream()
                .map(ContinueWatchingItem::from)
                .toList();
    }

    private boolean isInProgress(WatchProgress progress) {
        if (progress.getDurationMs() <= 0) {
            return true;
        }
        return (double) progress.getProgressMs() / progress.getDurationMs() < COMPLETION_THRESHOLD;
    }
}
