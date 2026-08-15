package zechs.zplex.userdata.model.api;

import zechs.zplex.common.model.MediaType;
import zechs.zplex.userdata.model.WatchProgress;

import java.time.Instant;

public record ContinueWatchingItem(
        Long id,
        MediaType mediaType,
        Integer tmdbId,
        int seasonNumber,
        int episodeNumber,
        long progressMs,
        long durationMs,
        Instant updatedAt
) {
    public static ContinueWatchingItem from(WatchProgress progress) {
        return new ContinueWatchingItem(
                progress.getId(),
                progress.getMediaType(),
                progress.getTmdbId(),
                progress.getSeasonNumber(),
                progress.getEpisodeNumber(),
                progress.getProgressMs(),
                progress.getDurationMs(),
                progress.getUpdatedAt()
        );
    }
}
