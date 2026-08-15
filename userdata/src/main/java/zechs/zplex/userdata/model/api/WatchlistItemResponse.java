package zechs.zplex.userdata.model.api;

import zechs.zplex.common.model.MediaType;
import zechs.zplex.userdata.model.WatchlistItem;

import java.time.Instant;

public record WatchlistItemResponse(
        MediaType mediaType,
        Integer tmdbId,
        Instant addedAt
) {
    public static WatchlistItemResponse from(WatchlistItem item) {
        return new WatchlistItemResponse(item.getMediaType(), item.getTmdbId(), item.getAddedAt());
    }
}
