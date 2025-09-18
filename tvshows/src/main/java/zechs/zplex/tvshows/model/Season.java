package zechs.zplex.tvshows.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Season(
        Integer id,
        String name,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("release_date") String releaseDate,
        @JsonProperty("release_year") Short releaseYear,
        @JsonProperty("season_number") Short seasonNumber,
        @JsonProperty("episodes_count") Short EpisodeCount
) {
}