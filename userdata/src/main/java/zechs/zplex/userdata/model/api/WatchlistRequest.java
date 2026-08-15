package zechs.zplex.userdata.model.api;

import jakarta.validation.constraints.NotNull;
import zechs.zplex.common.model.MediaType;

public record WatchlistRequest(
        @NotNull(message = "mediaType is required")
        MediaType mediaType,

        @NotNull(message = "tmdbId is required")
        Integer tmdbId
) {
}
