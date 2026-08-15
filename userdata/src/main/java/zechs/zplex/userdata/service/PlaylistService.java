package zechs.zplex.userdata.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zechs.zplex.common.model.MediaType;
import zechs.zplex.userdata.model.Playlist;
import zechs.zplex.userdata.model.PlaylistItem;
import zechs.zplex.userdata.model.api.PlaylistDetailResponse;
import zechs.zplex.userdata.model.api.PlaylistItemResponse;
import zechs.zplex.userdata.model.api.PlaylistResponse;
import zechs.zplex.userdata.repository.PlaylistItemRepository;
import zechs.zplex.userdata.repository.PlaylistRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Service
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistItemRepository playlistItemRepository;

    public PlaylistService(PlaylistRepository playlistRepository,
                           PlaylistItemRepository playlistItemRepository) {
        this.playlistRepository = playlistRepository;
        this.playlistItemRepository = playlistItemRepository;
    }

    @Transactional
    public PlaylistResponse create(String username, String name) {
        Instant now = Instant.now();
        Playlist playlist = new Playlist();
        playlist.setUsername(username);
        playlist.setName(name);
        playlist.setCreatedAt(now);
        playlist.setUpdatedAt(now);
        return PlaylistResponse.from(playlistRepository.save(playlist));
    }

    public List<PlaylistResponse> list(String username) {
        return playlistRepository.findByUsernameOrderByUpdatedAtDesc(username).stream()
                .map(PlaylistResponse::from)
                .toList();
    }

    public Optional<PlaylistDetailResponse> get(String username, Long playlistId) {
        return playlistRepository.findByIdAndUsername(playlistId, username)
                .map(playlist -> PlaylistDetailResponse.from(playlist, itemsOf(playlistId)));
    }

    @Transactional
    public boolean rename(String username, Long playlistId, String name) {
        Optional<Playlist> found = playlistRepository.findByIdAndUsername(playlistId, username);
        if (found.isEmpty()) {
            return false;
        }
        Playlist playlist = found.get();
        playlist.setName(name);
        playlist.setUpdatedAt(Instant.now());
        playlistRepository.save(playlist);
        return true;
    }

    @Transactional
    public boolean delete(String username, Long playlistId) {
        Optional<Playlist> found = playlistRepository.findByIdAndUsername(playlistId, username);
        if (found.isEmpty()) {
            return false;
        }
        playlistItemRepository.deleteByPlaylistId(playlistId);
        playlistRepository.delete(found.get());
        return true;
    }

    @Transactional
    public boolean addItem(String username, Long playlistId, MediaType mediaType, Integer tmdbId) {
        Optional<Playlist> found = playlistRepository.findByIdAndUsername(playlistId, username);
        if (found.isEmpty()) {
            return false;
        }
        if (playlistItemRepository.existsByPlaylistIdAndTmdbIdAndMediaType(playlistId, tmdbId, mediaType)) {
            return true;
        }
        PlaylistItem last = playlistItemRepository.findFirstByPlaylistIdOrderByPositionDesc(playlistId);
        int nextPosition = last == null ? 0 : last.getPosition() + 1;
        PlaylistItem item = new PlaylistItem();
        item.setPlaylistId(playlistId);
        item.setMediaType(mediaType);
        item.setTmdbId(tmdbId);
        item.setPosition(nextPosition);
        item.setAddedAt(Instant.now());
        playlistItemRepository.save(item);
        touch(found.get());
        return true;
    }

    @Transactional
    public boolean removeItem(String username, Long playlistId, Long itemId) {
        Optional<Playlist> found = playlistRepository.findByIdAndUsername(playlistId, username);
        if (found.isEmpty()) {
            return false;
        }
        boolean removed = playlistItemRepository.deleteByIdAndPlaylistId(itemId, playlistId) > 0;
        if (removed) {
            touch(found.get());
        }
        return removed;
    }

    @Transactional
    public boolean reorder(String username, Long playlistId, List<Long> itemIds) {
        Optional<Playlist> found = playlistRepository.findByIdAndUsername(playlistId, username);
        if (found.isEmpty()) {
            return false;
        }
        Map<Long, PlaylistItem> byId = playlistItemRepository.findByPlaylistIdOrderByPositionAsc(playlistId).stream()
                .collect(java.util.stream.Collectors.toMap(PlaylistItem::getId, Function.identity()));
        int position = 0;
        for (Long itemId : itemIds) {
            PlaylistItem item = byId.get(itemId);
            if (item != null) {
                item.setPosition(position++);
            }
        }
        playlistItemRepository.saveAll(byId.values());
        touch(found.get());
        return true;
    }

    private List<PlaylistItemResponse> itemsOf(Long playlistId) {
        return playlistItemRepository.findByPlaylistIdOrderByPositionAsc(playlistId).stream()
                .map(PlaylistItemResponse::from)
                .toList();
    }

    private void touch(Playlist playlist) {
        playlist.setUpdatedAt(Instant.now());
        playlistRepository.save(playlist);
    }
}
