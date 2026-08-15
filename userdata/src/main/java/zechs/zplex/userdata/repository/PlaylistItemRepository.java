package zechs.zplex.userdata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zechs.zplex.common.model.MediaType;
import zechs.zplex.userdata.model.PlaylistItem;

import java.util.List;

public interface PlaylistItemRepository extends JpaRepository<PlaylistItem, Long> {

    List<PlaylistItem> findByPlaylistIdOrderByPositionAsc(Long playlistId);

    boolean existsByPlaylistIdAndTmdbIdAndMediaType(Long playlistId, Integer tmdbId, MediaType mediaType);

    PlaylistItem findFirstByPlaylistIdOrderByPositionDesc(Long playlistId);

    long deleteByIdAndPlaylistId(Long id, Long playlistId);

    void deleteByPlaylistId(Long playlistId);
}
