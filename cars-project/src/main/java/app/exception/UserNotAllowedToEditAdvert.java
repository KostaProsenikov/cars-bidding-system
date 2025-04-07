package app.exception;

public class UserNotAllowedToEditAdvert extends RuntimeException {
    public UserNotAllowedToEditAdvert(String message) {
        super(message);
    }
}
