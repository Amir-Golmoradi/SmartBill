package dev.amirgol.smartbill.customer.domain.exception;

public class InvalidFullNameException extends RuntimeException {
    public InvalidFullNameException(String message) {
        super(message);
    }

    public InvalidFullNameException(String message, Throwable cause) {
        super(message, cause);
    }
}
