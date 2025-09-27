package zechs.zplex.auth.model.api;

public record TokenAccessResponse(
        String accessToken,
        String tokenType
) {
    public TokenAccessResponse(String accessToken) {
        this(accessToken, "Bearer");
    }
}
