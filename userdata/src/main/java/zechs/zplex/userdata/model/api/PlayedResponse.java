package zechs.zplex.userdata.model.api;

import zechs.zplex.common.model.MediaType;
import zechs.zplex.userdata.model.Played;

import java.time.Instant;

public record PlayedResponse(
        MediaType mediaType,
        Integer tmdbId,
        int seasonNumber,
        int episodeNumber,
        Instant playedAt
) {
    public static PlayedResponse from(Played played) {
        return new PlayedResponse(
                played.getMediaType(),
                played.getTmdbId(),
                played.getSeasonNumber(),
                played.getEpisodeNumber(),
                played.getPlayedAt()
        );
    }
}
