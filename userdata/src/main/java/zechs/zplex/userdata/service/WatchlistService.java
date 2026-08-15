package zechs.zplex.userdata.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zechs.zplex.common.model.MediaType;
import zechs.zplex.userdata.model.WatchlistItem;
import zechs.zplex.userdata.model.api.WatchlistItemResponse;
import zechs.zplex.userdata.repository.WatchlistItemRepository;

import java.time.Instant;
import java.util.List;

@Service
public class WatchlistService {

    private final WatchlistItemRepository watchlistItemRepository;

    public WatchlistService(WatchlistItemRepository watchlistItemRepository) {
        this.watchlistItemRepository = watchlistItemRepository;
    }

    @Transactional
    public void add(String username, MediaType mediaType, Integer tmdbId) {
        if (watchlistItemRepository.existsByUsernameAndTmdbIdAndMediaType(username, tmdbId, mediaType)) {
            return;
        }
        WatchlistItem item = new WatchlistItem();
        item.setUsername(username);
        item.setMediaType(mediaType);
        item.setTmdbId(tmdbId);
        item.setAddedAt(Instant.now());
        watchlistItemRepository.save(item);
    }

    @Transactional
    public void remove(String username, MediaType mediaType, Integer tmdbId) {
        watchlistItemRepository.deleteByUsernameAndTmdbIdAndMediaType(username, tmdbId, mediaType);
    }

    public List<WatchlistItemResponse> list(String username) {
        return watchlistItemRepository.findByUsernameOrderByAddedAtDesc(username).stream()
                .map(WatchlistItemResponse::from)
                .toList();
    }
}
