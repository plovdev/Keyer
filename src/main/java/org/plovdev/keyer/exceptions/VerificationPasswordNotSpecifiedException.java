package org.plovdev.keyer.exceptions;

public class VerificationPasswordNotSpecifiedException extends KeyerException {
    public VerificationPasswordNotSpecifiedException() {
    }

    public VerificationPasswordNotSpecifiedException(KeyerStatusCode statusCode) {
        super(statusCode);
    }

    public VerificationPasswordNotSpecifiedException(String message) {
        super(message);
    }

    public VerificationPasswordNotSpecifiedException(String message, KeyerStatusCode statusCode) {
        super(message, statusCode);
    }

    public VerificationPasswordNotSpecifiedException(String message, Throwable cause) {
        super(message, cause);
    }

    public VerificationPasswordNotSpecifiedException(String message, KeyerStatusCode statusCode, Throwable cause) {
        super(message, statusCode, cause);
    }

    public VerificationPasswordNotSpecifiedException(Throwable cause) {
        super(cause);
    }

    public VerificationPasswordNotSpecifiedException(KeyerStatusCode statusCode, Throwable cause) {
        super(statusCode, cause);
    }

    public VerificationPasswordNotSpecifiedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public VerificationPasswordNotSpecifiedException(String message, KeyerStatusCode statusCode, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, statusCode, cause, enableSuppression, writableStackTrace);
    }
}