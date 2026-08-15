package zechs.zplex.userdata.model.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import zechs.zplex.common.model.MediaType;

public record ProgressUpdateRequest(
        @NotNull(message = "mediaType is required")
        MediaType mediaType,

        @NotNull(message = "tmdbId is required")
        Integer tmdbId,

        @Min(value = 0, message = "seasonNumber must be >= 0")
        Integer seasonNumber,

        @Min(value = 0, message = "episodeNumber must be >= 0")
        Integer episodeNumber,

        @NotNull(message = "progressMs is required")
        @Min(value = 0, message = "progressMs must be >= 0")
        Long progressMs,

        @NotNull(message = "durationMs is required")
        @Min(value = 0, message = "durationMs must be >= 0")
        Long durationMs
) {
    public int seasonNumberOrZero() {
        return seasonNumber == null ? 0 : seasonNumber;
    }

    public int episodeNumberOrZero() {
        return episodeNumber == null ? 0 : episodeNumber;
    }
}
