package zechs.zplex.auth.model.api;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(
        @NotBlank(message = "Refresh Token cannot be empty")
        String refreshToken
) {
}