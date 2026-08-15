package zechs.zplex.userdata.model.api;

import zechs.zplex.userdata.model.Playlist;

import java.time.Instant;

public record PlaylistResponse(
        Long id,
        String name,
        Instant createdAt,
        Instant updatedAt
) {
    public static PlaylistResponse from(Playlist playlist) {
        return new PlaylistResponse(
                playlist.getId(),
                playlist.getName(),
                playlist.getCreatedAt(),
                playlist.getUpdatedAt()
        );
    }
}
