package zechs.zplex.media.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Studio(
        String id,
        String name,
        @JsonProperty("logo_path") String logoPath,
        @JsonProperty("origin_country") String originCountry
) {
}