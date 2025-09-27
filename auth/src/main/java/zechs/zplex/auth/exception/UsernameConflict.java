package zechs.zplex.auth.exception;

public class UsernameConflict extends RuntimeException {
    public UsernameConflict(String username) {
        super("Username: " + username + " already exists");
    }
}
