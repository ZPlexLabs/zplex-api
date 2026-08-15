package zechs.zplex.userdata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zechs.zplex.common.model.MediaType;
import zechs.zplex.userdata.model.WatchlistItem;

import java.util.List;

public interface WatchlistItemRepository extends JpaRepository<WatchlistItem, Long> {

    List<WatchlistItem> findByUsernameOrderByAddedAtDesc(String username);

    boolean existsByUsernameAndTmdbIdAndMediaType(String username, Integer tmdbId, MediaType mediaType);

    void deleteByUsernameAndTmdbIdAndMediaType(String username, Integer tmdbId, MediaType mediaType);
}
