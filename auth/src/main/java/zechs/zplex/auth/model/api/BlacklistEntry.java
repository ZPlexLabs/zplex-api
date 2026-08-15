package zechs.zplex.auth.model.api;

import zechs.zplex.common.model.MediaType;

public record BlacklistEntry(MediaType mediaType, int tmdbId) {
}
