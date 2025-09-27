package zechs.zplex.auth.exception;

public class ExpiredRefreshToken extends RuntimeException {
    public ExpiredRefreshToken() {
        super("Refresh token is expired. Please login again.");
    }
}
