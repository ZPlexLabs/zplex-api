package zechs.zplex.userdata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zechs.zplex.common.model.MediaType;
import zechs.zplex.userdata.model.WatchProgress;

import java.util.List;
import java.util.Optional;

public interface WatchProgressRepository extends JpaRepository<WatchProgress, Long> {

    List<WatchProgress> findByUsernameOrderByUpdatedAtDesc(String username);

    Optional<WatchProgress> findByUsernameAndTmdbIdAndMediaTypeAndSeasonNumberAndEpisodeNumber(
            String username, Integer tmdbId, MediaType mediaType, int seasonNumber, int episodeNumber);

    void deleteByUsernameAndTmdbIdAndMediaType(String username, Integer tmdbId, MediaType mediaType);
}
