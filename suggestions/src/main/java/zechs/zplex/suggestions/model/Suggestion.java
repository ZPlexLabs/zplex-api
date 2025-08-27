package zechs.zplex.suggestions.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Suggestion(
        Integer tmdbId,
        String title,
        String posterPath,
        String backdropPath,
        String release,
        String type
) {
}