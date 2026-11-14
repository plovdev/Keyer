package org.plovdev.keyer.exceptions;

public class VerificationPasswordNotSpecified extends KeyerException {
    public VerificationPasswordNotSpecified() {
    }

    public VerificationPasswordNotSpecified(KeyerStatusCode statusCode) {
        super(statusCode);
    }

    public VerificationPasswordNotSpecified(String message) {
        super(message);
    }

    public VerificationPasswordNotSpecified(String message, KeyerStatusCode statusCode) {
        super(message, statusCode);
    }

    public VerificationPasswordNotSpecified(String message, Throwable cause) {
        super(message, cause);
    }

    public VerificationPasswordNotSpecified(String message, KeyerStatusCode statusCode, Throwable cause) {
        super(message, statusCode, cause);
    }

    public VerificationPasswordNotSpecified(Throwable cause) {
        super(cause);
    }

    public VerificationPasswordNotSpecified(KeyerStatusCode statusCode, Throwable cause) {
        super(statusCode, cause);
    }

    public VerificationPasswordNotSpecified(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public VerificationPasswordNotSpecified(String message, KeyerStatusCode statusCode, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, statusCode, cause, enableSuppression, writableStackTrace);
    }
}