package zechs.zplex.auth.exception;

public class AdminDeletionNotAllowedException extends RuntimeException {
    public AdminDeletionNotAllowedException() {
        super("The default admin user cannot be deleted.");
    }
}