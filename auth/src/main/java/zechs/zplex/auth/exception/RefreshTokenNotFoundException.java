package zechs.zplex.auth.exception;

public class RefreshTokenNotFoundException extends RuntimeException {
    public RefreshTokenNotFoundException() {
        super("Refresh token not found for user");
    }
}
