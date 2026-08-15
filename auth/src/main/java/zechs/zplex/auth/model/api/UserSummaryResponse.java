package zechs.zplex.auth.model.api;

import java.util.List;

public record UserSummaryResponse(
        String username,
        String firstName,
        String lastName,
        int[] capabilities,
        boolean isAdult,
        int[] allowedLibraries,
        int maxRatingRank,
        boolean allowUnrated,
        List<BlacklistEntry> blacklist
) {
}
