package zechs.zplex.auth.exception;

public class UserDoesNotExist extends RuntimeException {
    public UserDoesNotExist(String username) {
        super("User " + username + " does not exist");
    }
}
