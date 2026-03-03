package gift.auth;

public class ForbiddenException extends RuntimeException {
    public ForbiddenException() {
        super("Access denied.");
    }
}
