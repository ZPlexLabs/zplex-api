package zechs.zplex.userdata.model.api;

import zechs.zplex.userdata.model.Playlist;

import java.time.Instant;
import java.util.List;

public record PlaylistDetailResponse(
        Long id,
        String name,
        Instant createdAt,
        Instant updatedAt,
        List<PlaylistItemResponse> items
) {
    public static PlaylistDetailResponse from(Playlist playlist, List<PlaylistItemResponse> items) {
        return new PlaylistDetailResponse(
                playlist.getId(),
                playlist.getName(),
                playlist.getCreatedAt(),
                playlist.getUpdatedAt(),
                items
        );
    }
}
