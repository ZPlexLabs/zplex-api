package zechs.zplex.userdata.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zechs.zplex.common.model.MediaType;
import zechs.zplex.userdata.model.Played;
import zechs.zplex.userdata.model.api.PlayedResponse;
import zechs.zplex.userdata.repository.PlayedRepository;

import java.time.Instant;
import java.util.List;

@Service
public class PlayedService {

    private final PlayedRepository playedRepository;

    public PlayedService(PlayedRepository playedRepository) {
        this.playedRepository = playedRepository;
    }

    @Transactional
    public void markPlayed(String username, MediaType mediaType, Integer tmdbId, int seasonNumber, int episodeNumber) {
        boolean exists = playedRepository
                .existsByUsernameAndTmdbIdAndMediaTypeAndSeasonNumberAndEpisodeNumber(
                        username, tmdbId, mediaType, seasonNumber, episodeNumber);
        if (exists) {
            return;
        }
        Played played = new Played();
        played.setUsername(username);
        played.setMediaType(mediaType);
        played.setTmdbId(tmdbId);
        played.setSeasonNumber(seasonNumber);
        played.setEpisodeNumber(episodeNumber);
        played.setPlayedAt(Instant.now());
        playedRepository.save(played);
    }

    @Transactional
    public void unmarkPlayed(String username, MediaType mediaType, Integer tmdbId, int seasonNumber, int episodeNumber) {
        playedRepository.deleteByUsernameAndTmdbIdAndMediaTypeAndSeasonNumberAndEpisodeNumber(
                username, tmdbId, mediaType, seasonNumber, episodeNumber);
    }

    public List<PlayedResponse> list(String username) {
        return playedRepository.findByUsernameOrderByPlayedAtDesc(username).stream()
                .map(PlayedResponse::from)
                .toList();
    }
}
