package zechs.zplex.auth.model.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UserAccessRequest(
        @NotNull(message = "allowedLibraries is required")
        int[] allowedLibraries,

        @NotNull(message = "maxRatingRank is required")
        @Min(value = 0, message = "maxRatingRank must be >= 0")
        Integer maxRatingRank,

        @NotNull(message = "allowUnrated is required")
        Boolean allowUnrated
) {
}
