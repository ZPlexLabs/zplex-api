package zechs.zplex.auth.model.api;

public record SignupRequest(
        String firstName,
        String lastName,
        String username,
        String password
) {
}