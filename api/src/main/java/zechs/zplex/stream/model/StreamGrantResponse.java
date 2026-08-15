package zechs.zplex.stream.model;

public record StreamGrantResponse(
        String grant,
        String tokenType
) {
    public StreamGrantResponse(String grant) {
        this(grant, "Bearer");
    }
}
