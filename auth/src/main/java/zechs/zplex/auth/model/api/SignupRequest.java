package zechs.zplex.auth.model.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(

        @NotBlank(message = "First name cannot be empty")
        @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
        @Pattern(regexp = "^[A-Za-z]+$", message = "First name can only contain English letters, no spaces")
        String firstName,

        @NotBlank(message = "Last name cannot be empty")
        @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
        @Pattern(regexp = "^[A-Za-z]+$", message = "Last name can only contain English letters, no spaces")
        String lastName,

        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])[A-Za-z0-9]+$",
                message = "Username must include at least one letter and contain only English letters and numbers, no spaces"
        )
        String username,

        @NotBlank(message = "Password cannot be empty")
        @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
        String password
) {
}