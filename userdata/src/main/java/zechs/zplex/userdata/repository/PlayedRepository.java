package zechs.zplex.userdata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import zechs.zplex.common.model.MediaType;
import zechs.zplex.userdata.model.Played;

import java.util.List;

public interface PlayedRepository extends JpaRepository<Played, Long> {

    List<Played> findByUsernameOrderByPlayedAtDesc(String username);

    boolean existsByUsernameAndTmdbIdAndMediaTypeAndSeasonNumberAndEpisodeNumber(
            String username, Integer tmdbId, MediaType mediaType, int seasonNumber, int episodeNumber);

    void deleteByUsernameAndTmdbIdAndMediaTypeAndSeasonNumberAndEpisodeNumber(
            String username, Integer tmdbId, MediaType mediaType, int seasonNumber, int episodeNumber);
}
