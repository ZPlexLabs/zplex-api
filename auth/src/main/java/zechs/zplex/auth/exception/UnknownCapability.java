package zechs.zplex.auth.exception;

public class UnknownCapability extends RuntimeException {
    public UnknownCapability(String capability) {
        super("Unknown capability: " + capability);
    }
}
