package app.exception;

public class NumberFormatExceptionHandler  extends RuntimeException {
    public NumberFormatExceptionHandler(String message) {
        super(message);
    }
}
