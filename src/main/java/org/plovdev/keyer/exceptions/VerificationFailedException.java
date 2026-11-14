package org.plovdev.keyer.exceptions;

public class VerificationFailedException extends KeyerException {
    public VerificationFailedException() {
    }

    public VerificationFailedException(KeyerStatusCode statusCode) {
        super(statusCode);
    }

    public VerificationFailedException(String message) {
        super(message);
    }

    public VerificationFailedException(String message, KeyerStatusCode statusCode) {
        super(message, statusCode);
    }

    public VerificationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public VerificationFailedException(String message, KeyerStatusCode statusCode, Throwable cause) {
        super(message, statusCode, cause);
    }

    public VerificationFailedException(Throwable cause) {
        super(cause);
    }

    public VerificationFailedException(KeyerStatusCode statusCode, Throwable cause) {
        super(statusCode, cause);
    }

    public VerificationFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public VerificationFailedException(String message, KeyerStatusCode statusCode, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, statusCode, cause, enableSuppression, writableStackTrace);
    }
}