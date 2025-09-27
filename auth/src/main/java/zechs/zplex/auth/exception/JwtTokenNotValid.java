package zechs.zplex.auth.exception;

public class JwtTokenNotValid extends RuntimeException {
    public JwtTokenNotValid() {
        super("JWT token not valid");
    }
}
