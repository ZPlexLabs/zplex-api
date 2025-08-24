package zechs.zplex.auth.model.api;

public record TokenRefreshRequest(
        String refreshToken
) {
}