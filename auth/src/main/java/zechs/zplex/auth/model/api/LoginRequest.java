package zechs.zplex.auth.model.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(

        @NotBlank(message = "Username is required")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])[A-Za-z0-9]+$",
                message = "Username must include at least one letter and contain only letters and numbers"
        )
        String username,

        @NotBlank(message = "Password cannot be empty")
        String password
) {
}