package zechs.zplex.userdata.model.api;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PlaylistReorderRequest(
        @NotEmpty(message = "itemIds is required")
        List<Long> itemIds
) {
}
