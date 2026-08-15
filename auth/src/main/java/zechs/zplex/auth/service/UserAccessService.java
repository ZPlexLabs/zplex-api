package zechs.zplex.auth.service;

import org.springframework.stereotype.Service;
import zechs.zplex.auth.exception.UserDoesNotExist;
import zechs.zplex.auth.model.User;
import zechs.zplex.auth.model.UserBlacklist;
import zechs.zplex.auth.repository.UserBlacklistRepository;
import zechs.zplex.auth.repository.UserRepository;
import zechs.zplex.common.model.MediaType;
import zechs.zplex.common.model.UserAccess;
import zechs.zplex.common.utils.SimpleCache;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

// Resolves + caches effective per-user access; evicted on admin change so updates apply instantly.
@Service
public class UserAccessService {

    private static final int CACHE_TTL_MINUTES = 30;

    private final UserRepository userRepository;
    private final UserBlacklistRepository userBlacklistRepository;
    private final SimpleCache<String, UserAccess> cache = new SimpleCache<>();

    public UserAccessService(UserRepository userRepository, UserBlacklistRepository userBlacklistRepository) {
        this.userRepository = userRepository;
        this.userBlacklistRepository = userBlacklistRepository;
    }

    public UserAccess getAccess(String username) throws UserDoesNotExist {
        UserAccess cached = cache.get(username);
        if (cached != null) {
            return cached;
        }
        UserAccess access = resolve(username);
        cache.put(username, access, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        return access;
    }

    public void evict(String username) {
        cache.remove(username);
    }

    private UserAccess resolve(String username) throws UserDoesNotExist {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserDoesNotExist(username));

        Map<MediaType, Set<Integer>> blacklist = new EnumMap<>(MediaType.class);
        for (UserBlacklist entry : userBlacklistRepository.findByIdUsername(username)) {
            blacklist.computeIfAbsent(entry.getId().getMediaType(), key -> new HashSet<>())
                    .add(entry.getId().getTmdbId());
        }

        return new UserAccess(user.getAllowedLibraries(), user.getMaxRatingRank(),
                user.isAllowUnrated(), blacklist);
    }
}
