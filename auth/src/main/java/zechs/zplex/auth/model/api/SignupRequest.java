package zechs.zplex.auth.model.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(

        @NotBlank(message = "First name cannot be empty")
        @Pattern(regexp = "[A-Za-z]+", message = "First name can only contain letters, no spaces")
        String firstName,

        @NotBlank(message = "Last name cannot be empty")
        @Pattern(regexp = "[A-Za-z]+", message = "First name can only contain letters, no spaces")
        String lastName,

        @NotBlank(message = "Username is required")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])[A-Za-z0-9]+$",
                message = "Username must include at least one letter and contain only letters and numbers, no spaces."
        )
        String username,

        @NotBlank(message = "Password cannot be empty")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password
) {
}