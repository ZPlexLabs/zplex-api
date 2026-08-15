package zechs.zplex.common.model;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

// Resolved per-user effective access (cached); source of truth for catalog + stream authorization.
public class UserAccess {

    private final int[] allowedLibraries;
    private final int maxRatingRank;
    private final boolean allowUnrated;
    private final Map<MediaType, Set<Integer>> blacklist;

    public UserAccess(int[] allowedLibraries, int maxRatingRank, boolean allowUnrated,
                      Map<MediaType, Set<Integer>> blacklist) {
        this.allowedLibraries = allowedLibraries == null ? new int[0] : allowedLibraries;
        this.maxRatingRank = maxRatingRank;
        this.allowUnrated = allowUnrated;
        this.blacklist = blacklist == null ? Collections.emptyMap() : blacklist;
    }

    public int[] getAllowedLibraries() {
        return allowedLibraries;
    }

    public int getMaxRatingRank() {
        return maxRatingRank;
    }

    public boolean isAllowUnrated() {
        return allowUnrated;
    }

    public boolean isLibraryAllowed(int libraryId) {
        for (int id : allowedLibraries) {
            if (id == libraryId) {
                return true;
            }
        }
        return false;
    }

    public Set<Integer> getBlacklistedTmdbIds(MediaType mediaType) {
        Set<Integer> ids = blacklist.get(mediaType);
        return ids == null ? Collections.emptySet() : Collections.unmodifiableSet(ids);
    }

    public boolean isBlacklisted(MediaType mediaType, int tmdbId) {
        Set<Integer> ids = blacklist.get(mediaType);
        return ids != null && ids.contains(tmdbId);
    }
}
