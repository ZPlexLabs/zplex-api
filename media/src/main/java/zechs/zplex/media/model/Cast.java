package zechs.zplex.media.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Cast(
        String name,
        String role,
        String image,
        String gender) {
}
