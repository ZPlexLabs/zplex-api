package zechs.zplex.userdata.model.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import zechs.zplex.common.model.MediaType;

public record PlayedRequest(
        @NotNull(message = "mediaType is required")
        MediaType mediaType,

        @NotNull(message = "tmdbId is required")
        Integer tmdbId,

        @Min(value = 0, message = "seasonNumber must be >= 0")
        Integer seasonNumber,

        @Min(value = 0, message = "episodeNumber must be >= 0")
        Integer episodeNumber
) {
    public int seasonNumberOrZero() {
        return seasonNumber == null ? 0 : seasonNumber;
    }

    public int episodeNumberOrZero() {
        return episodeNumber == null ? 0 : episodeNumber;
    }
}
