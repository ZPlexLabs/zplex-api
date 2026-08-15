package zechs.zplex.userdata.model.api;

import zechs.zplex.common.model.MediaType;
import zechs.zplex.userdata.model.PlaylistItem;

import java.time.Instant;

public record PlaylistItemResponse(
        Long id,
        MediaType mediaType,
        Integer tmdbId,
        int position,
        Instant addedAt
) {
    public static PlaylistItemResponse from(PlaylistItem item) {
        return new PlaylistItemResponse(
                item.getId(),
                item.getMediaType(),
                item.getTmdbId(),
                item.getPosition(),
                item.getAddedAt()
        );
    }
}
