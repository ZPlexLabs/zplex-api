package zechs.zplex.auth.model;

public record AuthenticatedUser(
        User user,
        TokenType tokenType) {
}