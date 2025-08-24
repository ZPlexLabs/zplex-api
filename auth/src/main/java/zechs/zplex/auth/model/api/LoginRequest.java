package zechs.zplex.auth.model.api;

public record LoginRequest(
        String username,
        String password
) {
}