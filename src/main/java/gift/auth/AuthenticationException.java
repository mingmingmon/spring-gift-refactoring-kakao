package gift.auth;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException() {
        super("Authentication failed.");
    }
}
